package com.sociallogin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.sociallogin.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sociallogin.users.SmartGoogleUser;
import com.sociallogin.util.Constants;
import com.sociallogin.util.SmartLoginException;
import com.sociallogin.util.UserUtil;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2016 Codelight Studios
 * Created by irshad on 26/09/16.
 */

public class GoogleLogin extends SmartLogin {
    private GoogleApiClient apiClient;

    @Override
    public void facebook(SmartLoginConfig config) {

    }

    @Override
    public void google(SmartLoginConfig config) {
        apiClient = config.getGoogleApiClient();
        Activity activity = config.getActivity();

        if (apiClient == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(config.getClient_id())
                    .requestEmail()
                    .requestProfile()
                    .build();

            apiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
            config.setGoogleApiClient(apiClient);
        }

        ProgressDialog progress = ProgressDialog.show(activity, "", activity.getString(com.sociallogin.R.string.logging_holder), true);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        activity.startActivityForResult(signInIntent, Constants.GOOGLE_LOGIN_REQUEST);
        progress.dismiss();
    }

    @Override
    public void linkdin(SmartLoginConfig config) {

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
            Log.e("GoogleLogin", e.getMessage());
            return false;
        }
    }

    @Override
    public void twitter(SmartLoginConfig config) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, final SmartLoginConfig config) {
//        final ProgressDialog progress = ProgressDialog.show(config.getActivity(), "", config.getActivity().getString(com.sociallogin.R.string.getting_data), true);
        if (config.getProgressManager() != null)
            config.getProgressManager().onStartProgress();
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//        vollyRequestSimpleLoginClass.setVolleyRequestResponce(config.getCallback());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();

            final SmartGoogleUser googleUser = UserUtil.populateGoogleUser(acct);
            UserSessionManager.setUserSession(config.getActivity(), googleUser);
            pools(new CallBack() {
                @Override
                public void OnThreadCalling() {
                    String token = "";
                    try {
                        token = GoogleAuthUtil.getToken(config.getActivity().getApplicationContext(), acct.getAccount(), "oauth2:https://www.googleapis.com/auth/plus.login");
                        Log.e("TAG", "onActivityResult: " + token);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GoogleAuthException e) {
                        e.printStackTrace();
                    }

                    final String finalToken = token;
                    config.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleUser.setToken(finalToken);
                            config.getCallback().onGoogleLoginSuccess(googleUser);
//                            progress.dismiss();
                            if (config.getProgressManager() != null)
                                config.getProgressManager().onEndProgress();
                            if (config.getGoogleApiClient() != null)
                                logout(config.getGoogleApiClient());
                        }
                    });
                }
            });

/*
            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String token = null;

                    try {
                        token = GoogleAuthUtil.getToken(config.getActivity().getApplicationContext(), acct.getAccount(), "oauth2:https://www.googleapis.com/auth/plus.login");
                        Log.e("TAG", "onActivityResult: " + token);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GoogleAuthException e) {
                        e.printStackTrace();
                    }
                    return token;
                }

                @Override
                protected void onPostExecute(String token) {
                    Log.i("tag", "Access token retrieved:" + token);
                    googleUser.setToken(token);
                    config.getCallback().onGoogleLoginSuccess(googleUser);
                    progress.dismiss();
                    if (config.getGoogleApiClient() != null)
                        logout(config.getGoogleApiClient());
                }

            };
*/
//            task.execute();

            // Save the user


        } else {
//            vollyRequestSimpleLoginClass.getVolleyRequestResponce().onLoginFailure(new SmartLoginException("requestCode-->"+requestCode,LoginType.Google));
            Log.d("GOOGLE SIGN IN", "" + requestCode);
            // Signed out, show unauthenticated UI.
            if (config.getProgressManager() != null)
                config.getProgressManager().onEndProgress();
            config.getCallback().onLoginFailure(new SmartLoginException("Google login failed", LoginType.Google));
        }
    }

    private void logout(final GoogleApiClient mGoogleApiClient) {
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                android.util.Log.e("logout", "onResult: " );
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        });
    }
    public static void pools(final CallBack callBack) {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();

        ExecutorService executorService = new ThreadPoolExecutor(NUMBER_OF_CORES,
                NUMBER_OF_CORES * 2,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                taskQueue,
                new BackgroundThreadFactory());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                callBack.OnThreadCalling();
            }
        });

    }
    public interface CallBack {
        void OnThreadCalling();
    }
    private static class BackgroundThreadFactory implements ThreadFactory {
        private static int sTag = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("CustomThread" + sTag);
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            // A exception handler is created to log the exception from threads
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Log.e("CustomThread" + sTag, thread.getName() + " encountered an error: " + ex.getMessage());
                }
            });
            return thread;
        }
    }

}
