package de.NikomitK.RaspiOpener.handler;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public enum Error {

    OK,
    KEY_UNSAVED("01"),
    PASSWORD_EXISTS("02"),
    KEY_MISMATCH("03"),
    OTP_NOT_FOUND("04"),
    PASSWORD_MISMATCH("05"),
    PASSWORD_NOT_SAVED("06"),
    PASSWORD_NO_RESET("07"),
    OPT_NOT_SAVED("08"),
    SERVER_ERROR("09"),
    COMMAND_WRONG("10");

    private String errorCode = null;

}
