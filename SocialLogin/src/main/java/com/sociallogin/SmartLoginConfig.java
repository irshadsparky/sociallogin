package com.sociallogin;

import android.app.Activity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.sociallogin.util.LinkedinScope;
import com.sociallogin.util.TwitterScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2016 Codelight Studios
 * Created by Kalyan on 9/9/2015.
 */
public class SmartLoginConfig {

    private String facebookAppId;
    private ArrayList<String> facebookPermissions;
    private GoogleApiClient googleApiClient;
    private Activity activity;
    private SmartLoginCallbacks callback;
    private String topCardUrl;
    private String client_id;
    private String linkdinApiKey;
    private String linkdinSecretKey;
    private String linkdinRedirectUrl;
    private String TwitterConsumerKey;
    private String TwitterConsumerSecrete;
    private LinkedinScope[] linkedinScope;
    private TwitterScope[] twitterScopes;
    private ProgressManager progressManager;

    public SmartLoginConfig(Activity activity, SmartLoginCallbacks callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public static ArrayList<String> getDefaultFacebookPermissions() {
        ArrayList<String> defaultPermissions = new ArrayList<>();
        defaultPermissions.add("public_profile");
        defaultPermissions.add("email");
        defaultPermissions.add("user_birthday");
        return defaultPermissions;
    }

    public List<TwitterScope> getTwitterScopes() {
        return Arrays.asList(twitterScopes);
    }

    public void setTwitterScopes(TwitterScope... twitterScopes) {
        this.twitterScopes = twitterScopes;
    }

    public List<LinkedinScope> getLinkedinScope() {
        return Arrays.asList(linkedinScope);
    }

    public void setLinkedinScope(LinkedinScope... linkedinScope) {
        this.linkedinScope = linkedinScope;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getLinkdinApiKey() {
        return linkdinApiKey;
    }

    public void setLinkdinApiKey(String linkdinApiKey) {
        this.linkdinApiKey = linkdinApiKey;
    }

    public String getLinkdinSecretKey() {
        return linkdinSecretKey;
    }

    public void setLinkdinSecretKey(String linkdinSecretKey) {
        this.linkdinSecretKey = linkdinSecretKey;
    }

    public String getLinkdinRedirectUrl() {
        return linkdinRedirectUrl;
    }

    public void setLinkdinRedirectUrl(String linkdinRedirectUrl) {
        this.linkdinRedirectUrl = linkdinRedirectUrl;
    }

    public String getDefaultTopCardUrl() {
        return "https://api.linkedin.com/v1/people/~:" +
                "(email-address,formatted-name,phone-numbers,public-profile-url,picture-url,picture-urls::(original))";
    }

    public String getTopCardUrl() {
        return topCardUrl;
    }

    public void setTopCardUrl(String topCardUrl) {
        this.topCardUrl = topCardUrl;
    }

    public Activity getActivity() {
        return activity;
    }

    public SmartLoginCallbacks getCallback() {
        return callback;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId) {
        this.facebookAppId = facebookAppId;
    }

    public ArrayList<String> getFacebookPermissions() {
        return facebookPermissions;
    }

    public void setFacebookPermissions(ArrayList<String> facebookPermissions) {
        this.facebookPermissions = facebookPermissions;
    }

    public String getTwitterConsumerKey() {
        return TwitterConsumerKey;
    }

    public void setTwitterConsumerKey(String twitterConsumerKey) {
        TwitterConsumerKey = twitterConsumerKey;
    }

    public String getTwitterConsumerSecrete() {
        return TwitterConsumerSecrete;
    }

    public void setTwitterConsumerSecrete(String twitterConsumerSecrete) {
        TwitterConsumerSecrete = twitterConsumerSecrete;
    }

    public ProgressManager getProgressManager() {
        return progressManager;
    }

    public void setProgressManager(ProgressManager progressManager) {
        this.progressManager = progressManager;
    }

    public interface ProgressManager {
        void onStartProgress();

        void onEndProgress();
    }
}
