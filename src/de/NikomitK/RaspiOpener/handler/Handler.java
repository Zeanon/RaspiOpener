package de.NikomitK.RaspiOpener.handler;

import de.NikomitK.RaspiOpener.main.Main;
import de.NikomitK.RaspiOpener.main.Updater;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Handler {

    public Error storeKey(String key) {
        Main.storage.setKey(key);
        Main.logger.log("Key set to: " + key);
        if (key == null) {
            Main.logger.warn("Error #01 while setting key: null");
            return Error.KEY_UNSAVED;
        }
        return Error.OK;
    }

    public Error storePW(String parameter) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (Main.storage.getHash() != null && !Main.storage.getHash().equals("")) {
            return Error.PASSWORD_EXISTS;
        }
        int index = parameter.indexOf(';');
        String hash = Decryption.decrypt(Main.storage.getKey(), parameter.substring(index + 1), parameter.substring(0, index));
        Main.storage.setHash(hash);
        Main.logger.log("The password hash was set to: " + hash);
        return Error.OK;
    }

    public Error storeNonce(String parameter) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int index = parameter.indexOf(';');
        String nonce = parameter.substring(index + 1);

        String decryptedNonce = Decryption.decrypt(Main.storage.getKey(), nonce, parameter.substring(0, index));
        Main.logger.debug("DecryptedNonce: " + decryptedNonce);

        int posNonce = decryptedNonce.indexOf(';');
        nonce = decryptedNonce.substring(0, posNonce);
        String hash = decryptedNonce.substring(posNonce + 1);
        Main.logger.debug("Nonce: " + nonce);

        if (!hash.equals(Main.storage.getHash())) {
            return Error.PASSWORD_MISMATCH;
        }

        Main.storage.setNonce(nonce);
        Main.logger.debug("A new Nonce was set!");
        return Error.OK;
    }

    public Error changePW(String parameter) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (Main.storage.getHash() == null) {
            return Error.PASSWORD_NO_RESET;
        }

        int index = parameter.indexOf(';');
        String nonce = parameter.substring(index + 1);
        String encryptedHash = parameter.substring(0, index);

        String decryptedHash = Decryption.decrypt(Main.storage.getKey(), nonce, encryptedHash);

        index = encryptedHash.indexOf(';');
        String transferredHash = decryptedHash.substring(index + 1);
        String newHash = decryptedHash.substring(0, index);

        String oldHash = Main.storage.getHash();
        if (!transferredHash.equals(oldHash)) {
            return Error.PASSWORD_MISMATCH;
        }

        if (newHash.equals(oldHash)) {
            Main.logger.log("Password hash was not changed");
            return Error.PASSWORD_NOT_SAVED;
        }

        Main.storage.setHash(newHash);
        Main.logger.log("Password hash was changed to: " + newHash);
        return Error.OK;
    }

    public Error setOTP(String parameter) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int index = parameter.indexOf(';');
        String nonce = parameter.substring(index + 1);
        String encryptedOTP = parameter.substring(0, index);
        String decryptedOTP = Decryption.decrypt(Main.storage.getKey(), nonce, encryptedOTP);

        index = decryptedOTP.indexOf(';');
        String otp = decryptedOTP.substring(index + 1);
        String transferredHash = decryptedOTP.substring(0, index);

        if (!transferredHash.equals(Main.storage.getHash())) {
            return Error.PASSWORD_MISMATCH;
        }

        Main.logger.log("A new OTP was set");
        if (!Main.storage.getOtps().add(otp)) {
            return Error.OTP_NOT_SAVED;
        }
        return Error.OK;
    }

    public Error einmalOeffnung(String parameter) {
        int index = parameter.indexOf(';');
        String openTime = parameter.substring(index + 1);
        String transferredOtp = parameter.substring(0, index);

        if (Main.storage.getOtps().isEmpty()) {
            Main.logger.log("There were no OTPs, but it was tried anyway");
            return Error.OTP_NOT_FOUND;
        }

        boolean isValid = Main.storage.getOtps().remove(transferredOtp);
        if (!isValid) {
            Main.logger.log("A wrong OTP has been used");
            return Error.OTP_NOT_FOUND;
        }

        Main.logger.log("Door is being opened by OTP");
        GpioUtils.activate(Integer.parseInt(openTime));
        return Error.OK;
    }

    public Error open(String parameter) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int index = parameter.indexOf(';');
        String nonce = parameter.substring(index + 1);
        String encryptedHash = parameter.substring(0, index);

        String decrpytedHash = Decryption.decrypt(Main.storage.getKey(), nonce, encryptedHash);
        index = decrpytedHash.indexOf(';');
        String hash = decrpytedHash.substring(0, index);
        int time = Integer.parseInt(decrpytedHash.substring(index + 1));

        if (!hash.equals(Main.storage.getHash())) {
            Main.logger.log("Client used a wrong password");
            Main.logger.debug(Main.storage.getHash() + " " + hash);
            return Error.PASSWORD_MISMATCH;
        }

        Main.logger.log("Door is being opened");
        GpioUtils.activate(time);
        return Error.OK;
    }

    public Error godeOpener(String parameter) {
        int index = parameter.indexOf(';');
        String hash = parameter.substring(0, index);

        if (!hash.equals(Main.storage.getHash())) {
            return einmalOeffnung(hash + ";3000");
        }
        Main.logger.log("Door is being opened");
        GpioUtils.activate(3000);
        Main.logger.log("Door was being opened");

        return Error.OK;
    }

    public void reset(String parameter) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int index = parameter.indexOf(';');
        String nonce = parameter.substring(index + 1);
        String encryptedHash = parameter.substring(0, index);

        String decrpytedHash = Decryption.decrypt(Main.storage.getKey(), nonce, encryptedHash);
        if (!decrpytedHash.equals(Main.storage.getHash())) {
            Main.logger.log("Client used a wrong password");
            return;
        }

        Main.logger.log("The Pi was reset");
        Main.resetStorage();
    }

    public Updater.UpdateResult checkForUpdate(String parameter) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int index = parameter.indexOf(';');
        String nonce = parameter.substring(index + 1);
        String encryptedHash = parameter.substring(0, index);

        String decrpytedHash = Decryption.decrypt(Main.storage.getKey(), nonce, encryptedHash);
        if (!decrpytedHash.equals(Main.storage.getHash())) {
            Main.logger.log("Client used a wrong password");
            return new Updater.UpdateResult(Updater.UpdateType.INVALID);
        }
        return Updater.checkForUpdate();
    }

    public Error update(String parameter) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int index = parameter.indexOf(';');
        String nonce = parameter.substring(index + 1);
        String encryptedHash = parameter.substring(0, index);

        String decrpytedHash = Decryption.decrypt(Main.storage.getKey(), nonce, encryptedHash);
        if (!decrpytedHash.equals(Main.storage.getHash())) {
            Main.logger.log("Client used a wrong password");
            return Error.PASSWORD_MISMATCH;
        }
        if (Updater.checkForUpdate().getUpdateType() != Updater.UpdateType.UPDATE_AVAILABLE) {
            Main.logger.log("No update found");
            return Error.NO_UPDATE;
        }
        Main.logger.log("Updating now!");
        Updater.update();
        return Error.OK;
    }
}