package de.NikomitK.RaspiOpener.main;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.*;

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
        private String updateDescription = null;
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
            process(Runtime.getRuntime().exec("./updateRepo.sh", new String[]{repoUrl}));

            remoteVersion = YAPIONParser.parse(new FileInputStream(new File("./RaspiOpener/src/version.yapion")));
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
            String description = null;
            if (remoteVersion.containsKey("description", String.class)) {
                description = remoteVersion.getPlainValue("description");
            }
            return new UpdateResult(UpdateType.UPDATE_AVAILABLE, remoteVersion.getValue("version", "").get() + " (" + remoteVersion.getValue("build", 0).get() + ")", description);
        }
        Main.logger.debug("Update: NO_UPDATE");
        return new UpdateResult(UpdateType.NO_UPDATE);
    }

    private boolean running = false;
    private boolean updateCanceled = false;

    public void update(boolean threaded, long timeOffset) {
        if (timeOffset < 0) {
            running = false;
            updateCanceled = true;
            Main.logger.log("Stop Update");
            return;
        }
        if (running || updateCanceled) {
            return;
        }
        Main.logger.log("Start Update");
        if (timeOffset > 0) {
            threaded = true;
        }
        running = true;
        long updateTime = System.currentTimeMillis() + timeOffset;
        Runnable updateRunnable = () -> {
            while (running && System.currentTimeMillis() < updateTime) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (!running) {
                updateCanceled = false;
                return;
            }
            Main.logger.log("Updating now!");
            try {
                Main.logger.debug("Exec: ./updateRepo.sh " + repoUrl);
                process(Runtime.getRuntime().exec("./updateRepo.sh", new String[]{repoUrl}));
                Main.logger.debug("Exec: ./buildRepo.sh");
                process(Runtime.getRuntime().exec("./buildRepo.sh"));
                Main.logger.debug("Exec: ./preRestart.sh " + Main.getArguments() + "");
                process(Runtime.getRuntime().exec("./preRestart.sh " + Main.getArguments()));
                Main.logger.debug("Exiting");
                System.exit(0);
            } catch (IOException e) {
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

    public void process(Process process) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (process.isAlive()) {
            if (bufferedReader.ready()) {
                Main.logger.bash(bufferedReader.readLine());
            }
        }
        bufferedReader.lines().forEach(s -> {
            Main.logger.bash(s);
        });
    }
}
