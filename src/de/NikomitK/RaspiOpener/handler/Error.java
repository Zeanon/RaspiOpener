package de.NikomitK.RaspiOpener.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public enum Error {

    // Everything is OK
    OK,

    // Anything related with the key
    KEY_UNSAVED("01"),
    KEY_MISMATCH("03"),

    // Anything related with the password
    PASSWORD_EXISTS("02"),
    PASSWORD_MISMATCH("05"),
    PASSWORD_NOT_SAVED("06"),
    PASSWORD_NO_RESET("07"),

    // Anything related with the OneTimePad
    OTP_NOT_FOUND("04"),
    OPT_NOT_SAVED("08"),

    // Anything else
    SERVER_ERROR("09"),
    COMMAND_WRONG("10");

    private String errorCode = null;

}
