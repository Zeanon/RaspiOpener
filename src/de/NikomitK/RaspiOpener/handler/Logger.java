package de.NikomitK.RaspiOpener.handler;

import de.NikomitK.RaspiOpener.main.Main;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Logger {

    private final BufferedOutputStream outputStream;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Logger(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        outputStream = new BufferedOutputStream(new FileOutputStream(file, true));
    }

    public void log(String message) {
        try {
            internalLog(dateFormat.format(new Date()) + " [LOG  ] " + message + "\n");
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debug(String message) {
        if (!Main.isDebug()) {
            return;
        }

        try {
            internalLog(dateFormat.format(new Date()) + " [DEBUG] " + message + "\n");
            outputStream.flush();
            if (!Main.isStrackTrace()) {
                return;
            }
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                internalLog("\t" + stackTraceElement.toString() + "\n");
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void warn(String message) {
        try {
            internalLog(dateFormat.format(new Date()) + " [WARN ] " + message + "\n");
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void warn(Exception exception) {
        try {
            internalLog(dateFormat.format(new Date()) + " [WARN ] " + exception.toString() + "\n");

            Set<Throwable> dejaVu = new HashSet<>();
            dejaVu.add(exception);

            internalPrint(dejaVu, exception.getStackTrace(), exception.getSuppressed(), exception.getCause());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void internalPrint(Set<Throwable> dejaVu, StackTraceElement[] stackTrace, Throwable[] suppressed, Throwable cause) throws IOException {
        for (StackTraceElement stackTraceElement : stackTrace) {
            internalLog("\tat " + stackTraceElement + "\n");
        }

        for (Throwable throwable : suppressed) {
            internalWarn(throwable, dejaVu, "Suppressed", "\t");
        }

        if (cause != null) {
            internalWarn(cause, dejaVu, "Caused by", "");
        }
    }

    private void internalWarn(Throwable throwable, Set<Throwable> dejaVu, String suppressedPrefix, String prefix) throws IOException {
        if (dejaVu.contains(throwable)) {
            return;
        }

        dejaVu.add(throwable);
        internalLog(prefix + dateFormat.format(new Date()) + " [WARN ] [" + suppressedPrefix + "] " + throwable.toString() + "\n");

        internalPrint(dejaVu, throwable.getStackTrace(), throwable.getSuppressed(), throwable.getCause());
    }

    private void internalLog(String message) throws IOException {
        outputStream.write(message.getBytes(StandardCharsets.UTF_8));
        System.out.print(message);
    }
}