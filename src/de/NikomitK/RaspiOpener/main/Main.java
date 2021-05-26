package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.Logger;
import de.NikomitK.RaspiOpener.handler.Storage;
import lombok.Getter;
import yapion.parser.YAPIONParser;
import yapion.serializing.YAPIONDeserializer;

import java.io.File;
import java.util.Arrays;

public class Main {

    public static Logger logger;

    @Getter
    private static File storageFile;

    public static Storage storage;

    @Getter
    private static boolean debug = false;

    @Getter
    private static boolean strackTrace = false;

    @Getter
    private static TCPServer server;

    public static void main(String[] args) throws Exception {
        logger = new Logger(new File("log.txt"));

        logger.log("Loading Storage");
        storageFile = new File("storage.yapion");
        if (storageFile.exists()) {
            storage = (Storage) YAPIONDeserializer.deserialize(YAPIONParser.parse(new File("storage.yapion")));
        } else {
            storage = new Storage();
        }
        logger.log("Loaded Storage");

        for (String s : args) {
            if (s.startsWith("-") && !s.startsWith("--") && s.length() > 2) {
                char[] chars = s.substring(1).toCharArray();
                for (char c : chars) {
                    arg("-" + c);
                }
            } else {
                arg(s);
            }
        }
        storage.save();

        logger.debug("CLI Args: " + Arrays.toString(args));

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

    private static void arg(String s) {
        switch (s) {
            case "-r":
            case "--reset":
                resetStorage();
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

    public static void resetStorage() {
        storage = new Storage();
        storage.save();
    }
}
