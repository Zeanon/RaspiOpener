package de.NikomitK.RaspiOpener.main;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


public class NewTCPServer {

    public NewTCPServer() {

       File firstFile = null;
        try {
            final BufferedReader reader = new BufferedReader(Channels.newReader((new RandomAccessFile(firstFile, "r")).getChannel(), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}