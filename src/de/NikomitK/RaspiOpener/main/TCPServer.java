package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.Error;
import de.NikomitK.RaspiOpener.handler.Handler;

import javax.crypto.AEADBadTagException;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {

    private static final DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private String key;
    private String oriHash;
    private List<String> otps = new ArrayList<>();

    private final boolean secured = false;
    private Handler handler;
    private ServerSocket ss;
    private boolean fsu = true;
    private Thread socketHandler;

    public TCPServer() {
        try (BufferedReader keyPasReader = new BufferedReader(Channels.newReader((new RandomAccessFile(Main.getKeyPasStore(), "r")).getChannel(), StandardCharsets.UTF_8)); BufferedReader otpReader = new BufferedReader(Channels.newReader((new RandomAccessFile(Main.getOtpStore(), "r")).getChannel(), StandardCharsets.UTF_8))) {
            Main.logger.debug("Reading: " + Main.getKeyPasStore().getAbsolutePath());
            key = keyPasReader.readLine();
            oriHash = keyPasReader.readLine();
            otpReader.lines().forEach(s -> otps.add(s));

            Main.logger.debug("Creating new Handler");
            handler = new Handler(key, oriHash, otps);
        } catch (IOException e) {
            e.printStackTrace();
            Main.logger.warn(e);
            System.exit(1);
        }
    }

    public void startServer() {
        try {
            Main.logger.debug("Starting ServerSocket");
            ss = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
            Main.logger.warn(e);
            System.exit(1);
            return;
        }
        System.out.println("TCP-Server waiting for client on port 5000");
        Main.logger.log("TCP-Server waiting for client on port 5000");
        socketHandler = new Thread(() -> {
            while (!ss.isClosed()) {
                try {
                    Socket connected = ss.accept();
                    connected.setSoTimeout(3000);

                    System.out.println("Client at " + " " + connected.getInetAddress() + ":" + connected.getPort() + " connected ");
                    handleClient(connected);
                } catch (IOException e) {
                    e.printStackTrace();
                    Main.logger.warn(e);
                    try {
                        ss.close();
                    } catch (IOException ioException) {
                        Main.logger.warn(ioException);
                        // If the Code gets here: WTF!?!?!?
                    }
                }
            }
        }, "TCP-Server-handler");
        Main.logger.debug("Starting TCP-Server-handler Thread");
        socketHandler.start();
    }

    private void handleClient(Socket connected) {
        boolean processError = false;
        try {
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(connected.getInputStream()));
            PrintWriter toClient = new PrintWriter(connected.getOutputStream(), true);
            if (fsu) {
                toClient.println("BUT NOT FOR ME");
                fsu = false;
            }
            toClient.println("Connected");
            // receive from app
            String clientCommand = fromClient.readLine();

            System.out.println("Received: " + clientCommand);
            Main.logger.debug("Received: " + clientCommand);

            if (clientCommand.isEmpty()) {
                Main.logger.debug("Command was Empty");
                toClient.println("Invalid connection\n");
                connected.close();
                return;
            }

            if (clientCommand.charAt(1) != ':' || clientCommand.equals("null")) {
                Main.logger.debug("Command was Invalid");
                toClient.println("Invalid connection\n");
                connected.close();
                return;
            }
            if (clientCommand.charAt(0) == 'H' && !connected.getInetAddress().equals(InetAddress.getLocalHost())) {
                Main.logger.debug("Invalid Command from client");
                toClient.println("Invalid connection\n");
                connected.close();
                return;
            }
            Main.logger.log("Client at: " + connected.getInetAddress() + " sent " + clientCommand.charAt(0) + " command");

            processError = true;
            Error error = processFromClient(clientCommand, toClient);
            Main.logger.debug("Error Code " + error + " " + error.getErrorCode() + " from executing command '" + clientCommand + "'");
            if (error != Error.OK) {
                Main.logger.debug("Sending Client EOS as of ErrorCode not equal OK");
                toClient.println(error.getErrorCode() + "EOS");
            }
            connected.close();
        } catch (Exception e) {
            e.printStackTrace();
            Main.logger.warn(e);
            try {
                if (!connected.isClosed()) {
                    if (processError) {
                        connected.getOutputStream().write((Error.SERVER_ERROR.getErrorCode() + "EOS").getBytes(StandardCharsets.UTF_8));
                    } else {
                        connected.getOutputStream().write("Invalid connection\n".getBytes(StandardCharsets.UTF_8));
                    }
                    connected.getOutputStream().flush();
                    connected.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Main.logger.warn(ex);
            }
        }
    }

    private Error processFromClient(String fromclient, PrintWriter toClient) {
        String param = fromclient.substring(2);

        Error worked = Error.OK;
        try {
            switch (fromclient.charAt(0)) {
                case 'n': //storeNonce in progress
                    // Command syntax: "n:(<nonce>;<hash>);nonce
                    worked = handler.storeNonce(param);
                    break;

                case 'k': //storeKey done
                    // Command syntax: "k:<key>"
                    if (((key == null || key.equals("")) && param.length() == 32) && secured)
                        worked = handler.storeKey(param);
                    key = handler.key;
                    break;

                case 'p': //storePW done
                    // Command syntax: "p:(<hash>);<nonce>"
                    if (((oriHash == null || oriHash.equals("")) && secured))
                        worked = handler.storePW(param);
                    oriHash = handler.oriHash;
                    break;

                case 'c': //changePW done
                    // Command syntax: "c:(<oldHash>;<newHash>);<nonce>"
                    worked = handler.changePW(param);
                    break;

                case 's': // setOTP done
                    // Command syntax "s:(<hash>;<otp>);<nonce>"
                    worked = handler.setOTP(param);
                    otps = handler.otps;
                    break;

                case 'e': // einmalöffnung done
                    // Command syntax: "e:<otp>;<time>"
                    worked = handler.einmalOeffnung(param);
                    otps = handler.otps;
                    break;

                case 'o': // Open  done
                    // Command syntax: "o:(<hash>;<time>);<nonce>"
                    worked = handler.open(param);
                    break;
                case 'g': // godeOpener?
                    // command syntax: "g:<hash>"
                    worked = handler.godeOpener(param);
                    break;
                case 'r': //reset
                    // Command syntax: "r:(<hash>);<nonce>"
                    handler.reset(param);
                    key = handler.key;
                    oriHash = handler.oriHash;
                    break;

                case 'H': // "how are you", get's called from alivekeeper, never from user
                    toClient.println("I'm fine, thanks");
                    break;

                default:
                    return Error.COMMAND_WRONG;
            }
        } catch (AEADBadTagException bte) {
            bte.printStackTrace();
            worked = Error.KEY_MISMATCH;
        } catch (Exception exc) {
            throw new SecurityException("Something went wrong", exc);
        }

        return worked;
    }
}