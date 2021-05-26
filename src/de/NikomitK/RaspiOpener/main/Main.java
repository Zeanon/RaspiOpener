package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.Logger;
import de.NikomitK.RaspiOpener.handler.Storage;
import lombok.Getter;
import yapion.parser.YAPIONParser;
import yapion.serializing.YAPIONDeserializer;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

        loadFileFromJar("dependencies.sh", true);
        loadFileFromJar("bluetooth.sh", false);

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
        logger.debug("Resetting Storage");
        storage = new Storage();
        storage.save();
    }

    private static void loadFileFromJar(String fileName, boolean exec) throws IOException, NoSuchAlgorithmException {
        File file = new File(fileName);
        if (file.exists()) {
            byte[] bytes1 = md5(Main.class.getResourceAsStream("/" + fileName));
            byte[] bytes2 = md5(new FileInputStream(file));
            if (Arrays.equals(bytes1, bytes2)) {
                logger.log("File already exists");
                return;
            }
            logger.log("Update File as it has changed or has been changed");
        }
        InputStream inputStream = Main.class.getResourceAsStream("/" + fileName);
        if (inputStream == null) {
            logger.warn("File in Jar not found");
            System.exit(1);
        }
        inputStream.transferTo(new FileOutputStream(file));
        logger.debug(file.length() + " bytes loaded");

        if (exec) {
            logger.debug("Setting: " + fileName + " executable");
            file.setExecutable(true);
            logger.debug("Running: bash -c ./" + fileName);
            Runtime.getRuntime().exec("bash -c", new String[]{"./" + fileName});
        }
    }

    private static byte[] md5(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        while (inputStream.available() > 0) {
            int i = inputStream.read();
            if (i == -1) {
                break;
            }
            messageDigest.update((byte) i);
        }
        return messageDigest.digest();
    }
}
