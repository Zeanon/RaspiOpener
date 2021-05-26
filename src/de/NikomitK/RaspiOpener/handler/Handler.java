package de.NikomitK.RaspiOpener.handler;

import de.NikomitK.RaspiOpener.main.Main;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class Handler {
    public String key;
    public String oriHash;
    public List<String> otps;

    public Handler(String pKey, String pHash, List<String> pOtps) {
        this.key = pKey;
        this.oriHash = pHash;
        this.otps = pOtps;
    }

    public Error storeKey(String pMsg) throws IOException {
        key = pMsg;
        Printer.printToFile(key, Main.getKeyPasStore().getName(), false);
        Main.logger.log("Key set to: " + key);
        if (key == null) {
            Main.logger.warn("Error #01 while setting key: null");
            return Error.KEY_UNSAVED;
        }
        return Error.OK;
    }

    public Error storePW(String pMsg) throws Exception {
        if (!oriHash.equals("")) {
            return Error.PASSWORD_EXISTS;
        }
        String enHash = null;
        String nonce = null;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enHash = pMsg.substring(0, i);
            }
        }
        oriHash = Decryption.decrypt(key, nonce, enHash);
        Printer.printToFile(oriHash, Main.getKeyPasStore().getName(), true);
        Main.logger.log("The password hash was set to: " + oriHash);
        return Error.OK;
    }

    public Error storeNonce(String pMsg) throws Exception {
        String enHash = null;
        String aesNonce = null;
        String oNonce;
        String trHash;
        int posNonce = -1;

        for (int i = 0; i < pMsg.length() - 1; i++) {
            if (pMsg.charAt(i) == ';') {
                aesNonce = pMsg.substring(i + 1);
                enHash = pMsg.substring(0, i);
            }
        }

        String deMsg = Decryption.decrypt(key, aesNonce, enHash);
        System.out.println(deMsg);

        for (int i = 0; i < deMsg.length(); i++) {
            if (deMsg.charAt(i) == ';') {
                posNonce = i;
                break;
            }
        }
        //for testing purposes because justin is kinda dumb
        // a few weeks later, I have no clue what the hell this was about, but I know it's still not fixed
        //oNonce = deMsg;
        oNonce = deMsg.substring(0, posNonce);
        System.out.println("oNonce: " + oNonce);
        trHash = deMsg.substring(posNonce + 1);

        if (oriHash.equals(trHash)) {
            try {
                Printer.printToFile(oNonce, Main.getNonceStore().getName(), false);
                Main.logger.debug("A new Nonce was set!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return Error.PASSWORD_MISMATCH;
        }

        return Error.OK;
    }

    public Error changePW(String pMsg) throws Exception {
        if (oriHash == null) {
            return Error.PASSWORD_NO_RESET;
        }

        String oldHash = oriHash;
        String nonce = null;
        String enHashes = null;
        String trHash = null;
        String neHash = null;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enHashes = pMsg.substring(0, i);
            }
        }

        String deHashes = Decryption.decrypt(key, nonce, enHashes);
        for (int i = 0; i < deHashes.length(); i++) {
            if (deHashes.charAt(i) == ';') {
                neHash = deHashes.substring(i + 1);
                trHash = deHashes.substring(0, i);
            }
        }

        if (oriHash.equals(trHash)) {
            oriHash = neHash;
            try {
                Printer.printToFile(key + "\n" + neHash, Main.getKeyPasStore().getName(), false);
                Main.logger.log("Password hash was changed to: " + neHash);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return Error.PASSWORD_MISMATCH;
        }

        if (oldHash.equals(oriHash)) {
            return Error.PASSWORD_NOT_SAVED;
        }

        return Error.OK;
    }

    public Error setOTP(String pMsg) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        int listLength = otps.size();
        int posOtp = -1;
        String nonce = null;
        String enMsg = null;
        String deMsg;
        String neOtp;
        String trHash;

        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enMsg = pMsg.substring(0, i);
            }
        }

        deMsg = Decryption.decrypt(key, nonce, enMsg);
        for (int i = 0; i < deMsg.length(); i++) {
            if (deMsg.charAt(i) == ';') {
                posOtp = i;
                break;
            }
        }

        neOtp = deMsg.substring(posOtp + 1);
        trHash = deMsg.substring(0, posOtp);
        if (oriHash.equals(trHash)) {
            try {
                Printer.printToFile(neOtp, Main.getOtpStore().getName(), true);
                otps.add(neOtp);
                Main.logger.log("A new OTP was set");
            } catch (FileNotFoundException fnfe) {
                Bash.createFile(Main.getOtpStore());
                Printer.printToFile(neOtp, Main.getOtpStore().getName(), true);
                otps.add(neOtp);
                Main.logger.log("A new OTP was set");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return Error.PASSWORD_MISMATCH;
        }

        if (listLength == otps.size()) {
            return Error.OPT_NOT_SAVED;
        }

        return Error.OK;
    }

    public Error einmalOeffnung(String pMsg) throws InterruptedException, IOException {
        String openTime = null;
        String trOtp = null;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                openTime = pMsg.substring(i + 1);
                trOtp = pMsg.substring(0, i);
            }
        }

        if (!otps.isEmpty()) {
            boolean isValid = false;
            int position = -1;
            for (int i = 0; i < otps.size(); i++) {
                if (otps.get(i).equals(trOtp)) {
                    isValid = true;
                    position = i;
                }
            }
            if (isValid) {
                System.out.println("Door is being opened with OTP...");
                GpioUtils.activate(Integer.parseInt(openTime));
                Main.logger.log("Door is being opened by OTP");
                System.out.println("OTPSTORE LÄNGE " + otps.size());
                otps.remove(position);
                System.out.println("OTPSTORE LÄNGE " + otps.size());
                try {
                    Bash.clearFile(Main.getOtpStore());
//                    somehow doesn't print otps into file, try with normal for loop
//                    for (String otp : otps) {
//                        Printer.printToFile(otp, "otpStore.txt", true);
//                        System.out.println(otp);
//                    }
                    for (String otp : otps) {
                        System.out.println(otp);
                        Printer.printToFile(otp, Main.getOtpStore().getName(), true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Client used a wrong OTP");
                Main.logger.log("A wrong OTP has been used");
                return Error.OTP_NOT_FOUND;
            }

        } else {
            System.out.println("There are currently no OTPs stored");
            Main.logger.log("There were no OTPs, but it was tried anyway");
            return Error.OTP_NOT_FOUND;
        }

        return Error.OK;
    }

    public Error open(String pMsg) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException, IOException {
        int posHash = -1;
        String nonce = null;
        String enMsg = null;
        String deMsg;

        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enMsg = pMsg.substring(0, i);
            }
        }

        deMsg = Decryption.decrypt(key, nonce, enMsg);
        for (int i = 0; i < deMsg.length(); i++) {
            if (deMsg.charAt(i) == ';') {
                posHash = i;
                break;
            }
        }

        if (oriHash.equals(deMsg.substring(0, posHash))) {
            System.out.println("Door is being opened...");
            GpioUtils.activate(Integer.parseInt(deMsg.substring(posHash + 1)));
            Main.logger.log("Door is being opened");
        } else {
            System.out.println("a wrong password was used");
            System.out.println(oriHash + " " + deMsg.substring(0, posHash));
            Main.logger.log("client used a wrong password");
            return Error.PASSWORD_MISMATCH;
            //toClient.println("Wrong password"); I think this is useless cause the app doesn't receive anything
        }

        return Error.OK;
    }

    public Error godeOpener(String pMsg) {
        int posSem = 0;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                posSem = i;
            }
        }

        if (oriHash.equals(pMsg.substring(0, posSem))) {
            System.out.println("Door is being opened...");
            Main.logger.log("Door is being opened");
            GpioUtils.activate(3000);
            Main.logger.log("Door was being opened");
        } else {
            System.out.println(pMsg.substring(posSem + 1));
            try {
                einmalOeffnung(pMsg.substring(posSem + 1) + ";3000");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return Error.OK;
    }

    public void reset(String pMsg) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        String nonce = null;
        String enMsg = null;
        String deMsg;

        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enMsg = pMsg.substring(0, i);
            }
        }

        deMsg = Decryption.decrypt(key, nonce, enMsg);
        if (oriHash.equals(deMsg)) {
            System.out.println("Pi is getting reset...\n");
            Main.logger.log("");
            Main.logger.log("");
            Main.logger.log("");
            Main.logger.log("The Pi was reset");
            key = "";
            oriHash = "";
            Bash.clearFile(Main.getKeyPasStore());
            Bash.clearFile(Main.getOtpStore());
        } else {
            System.out.println("a wrong password was used");
            Main.logger.log("client used a wrong password");
        }
    }
}