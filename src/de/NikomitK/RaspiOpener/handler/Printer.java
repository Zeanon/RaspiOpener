package de.NikomitK.RaspiOpener.handler;

import lombok.experimental.UtilityClass;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// used for printing things into text files, why should i always use four lines of code when I can use one
@UtilityClass
public class Printer {

    public void printToFile(String text, String fileName, boolean append) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, append)));
        pw.println(text);
        pw.flush();
        pw.close();
    }

}
