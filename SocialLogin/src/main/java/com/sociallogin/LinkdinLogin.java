package com.sociallogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.sociallogin.util.Log;

import com.sociallogin.util.Constants;
import com.sociallogin.util.SmartLoginException;

public class LinkdinLogin extends SmartLogin {
    public static SmartLoginConfig config;


    @Override
    public void facebook(final SmartLoginConfig config) {

    }

    @Override
    public void google(SmartLoginConfig config) {

    }

    @Override
    public void twitter(SmartLoginConfig config) {

    }

    @Override
    public void linkdin(final SmartLoginConfig config) {
        this.config = config;
        if (config.getLinkdinApiKey() != null) {
            if (config.getLinkdinSecretKey() != null) {
                if (config.getLinkdinRedirectUrl() != null) {
                    config.getActivity().startActivity(new Intent(config.getActivity(), LinkdinLoginActivity.class));
                } else {
                    config.getCallback().onLoginFailure(new SmartLoginException("Add LinkdinRedirectUrl", LoginType.Linkdin));
                }
            } else {
                config.getCallback().onLoginFailure(new SmartLoginException("Add LinkdinSecretKey", LoginType.Linkdin));
            }
        } else {
            config.getCallback().onLoginFailure(new SmartLoginException("Add LinkdinApiKey", LoginType.Linkdin));
        }

    }

    @Override
    public boolean logout(Context context) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(Constants.USER_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(Constants.USER_TYPE);
            editor.remove(Constants.USER_SESSION);
            editor.apply();
            return true;
        } catch (Exception e) {
            Log.e("FacebookLogin", e.getMessage());
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, SmartLoginConfig config) {

    }
}
