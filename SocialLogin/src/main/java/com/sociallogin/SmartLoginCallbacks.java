package com.sociallogin;

import com.sociallogin.users.SmartFacebookUser;
import com.sociallogin.users.SmartGoogleUser;
import com.sociallogin.users.SmartLinkdinUser;
import com.sociallogin.users.SmartTwitterUser;
import com.sociallogin.util.SmartLoginException;

/**
 * Copyright (c) 2017 Codelight Studios
 * Created by irshad on 22/04/17.
 */

public interface SmartLoginCallbacks {


    void onFacebookLoginSuccess(SmartFacebookUser user);

    void onGoogleLoginSuccess(SmartGoogleUser user);

    void onLinkedinLoginSuccess(SmartLinkdinUser user);

    void onTwitterLoginSuccess(SmartTwitterUser user);

    void onLoginFailure(SmartLoginException e);

}
