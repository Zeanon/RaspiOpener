package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.BashIn;
import de.NikomitK.RaspiOpener.handler.Printer;
import lombok.Getter;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static final StringWriter sw = new StringWriter();
    private static final PrintWriter pw = new PrintWriter(sw);
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
    private static TCPServer server;
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

        if(!keyPasStore.exists()) {
            BashIn.createFile(keyPasStore);
        }
        if(!otpStore.exists()) {
            BashIn.createFile(otpStore);
        }
        if(!nonceStore.exists()) {
            BashIn.createFile(nonceStore);
        }
        //new File("keyPasStore.txt").createNewFile();
        //new File("otpStore.txt").createNewFile();
        //new File("nonceStore.txt").createNewFile();
        try{
            System.out.println("Starting...");
            // TCP Server starten...
            TCPServer.run(logFile.getName(), debug);
        }
        catch(Exception e){
            e.printStackTrace(pw);
            System.out.println("Closing...?");
            //de.NikomitK.RaspiOpener.handler.Printer.printToFile(dateF.format(new Date()) + ": server crashed?" + sw.toString(), new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
            Printer.printToFile(dateF.format(new Date()) + ": server crashed? " + sw, "log.txt", true);
        }

    }
}
