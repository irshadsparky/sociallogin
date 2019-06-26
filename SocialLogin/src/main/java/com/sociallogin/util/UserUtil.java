package com.sociallogin.util;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.sociallogin.users.SmartFacebookUser;
import com.sociallogin.users.SmartGoogleUser;
import com.sociallogin.users.SmartLinkdinUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Copyright (c) 2016 Codelight Studios
 * Created by Kalyan on 10/3/2015.
 */
public class UserUtil {

    public static SmartGoogleUser populateGoogleUser(GoogleSignInAccount account) {
        //Create a new google user
        SmartGoogleUser googleUser = new SmartGoogleUser();
        //populate the user
        googleUser.setDisplayName(account.getDisplayName());
        if (account.getPhotoUrl() != null) {
            googleUser.setPhotoUrl(account.getPhotoUrl().toString());
        }
        googleUser.setEmail(account.getEmail());
        googleUser.setIdToken(account.getIdToken());
        googleUser.setUserId(account.getId());
        if (account.getAccount() != null) {
            googleUser.setUsername(account.getAccount().name);
        }
        //return the populated google user
        return googleUser;
    }

    public static SmartLinkdinUser populateLinkdinUser(JSONObject jsonObject, String token) {
        if (jsonObject != null && jsonObject.length() > 0) {
            //firstName,id,lastName,publicProfileUrl
            SmartLinkdinUser smartLinkdinUser = new SmartLinkdinUser();
            try {
                if (jsonObject.has("firstName")) {
                    JSONObject firstName = jsonObject.getJSONObject("firstName");
                    if (firstName.has("localized")) {
                        JSONObject localized = firstName.getJSONObject("localized");
                        Iterator<String> iter = localized.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            try {
                                Object value = localized.get(key);
                                smartLinkdinUser.setFirstName(String.valueOf(value));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                // Something went wrong!
                            }
                        }
                    }


                }
                if (jsonObject.has("lastName")) {
                    JSONObject lastName = jsonObject.getJSONObject("lastName");
                    if (lastName.has("localized")) {
                        JSONObject localized = lastName.getJSONObject("localized");
                        Iterator<String> iter = localized.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            try {
                                Object value = localized.get(key);
                                smartLinkdinUser.setLastName(String.valueOf(value));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                // Something went wrong!
                            }
                        }
                    }


                }
                if (jsonObject.has("id")) {
                    smartLinkdinUser.setId(jsonObject.getString("id"));
                }

                if (jsonObject.has("profilePicture")) {
                    JSONObject profilePicture = jsonObject.getJSONObject("profilePicture");
                    if (profilePicture.has("displayImage~")) {
                        JSONObject displayImage = profilePicture.getJSONObject("displayImage~");
                        if (displayImage.has("elements")) {
                            JSONArray elements = displayImage.getJSONArray("elements");
                            if (elements.length() > 0) {
                                JSONObject jsonObject1 = elements.getJSONObject(0);
                                if (jsonObject1.has("identifiers")) {
                                    JSONArray identifiers = jsonObject1.getJSONArray("identifiers");
                                    if (identifiers.length() > 0) {
                                        JSONObject jsonObject2 = identifiers.getJSONObject(0);
                                        if (jsonObject2.has("identifier")) {
                                            String identifier = jsonObject2.getString("identifier");
                                            smartLinkdinUser.setPhotoUrl(identifier);
                                        }


                                    }
                                }
                            }
                        }

                    }

                }


                smartLinkdinUser.setToken(token);
                return smartLinkdinUser;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
    }

    public static SmartLinkdinUser populateLinkdinUserEmail(JSONObject jsonObject, SmartLinkdinUser smartLinkdinUser) {
        if (jsonObject != null && jsonObject.length() > 0) {
            try {
                if (jsonObject.has("elements")) {
                    JSONArray elements = jsonObject.getJSONArray("elements");
                    if (elements.length() > 0) {
                        JSONObject jsonObject1 = elements.getJSONObject(0);
                        if (jsonObject1.has("handle~")) {
                            JSONObject handle = jsonObject1.getJSONObject("handle~");
                            if (handle.has("emailAddress")) {
                                String emailAddress = handle.getString("emailAddress");
                                smartLinkdinUser.setEmail(emailAddress);
                            }
                        }
                    }
                }

                return smartLinkdinUser;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
    }

    public static SmartFacebookUser populateFacebookUser(JSONObject object,AccessToken accessToken) {
        SmartFacebookUser facebookUser = new SmartFacebookUser();
        facebookUser.setGender(-1);
        facebookUser.setAccessToken(accessToken);
        try {
            if (object.has(Constants.FacebookFields.EMAIL))
                facebookUser.setEmail(object.getString(Constants.FacebookFields.EMAIL));
            if (object.has(Constants.FacebookFields.BIRTHDAY))
                facebookUser.setBirthday(object.getString(Constants.FacebookFields.BIRTHDAY));
            if (object.has(Constants.FacebookFields.GENDER)) {
                try {
                    Constants.Gender gender = Constants.Gender.valueOf(object.getString(Constants.FacebookFields.GENDER));
                    switch (gender) {
                        case male:
                            facebookUser.setGender(0);
                            break;
                        case female:
                            facebookUser.setGender(1);
                            break;
                    }
                } catch (Exception e) {
                    //if gender is not in the enum it is already set to unspecified value (-1)
                    //Log.e("UserUtil", e.getMessage());
                }
            }
            if (object.has(Constants.FacebookFields.LINK))
                facebookUser.setProfileLink(object.getString(Constants.FacebookFields.LINK));
            if (object.has(Constants.FacebookFields.ID))
                facebookUser.setUserId(object.getString(Constants.FacebookFields.ID));
            if (object.has(Constants.FacebookFields.NAME))
                facebookUser.setProfileName(object.getString(Constants.FacebookFields.NAME));
            if (object.has(Constants.FacebookFields.FIRST_NAME))
                facebookUser.setFirstName(object.getString(Constants.FacebookFields.FIRST_NAME));
            if (object.has(Constants.FacebookFields.MIDDLE_NAME))
                facebookUser.setMiddleName(object.getString(Constants.FacebookFields.MIDDLE_NAME));
            if (object.has(Constants.FacebookFields.LAST_NAME))
                facebookUser.setLastName(object.getString(Constants.FacebookFields.LAST_NAME));
        } catch (JSONException e) {
            //Log.e("UserUtil", e.getMessage());
            facebookUser = null;
        }
        return facebookUser;
    }

//git push -f origin master
}
