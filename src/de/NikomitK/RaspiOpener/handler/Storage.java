package de.NikomitK.RaspiOpener.handler;

import de.NikomitK.RaspiOpener.main.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import yapion.annotations.object.YAPIONData;
import yapion.exceptions.YAPIONException;
import yapion.hierarchy.output.FileOutput;
import yapion.serializing.YAPIONSerializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@YAPIONData
@Setter
@Getter
@ToString
public class Storage {
    private String key = null;
    private String hash = null;
    private String nonce = null;
    private Set<String> otps = new HashSet<>();

    // {
    //   @type(de.NikomitK.RaspiOpener.handler.Storage)
    //   key()
    //   hash()
    //   nonce()
    //   otps{
    //     @type(java.util.HashSet)
    //     values[...]
    //   }
    // }

    public void save() {
        try {
            Main.logger.debug("Saving Storage: " + YAPIONSerializer.serialize(this));
            YAPIONSerializer.serialize(this).toYAPION(new FileOutput(Main.getStorageFile(), true)).close();
        } catch (IOException | YAPIONException e) {
            Main.logger.warn(e);
        }
    }
}
