package de.NikomitK.RaspiOpener.handler;

import de.NikomitK.RaspiOpener.main.Updater;
import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class GpioUtils {

    private Thread current = null;

    public void activate(long time) {
        if (current != null) {
            return;
        }
        current = new Thread(() -> {
            try {
                Updater.process(Runtime.getRuntime().exec("./gpio.sh", new String[]{time + ""}));
            } catch (IOException e) {
                e.printStackTrace();
            }
            current = null;
        });
        current.start();
    }
}