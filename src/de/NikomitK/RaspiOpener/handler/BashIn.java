package de.NikomitK.RaspiOpener.handler;

import java.io.IOException;

public class BashIn {

    // shorter command for executing linux shell commands
    public static void exec(String cmd) throws IOException {
        Runtime.getRuntime().exec(cmd);
    }

    public static void clearFile(String file) throws IOException {
        exec("sudo truncate -s 0 " + file);
    }

}
