package de.NikomitK.RaspiOpener.handler;

import java.io.File;
import java.io.IOException;

public class Bash {

    // shorter command for executing linux shell commands
    public static void exec(String cmd) throws IOException {
        Runtime.getRuntime().exec(cmd);
    }

    public static void createFile(File file) throws IOException {
        exec("sudo touch " + file.getName());
    }

    public static void clearFile(File file) throws IOException {
        exec("sudo echo \"\" > " + file.getName());
    }

}
