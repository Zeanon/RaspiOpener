package de.NikomitK.RaspiOpener.handler;

import lombok.Getter;
import lombok.ToString;
import yapion.annotations.object.YAPIONData;

import java.util.ArrayList;
import java.util.List;

@YAPIONData
@Getter
@ToString
public class Storage {

    private String key;
    private String hash;
    private String nonce;
    private List<String> otps = new ArrayList<>();

    public Storage(String key, String hash, String nonce) {
        this.key = key;
        this.hash = hash;
        this.nonce = nonce;
    }
}
