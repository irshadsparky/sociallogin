package com.sociallogin;

/**
 * Copyright (c) 2017 Codelight Studios
 * Created by irshad on 22/04/17.
 */

public class SmartLoginFactory {
    public static SmartLogin build(LoginType loginType) {
        switch (loginType) {
            case Facebook:
                return new FacebookLogin();
            case Google:
                return new GoogleLogin();
            case Linkdin:
                return new LinkdinLogin();
            case TWITTER:
                return new TwitterLogin();
            default:
                // To avoid null pointers
                return null;
        }
    }
}
