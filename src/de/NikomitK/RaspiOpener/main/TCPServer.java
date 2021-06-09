package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.Error;
import de.NikomitK.RaspiOpener.handler.Handler;

import javax.crypto.AEADBadTagException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPServer {

    private Handler handler;
    private ServerSocket ss;
    private boolean fsu = true;
    private Thread socketHandler;

    public TCPServer() {
        Main.logger.debug("Creating new Handler");
        handler = new Handler();
    }

    public void startServer() {
        try {
            Main.logger.debug("Starting ServerSocket");
            ss = new ServerSocket(5000);
        } catch (IOException e) {
            Main.logger.warn(e);
            System.exit(1);
            return;
        }
        Main.logger.log("TCP-Server waiting for client on port 5000");
        socketHandler = new Thread(() -> {
            while (!ss.isClosed()) {
                try {
                    Socket connected = ss.accept();
                    connected.setSoTimeout(3000);

                    Main.logger.debug("Client at " + " " + connected.getInetAddress() + ":" + connected.getPort() + " connected ");
                    handleClient(connected);
                } catch (IOException e) {
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
                Main.logger.debug("Sending FSU handshake for initial connect");
                toClient.println("BUT NOT FOR ME");
                fsu = false;
            }
            String clientCommand = fromClient.readLine();
            Main.logger.debug("Received: " + clientCommand);

            if (clientCommand == null || clientCommand.isEmpty()) {
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
            Main.logger.log("Client at: " + connected.getInetAddress() + " sent " + clientCommand.charAt(0) + " command");

            processError = true;
            Error error = processFromClient(clientCommand, toClient);
            Main.storage.save();
            Main.logger.debug("Error Code " + error + " from executing command '" + clientCommand + "'");
            if (error != Error.OK) {
                Main.logger.debug("Sending Client EOS as of ErrorCode not equal OK");
                toClient.println(error.getErrorCode() + "EOS");
            }
            Main.logger.debug("Closing Client connection");
            connected.close();
        } catch (Exception e) {
            Main.logger.warn(e);
            try {
                if (!connected.isClosed()) {
                    if (processError) {
                        Main.logger.debug("Sending ServerError as disconnect did not work");
                        connected.getOutputStream().write((Error.SERVER_ERROR.getErrorCode() + "EOS").getBytes(StandardCharsets.UTF_8));
                    } else {
                        Main.logger.debug("Sending invalid connection");
                        connected.getOutputStream().write("Invalid connection\n".getBytes(StandardCharsets.UTF_8));
                    }
                    connected.getOutputStream().flush();
                    Main.logger.debug("Closing connection");
                    connected.close();
                }
            } catch (IOException ex) {
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
                    // Command syntax: "n:(<nonce>;<hash>);<nonce>"
                    worked = handler.storeNonce(param);
                    break;

                case 'k': //storeKey done
                    // Command syntax: "k:<key>"
                    if ((Main.storage.getKey() == null || Main.storage.getKey().equals("")) && param.length() == 32) {
                        worked = handler.storeKey(param);
                    }
                    break;

                case 'p': //storePW done
                    // Command syntax: "p:(<hash>);<nonce>"
                    if ((Main.storage.getHash() == null || Main.storage.getHash().equals(""))) {
                        worked = handler.storePW(param);
                    }
                    break;

                case 'c': //changePW done
                    // Command syntax: "c:(<oldHash>;<newHash>);<nonce>"
                    worked = handler.changePW(param);
                    break;

                case 's': // setOTP done
                    // Command syntax "s:(<hash>;<otp>);<nonce>"
                    worked = handler.setOTP(param);
                    break;

                case 'e': // einmalöffnung done
                    // Command syntax: "e:<otp>;<time>"
                    worked = handler.einmalOeffnung(param);
                    break;

                case 'o': // Open  done
                    // Command syntax: "o:(<hash>;<time>);<nonce>"
                    try {
                        worked = handler.open(param);
                    } catch (NumberFormatException e) {
                        worked = Error.SERVER_ERROR;
                    }
                    break;
                case 'g': // godeOpener?
                    // command syntax: "g:<hash>"
                    worked = handler.godeOpener(param);
                    break;
                case 'r': //reset
                    // Command syntax: "r:(<hash>);<nonce>"
                    handler.reset(param);
                    break;

                case 'v':
                    // command syntax: "v:(<hash>);<nonce>"
                    StringBuilder st = new StringBuilder();
                    st.append(Updater.getCurrentVersion().getValue("version", "").get()).append(" ");
                    st.append("(").append(Updater.getCurrentVersion().getValue("build", 0).get()).append(")");
                    Updater.UpdateResult updateResult = handler.checkForUpdate(param);
                    if (updateResult.getUpdateType() == Updater.UpdateType.UPDATE_AVAILABLE) {
                        st.append(";");
                        st.append(updateResult.getUpdateVersion());
                        if (updateResult.getUpdateDescription() != null) {
                            st.append(";");
                            st.append(updateResult.getUpdateDescription());
                        }
                    }
                    st.append("EOS");
                    Main.logger.log("Version return: " + st);
                    toClient.println(st.toString());
                    break;
                case 'u':
                    // command syntax: "u:(<hash>);<nonce>"
                    // command syntax: "u:(<offset>;<hash>);<nonce>"
                    worked = handler.update(param);
                    break;

                default:
                    return Error.COMMAND_WRONG;
            }
        } catch (AEADBadTagException bte) {
            Main.logger.warn(bte);
            worked = Error.KEY_MISMATCH;
        } catch (Exception exc) {
            throw new SecurityException("Something went wrong", exc);
        }

        return worked;
    }
}