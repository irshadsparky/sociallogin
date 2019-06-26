package com.sociallogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.sociallogin.util.Log;

import com.facebook.login.LoginManager;
import com.sociallogin.users.SmartTwitterUser;
import com.sociallogin.util.Constants;
import com.sociallogin.util.SmartLoginException;
import com.sociallogin.util.TwitterScope;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit2.Call;

public class TwitterLogin extends SmartLogin {
    boolean isTokenNeeded = false;
    boolean isProfile = false;
    boolean isEmail = false;
    private TwitterAuthClient client;
    private SmartLoginConfig config;

    @Override
    public void facebook(SmartLoginConfig config) {

    }

    @Override

    public void twitter(final SmartLoginConfig config) {
        this.config = config;
        List<TwitterScope> twitterScopes = config.getTwitterScopes();
        if (twitterScopes == null || twitterScopes.size() < 1) {
            config.getCallback().onLoginFailure(new SmartLoginException("Please Add Twitter Scopes Liken config.setTwitterScopes(TwitterScope.TOKEN);", LoginType.TWITTER));
            return;
        }
        final SmartTwitterUser smartTwitterUser = new SmartTwitterUser();
        if (config.getTwitterConsumerKey() == null || config.getTwitterConsumerSecrete() == null) {
            config.getCallback().onLoginFailure(new SmartLoginException("Twitter Consumer Key OR Twitter Consumer Secrete required", LoginType.TWITTER));
            return;
        }
        TwitterConfig config_ = new TwitterConfig.Builder(config.getActivity())
                .logger(new DefaultLogger(Log.DEBUG))//enable logging when app is in debug mode
                .twitterAuthConfig(new TwitterAuthConfig(config.getTwitterConsumerKey(), config.getTwitterConsumerSecrete()))//pass the created app Consumer KEY and Secret also called API Key and Secret
                .debug(true)//enable debug mode
                .build();

        //finally initialize twitter with created configs
        Twitter.initialize(config_);
        client = new TwitterAuthClient();

        if (twitterScopes.contains(TwitterScope.TOKEN)) {
            isTokenNeeded = true;
        }
        if (twitterScopes.contains(TwitterScope.BASE_PROFILE)) {
            isProfile = true;
        }
        if (twitterScopes.contains(TwitterScope.EMAIL)) {
            isEmail = true;
        }


        if (getTwitterSession() == null) {
            client.authorize(config.getActivity(), new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    TwitterSession twitterSession = result.data;
                    if(twitterSession.getAuthToken().token!=null)
                        smartTwitterUser.setToken(twitterSession.getAuthToken().token);
                    if (isProfile && isEmail) {
                        fetchTwitterEmail(twitterSession, smartTwitterUser);
                    } else if (isProfile) {
                        fetchTwitterImage(smartTwitterUser);
                    } else if (isEmail) {
                        fetchTwitterEmail(twitterSession, smartTwitterUser);
                    } else {
                        config.getCallback().onTwitterLoginSuccess(smartTwitterUser);
                        //success
                    }


                }

                @Override
                public void failure(TwitterException e) {
                    config.getCallback().onLoginFailure(new SmartLoginException("Failed to authenticate. Please try again.", LoginType.TWITTER));
                }
            });
        } else {
            smartTwitterUser.setToken(getTwitterSession().getAuthToken().token);
            if (isProfile && isEmail) {
                fetchTwitterEmail(getTwitterSession(), smartTwitterUser);
            } else if (isProfile) {
                fetchTwitterImage(smartTwitterUser);
            } else if (isEmail) {
                fetchTwitterEmail(getTwitterSession(), smartTwitterUser);
            } else {

                config.getCallback().onTwitterLoginSuccess(smartTwitterUser);
                //success
            }
        }

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
            Log.e("Twitter", e.getMessage());
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, SmartLoginConfig config) {
        if (client != null)
            client.onActivityResult(requestCode, resultCode, data);
    }


    //--------------------------------------------------------------------



    public void fetchTwitterEmail(final TwitterSession twitterSession, final SmartTwitterUser smartTwitterUser) {
        client.requestEmail(twitterSession, new Callback<String>() {
            @Override
            public void success(Result<String> result) {
                smartTwitterUser.setEmail(result.data);
                smartTwitterUser.setId(String.valueOf(twitterSession.getUserId()));
                smartTwitterUser.setUsername(twitterSession.getUserName());
                if (isProfile && isEmail) {
                    fetchTwitterImage(smartTwitterUser);
                } else {
                    config.getCallback().onTwitterLoginSuccess(smartTwitterUser);
                }

                //success
            }

            @Override
            public void failure(TwitterException exception) {
                config.getCallback().onLoginFailure(new SmartLoginException("Failed to authenticate. Please try again.", LoginType.TWITTER));
            }
        });
    }


    public void fetchTwitterImage(final SmartTwitterUser smartTwitterUser) {
        //check if user is already authenticated or not
        if (getTwitterSession() != null) {

            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();

            Call<User> call = twitterApiClient.getAccountService().verifyCredentials(true, false, true);
            call.enqueue(new Callback<User>() {
                @Override
                public void success(Result<User> result) {
                    User user = result.data;
                    smartTwitterUser.setId(String.valueOf(user.id));
                    smartTwitterUser.setUsername(user.name);
                    if (user.email != null && user.email.length() > 0)
                        smartTwitterUser.setEmail(user.email);
                    smartTwitterUser.setScreenName(user.screenName);

                    String imageProfileUrl = user.profileImageUrl;
                    imageProfileUrl = imageProfileUrl.replace("_normal", "");
                    smartTwitterUser.setPhotoUrl(imageProfileUrl);
                    config.getCallback().onTwitterLoginSuccess(smartTwitterUser);
                    //success

                }

                @Override
                public void failure(TwitterException exception) {
                    config.getCallback().onLoginFailure(new SmartLoginException("Failed to authenticate. Please try again.", LoginType.TWITTER));
                }
            });
        } else {
            config.getCallback().onLoginFailure(new SmartLoginException("First to Twitter auth to Verify Credentials.", LoginType.TWITTER));
        }

    }

    private TwitterSession getTwitterSession() {
        return TwitterCore.getInstance().getSessionManager().getActiveSession();
    }


}
