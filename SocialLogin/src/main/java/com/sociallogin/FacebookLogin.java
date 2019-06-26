package com.sociallogin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.sociallogin.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.sociallogin.users.SmartFacebookUser;
import com.sociallogin.util.Constants;
import com.sociallogin.util.SmartLoginException;
import com.sociallogin.util.UserUtil;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Copyright (c) 2016 Codelight Studios
 * Created by irshad on 25/09/16.
 */

public class FacebookLogin extends SmartLogin {

    private CallbackManager callbackManager;

    public FacebookLogin() {
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void twitter(SmartLoginConfig config) {

    }

    @Override
    public void facebook(final SmartLoginConfig config) {
        final Activity activity = config.getActivity();
        final SmartLoginCallbacks callback = config.getCallback();
        ProgressDialog progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
        progressDialog.setMessage(activity.getString(com.sociallogin.R.string.logging_holder));
        progressDialog.setCancelable(false);
        progressDialog.show();
        final ProgressDialog progress = progressDialog;
        ArrayList<String> permissions = config.getFacebookPermissions();
        if (permissions == null) {
            permissions = SmartLoginConfig.getDefaultFacebookPermissions();
        }
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                progress.setMessage(activity.getString(com.sociallogin.R.string.getting_data));
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse response) {
                        progress.dismiss();
                        SmartFacebookUser facebookUser = UserUtil.populateFacebookUser(jsonObject, loginResult.getAccessToken());
                        // Save the user
                        UserSessionManager.setUserSession(activity, facebookUser);
                        callback.onFacebookLoginSuccess(facebookUser);
//                        config.getActivity();
                    }
                });
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                progress.dismiss();
                Log.d("Facebook Login", "User cancelled the login process");
                callback.onLoginFailure(new SmartLoginException("User cancelled the login request", LoginType.Facebook));
            }

            @Override
            public void onError(FacebookException e) {
                progress.dismiss();
                callback.onLoginFailure(new SmartLoginException(e.getMessage(), e, LoginType.Facebook));
            }
        });
    }

    @Override
    public void google(SmartLoginConfig config) {

    }

    @Override
    public void linkdin(SmartLoginConfig config) {

    }


    @Override
    public boolean logout(Context context) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(Constants.USER_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            LoginManager.getInstance().logOut();
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
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
