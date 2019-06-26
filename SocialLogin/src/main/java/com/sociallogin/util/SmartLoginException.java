package com.sociallogin.util;

import com.sociallogin.LoginType;

/**
 * Copyright (c) 2017 Codelight Studios
 * Created by irshad on 22/04/17.
 */

public class SmartLoginException extends Exception {
    private LoginType loginType;

    public SmartLoginException(String message, LoginType loginType) {
        super(message);
        this.loginType = loginType;
    }

    public SmartLoginException(String message, Throwable cause, LoginType loginType) {
        super(message, cause);
        this.loginType = loginType;
    }

    public LoginType getLoginType() {
        return loginType;
    }
}
