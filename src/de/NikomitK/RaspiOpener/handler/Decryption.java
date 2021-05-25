package de.NikomitK.RaspiOpener.handler;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.*;
import lombok.experimental.UtilityClass;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class Decryption {
    private final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private final int TAG_LENGTH_BIT = 128;

    private final Charset UTF_8 = StandardCharsets.UTF_8;

    public String decrypt(String key, String nonce, String msg) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] encodedKey = DatatypeConverter.parseHexBinary(key);
        byte[] byteNonce = DatatypeConverter.parseHexBinary(nonce);
        byte[] encryptedText = DatatypeConverter.parseHexBinary(msg);

        SecretKey secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, byteNonce));
        return new String(cipher.doFinal(encryptedText), UTF_8);
    }
}

