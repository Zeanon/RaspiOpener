package de.NikomitK.RaspiOpener.handler;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import yapion.annotations.object.YAPIONData;

import java.util.ArrayList;
import java.util.List;

@YAPIONData
@Setter
@Getter
@ToString
public class Storage {
    private String key = "";
    private String hash = "";
    private String nonce = "";
    private List<String> otps = new ArrayList<>();
}
