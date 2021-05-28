package de.NikomitK.RaspiOpener.main;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.IOException;
import java.net.URL;

@UtilityClass
public class Updater {

    private String repoUrl = "https://github.com/Kreck-Projekt/RaspiOpener";
    private String updateURL = "https://raw.githubusercontent.com/Kreck-Projekt/RaspiOpener/master/src/version.yapion";

    public void setRepo(String userName) {
        repoUrl = "https://github.com/" + userName + "/RaspiOpener";
        updateURL = "https://raw.githubusercontent.com/" + userName + "/RaspiOpener/master/src/version.yapion";
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
            Main.logger.debug("Update check with: " + updateURL);
            URL versionCheckUrl = new URL(updateURL);
            remoteVersion = YAPIONParser.parse(versionCheckUrl.openStream());
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
        int currentBuild  = currentVersion.getPlainValueOrDefault("build", -1);

        Main.logger.debug("RemoteBuild: " + remoteBuild);
        Main.logger.debug("CurrentBuild: " + currentBuild);

        if (remoteBuild == -1 || currentBuild == -1) {
            Main.logger.debug("Update: INVALID");
            return new UpdateResult(UpdateType.INVALID);
        }

        if (remoteBuild > currentBuild) {
            Main.logger.debug("Update: UPDATE_AVAILABLE " + remoteVersion.getValue("version", ""));
            return new UpdateResult(UpdateType.UPDATE_AVAILABLE, remoteVersion.getValue("version", "").get());
        }
        Main.logger.debug("Update: NO_UPDATE");
        return new UpdateResult(UpdateType.NO_UPDATE);
    }

    public void update() {
        try {
            Main.logger.debug("Exec: screen -dm ./updater.sh " + repoUrl + " \"" + Main.getArguments() + "\"");
            Runtime.getRuntime().exec(new String[]{"screen", "-dm", "./updater.sh", repoUrl, Main.getArguments()});
        } catch (IOException e) {
            Main.logger.warn(e);
        }
    }

}
