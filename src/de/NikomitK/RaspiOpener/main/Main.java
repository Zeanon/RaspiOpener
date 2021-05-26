package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.Bash;
import de.NikomitK.RaspiOpener.handler.Logger;
import lombok.Getter;

import java.io.File;
import java.util.Arrays;

public class Main {
    public static Logger logger;

    @Getter
    private static File keyPasStore;
    @Getter
    private static File otpStore;
    @Getter
    private static File nonceStore;

    @Getter
    private static boolean debug = false;

    @Getter
    private static boolean strackTrace = false;

    @Getter
    private static TCPServer server;

    public static void main(String[] args) throws Exception {
        keyPasStore = new File("keyPasStore.txt");
        otpStore = new File("otpStore.txt");
        nonceStore = new File("nonceStore.txt");

        logger = new Logger(new File("log.txt"));
        for (String s : args) {
            switch (s) {
                case "-r":
                case "--reset":
                    if (keyPasStore.exists()) {
                        keyPasStore.delete();
                    }
                    break;
                case "-d":
                case "--debug":
                    debug = true;
                    break;
                case "-s":
                case "--stacktrace":
                case "--stackTrace":
                    strackTrace = true;
                    break;
                default:
                    System.out.println("Unknown arg: " + s);
            }
        }

        logger.debug("CLI Args: " + Arrays.toString(args));

        if (!keyPasStore.exists()) {
            logger.debug("Creating: " + keyPasStore.getAbsolutePath());
            Bash.createFile(keyPasStore);
        }
        if (!otpStore.exists()) {
            logger.debug("Creating: " + otpStore.getAbsolutePath());
            Bash.createFile(otpStore);
        }
        if (!nonceStore.exists()) {
            logger.debug("Creating: " + nonceStore.getAbsolutePath());
            Bash.createFile(nonceStore);
        }

        try {
            System.out.println("Starting...");
            // TCP Server starten...
            server = new TCPServer();
            server.startServer();
        } catch (Exception e) {
            System.out.println("Closing...?");
            logger.warn("Server crashed?");
            logger.warn(e);
        }

    }
}
