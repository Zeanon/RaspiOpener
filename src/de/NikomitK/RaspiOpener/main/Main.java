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
import java.util.HashSet;
import java.util.Set;

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

    public static void main(String[] args) {
        try {
            secondaryMain(args);
        } catch (Exception e) {
            System.exit(101);
        }
    }

    private static void secondaryMain(String[] args) throws Exception {
        Set<String> specifiedArguments = new HashSet<>();
        for (String s : args) {
            if (s.startsWith("-") && !s.startsWith("--") && s.length() > 2) {
                char[] chars = s.substring(1).toCharArray();
                for (char c : chars) {
                    specifiedArguments.add("- "+ c);
                }
            } else {
                specifiedArguments.add(s);
            }
        }

        if (specifiedArguments.contains("-h") || specifiedArguments.contains("--help")) {
            System.out.println("Help\n");
            System.out.println("-h or --help for this message");
            System.out.println("-d or --debug for debug logs");
            System.out.println("-s or --stacktrace for debug with stacktraces");
            System.out.println("-r or --reset for reseting it beforehand");
            return;
        }

        logger = new Logger(new File("log.txt"));
        debug = specifiedArguments.contains("-d") || specifiedArguments.contains("--debug");
        strackTrace = specifiedArguments.contains("-s") || specifiedArguments.contains("--stacktrace");
        logger.debug("Debug logging enabled");
        logger.debug("CLI Args: " + Arrays.toString(args));

        loadFileFromJar("dependencies.sh", true);
        loadFileFromJar("updater.sh", false);
        loadFileFromJar("bluetooth.sh", false);

        logger.log("Loading Storage");
        storageFile = new File("storage.yapion");
        if (storageFile.exists()) {
            storage = (Storage) YAPIONDeserializer.deserialize(YAPIONParser.parse(storageFile));
        } else {
            storage = new Storage();
        }
        logger.log("Loaded Storage");

        if (specifiedArguments.contains("-r") || specifiedArguments.contains("--reset")) {
            resetStorage();
        } else {
            storage.save();
        }

        for (String s : specifiedArguments) {
            if (s.startsWith("--updateRepo:")) {
                Updater.setRepo(s.substring(s.indexOf(':') + 1));
            }
        }
        Updater.UpdateResult updateResult = Updater.checkForUpdate();
        if (updateResult.getUpdateType() == Updater.UpdateType.UPDATE_AVAILABLE) {
            logger.log("New update found! " + updateResult.getUpdateVersion());
        }

        try {
            // TCP Server starten...
            logger.debug("Creating TCPServer");
            server = new TCPServer();
            logger.debug("Starting TCPServer");
            server.startServer();
        } catch (Exception e) {
            logger.warn("Server crashed?");
            logger.warn(e);
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
            logger.debug("File already exists");
            byte[] bytes1 = md5(Main.class.getResourceAsStream("/" + fileName));
            byte[] bytes2 = md5(new FileInputStream(file));
            if (Arrays.equals(bytes1, bytes2)) {
                logger.debug("File is the same");
                return;
            }
            logger.debug("Update File as it has changed or has been changed");
        }
        InputStream inputStream = Main.class.getResourceAsStream("/" + fileName);
        if (inputStream == null) {
            logger.debug("File in Jar not found");
            return;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        while (inputStream.available() > 0) {
            fileOutputStream.write(inputStream.read());
        }
        fileOutputStream.close();
        logger.debug(file.length() + " bytes loaded");

        if (exec) {
            logger.debug("Setting: " + fileName + " executable");
            file.setExecutable(true);
            logger.debug("Running: bash -c ./" + fileName);
            Runtime.getRuntime().exec("bash -c", new String[]{"./" + fileName});
        }
    }

    private static byte[] md5(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        if (inputStream == null) {
            return null;
        }
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
