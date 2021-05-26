package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.Bash;
import de.NikomitK.RaspiOpener.handler.Logger;
import lombok.Getter;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Main {
    private static final DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

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
                default:
                    System.out.println("Unknown arg: " + s);
            }
        }

        if (!keyPasStore.exists()) {
            Bash.createFile(keyPasStore);
        }
        if (!otpStore.exists()) {
            Bash.createFile(otpStore);
        }
        if (!nonceStore.exists()) {
            Bash.createFile(nonceStore);
        }

        try {
            System.out.println("Starting...");
            // TCP Server starten...
            server = new TCPServer();
            server.startServer();
        } catch (Exception e) {
            System.out.println("Closing...?");
            //de.NikomitK.RaspiOpener.handler.Printer.printToFile(dateF.format(new Date()) + ": server crashed?" + sw.toString(), new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
            logger.warn("Server crashed?");
            logger.warn(e);
        }

    }
}
