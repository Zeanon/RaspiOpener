package de.NikomitK.RaspiOpener.handler;

import com.pi4j.io.gpio.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GpioUtils {

    private final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "MyLed", PinState.LOW);

    // gets called from de.NikomitK.RaspiOpener.handler.Handler, if the App sends a command to activate GPIO pin 25
    public void activate(long time) {
        // sets the previously specified pin from 0V to 3.3V and back
        // TODO: check if problems with screen and update
        pin.high();
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pin.low();
    }
}