package de.NikomitK.RaspiOpener.main;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@UtilityClass
public class Updater {

    @Getter
    private String repoUrl = "https://github.com/Kreck-Projekt/RaspiOpener";

    public void setRepo(String userName) {
        repoUrl = "https://github.com/" + userName + "/RaspiOpener";
    }

    @Getter
    private YAPIONObject currentVersion = null;

    @Getter
    private YAPIONObject remoteVersion = null;
    private long remoteVersionUpdateTime = 0;

    public enum UpdateType {
        UPDATE_AVAILABLE,
        NO_UPDATE,
        UPDATE_CHECK_FAILED,
        INVALID
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class UpdateResult {
        private final UpdateType updateType;
        private String updateVersion = "";
    }

    public UpdateResult checkForUpdate() {
        if (System.currentTimeMillis() - remoteVersionUpdateTime < 60000) {
            Main.logger.debug("Direct return, Version known");
            return updateCheck();
        }
        if (currentVersion == null) {
            currentVersion = YAPIONParser.parse(Updater.class.getResourceAsStream("/version.yapion"));
            Main.logger.debug("CurrentVersion: " + currentVersion);
        }

        try {
            Main.logger.debug("Update check with: " + repoUrl);
            Runtime.getRuntime().exec("./updateRepo.sh", new String[]{repoUrl});

            remoteVersion = YAPIONParser.parse(new FileInputStream(new File("RaspiOpener/src/version.yapion")));
            remoteVersionUpdateTime = System.currentTimeMillis();
            Main.logger.debug("RemoteVersion: " + remoteVersion);
            return updateCheck();
        } catch (IOException e) {
            Main.logger.warn("Update: UPDATE_CHECK_FAILED");
            Main.logger.warn(e);
            return new UpdateResult(UpdateType.UPDATE_CHECK_FAILED);
        }
    }

    private UpdateResult updateCheck() {
        int remoteBuild = remoteVersion.getPlainValueOrDefault("build", -1);
        int currentBuild = currentVersion.getPlainValueOrDefault("build", -1);

        Main.logger.debug("RemoteBuild: " + remoteBuild);
        Main.logger.debug("CurrentBuild: " + currentBuild);

        if (remoteBuild == -1 || currentBuild == -1) {
            Main.logger.debug("Update: INVALID");
            return new UpdateResult(UpdateType.INVALID);
        }

        if (remoteBuild > currentBuild) {
            Main.logger.debug("Update: UPDATE_AVAILABLE " + remoteVersion.getValue("version", ""));
            return new UpdateResult(UpdateType.UPDATE_AVAILABLE, remoteVersion.getValue("version", "").get() + " (" + remoteVersion.getValue("build", 0).get() + ")");
        }
        Main.logger.debug("Update: NO_UPDATE");
        return new UpdateResult(UpdateType.NO_UPDATE);
    }

    public void update(boolean threaded) {
        Runnable updateRunnable = () -> {
            try {
                Main.logger.debug("Exec: ./updaterRepo.sh " + repoUrl);
                Runtime.getRuntime().exec("./updateRepo.sh", new String[]{repoUrl});
                Main.logger.debug("Exec: ./buildRepo.sh");
                Runtime.getRuntime().exec("./buildRepo.sh");
                Main.logger.debug("Exec: screen -dm ./restart.sh " + Main.getArguments());
                Runtime.getRuntime().exec(new String[]{"screen", "-dm", "./restart.sh \"", Main.getArguments() + "\""});
                Main.logger.debug("Exiting");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
                Main.logger.warn(e);
            }
        };
        if (threaded) {
            Thread thread = new Thread(updateRunnable);
            thread.setDaemon(true);
            thread.start();
        } else {
            updateRunnable.run();
        }
    }
}
