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

    private String updateURL = "https://raw.githubusercontent.com/Kreck-Projekt/RaspiOpener/master/src/version.yapion";

    public void setRepo(String userName) {
        updateURL = "https://raw.githubusercontent.com/" + userName + "/RaspiOpener/master/src/version.yapion";
    }

    @Getter
    private YAPIONObject currentVersion = null;

    public static void main(String[] args) {
        System.out.println(checkForUpdate());
    }

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
        if (currentVersion == null) {
            currentVersion = YAPIONParser.parse(Updater.class.getResourceAsStream("/version.yapion"));
        }

        try {
            URL versionCheckUrl = new URL(updateURL);
            YAPIONObject remoteVersion = YAPIONParser.parse(versionCheckUrl.openStream());
            int remoteBuild = remoteVersion.getPlainValueOrDefault("build", -1);
            int currentBuild  = currentVersion.getPlainValueOrDefault("build", -1);

            if (remoteBuild == -1 || currentBuild == -1) {
                return new UpdateResult(UpdateType.INVALID);
            }

            if (remoteBuild > currentBuild) {
                return new UpdateResult(UpdateType.UPDATE_AVAILABLE, remoteVersion.getValue("version", "").get());
            }
            return new UpdateResult(UpdateType.NO_UPDATE);
        } catch (IOException e) {
            return new UpdateResult(UpdateType.UPDATE_CHECK_FAILED);
        }
    }

}
