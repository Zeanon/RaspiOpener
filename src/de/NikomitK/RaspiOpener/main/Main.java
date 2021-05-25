package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.Bash;
import de.NikomitK.RaspiOpener.handler.Printer;
import lombok.Getter;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    private static final DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Getter
    private static File logFile;
    @Getter
    private static File debugLog;
    @Getter
    private static File keyPasStore;
    @Getter
    private static File otpStore;
    @Getter
    private static File nonceStore;

    @Getter
    private static boolean debug = false;

    public static void main(String[] args) throws Exception {
        logFile = new File("log.txt");
        debugLog = new File("debugLog.txt");
        keyPasStore = new File("keyPasStore.txt");
        otpStore = new File("otpStore.txt");
        nonceStore = new File("nonceStore.txt");

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
            TCPServer.run(logFile.getName(), debug);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.out.println("Closing...?");
            //de.NikomitK.RaspiOpener.handler.Printer.printToFile(dateF.format(new Date()) + ": server crashed?" + sw.toString(), new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
            Printer.printToFile(dateF.format(new Date()) + ": server crashed? " + sw, "log.txt", true);
        }

    }
}
