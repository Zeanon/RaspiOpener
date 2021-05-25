package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.Printer;

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

    public static File logFile;
    public static File debugLog;
    public static File keyPasStore;
    public static File otpStore;
    public static File nonceStore;

    public static void main(String[] args) throws Exception {
        logFile = new File("log.txt");
        debugLog = new File("debugLog.txt");
        keyPasStore = new File("keyPasStore.txt");
        otpStore = new File("otpStore.txt");
        nonceStore = new File("nonceStore.txt");
        boolean debug = false;
        if(args.length>0 && args[0].equals("-r")){
            if(keyPasStore.exists()) {
                keyPasStore.delete();
            }
        }
        else if(args.length>0 && args[0].equals("-debug")) {
            debug = true;
        }
//        gets removed in the next update, just here in case the replacement doesn't work reliably
        if(!keyPasStore.exists()) {
            keyPasStore.createNewFile();
        }
        if(!otpStore.exists()) {
            otpStore.createNewFile();
        }
        if(!nonceStore.exists()) {
            nonceStore.createNewFile();
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
