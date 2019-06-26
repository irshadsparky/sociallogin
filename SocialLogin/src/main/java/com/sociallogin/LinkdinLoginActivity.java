package com.sociallogin;


import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.sociallogin.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sociallogin.users.SmartLinkdinUser;
import com.sociallogin.util.LinkedinScope;
import com.sociallogin.util.SmartLoginException;
import com.sociallogin.util.UserUtil;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class LinkdinLoginActivity extends Activity {

    /*CONSTANT FOR THE AUTHORIZATION PROCESS*/

    /****FILL THIS WITH YOUR INFORMATION*********/
    //This is the public api key of our application
//    private static final String API_KEY = "86yc030e8algad";
    //This is the private api key of our application
//    private static final String SECRET_KEY = "UAjhRyAI3BS4r7SD";
    //This is any string we want to use. This will be used for avoid CSRF attacks. You can generate one here: http://strongpasswordgenerator.com/
    private static final String STATE = "E3ZYKC1T6H2yP4z";
    //This is the url that LinkedIn Auth process will redirect to. We can put whatever we want that starts with http:// or https:// .
    //We use a made up url that we will intercept when redirecting. Avoid Uppercases.
//    private static final String REDIRECT_URI = "http://google.com";
    /*********************************************/

    //These are constants used for build the urls
    private static final String AUTHORIZATION_URL = "https://www.linkedin.com/uas/oauth2/authorization";
    private static final String ACCESS_TOKEN_URL = "https://www.linkedin.com/uas/oauth2/accessToken";
    private static final String SECRET_KEY_PARAM = "client_secret";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String GRANT_TYPE_PARAM = "grant_type";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String RESPONSE_TYPE_VALUE = "code";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String STATE_PARAM = "state";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    /*---------------------------------------*/
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUALS = "=";
    private static final String host = "api.linkedin.com";
    private static final String LINKDIN_PROFILE_DATA = "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))";
    private static final String LINKDIN_EMAIL_DATA = "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))";
    private static final String GET_DETAILS_URL = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,picture-url,public-profile-url,email-address)?format=json&oauth2_access_token=";
    private static final String topCardUrl = "https://" + host + "/v1/people/~:" +
            "(email-address,formatted-name,phone-numbers,public-profile-url,picture-url,picture-urls::(original))";
    static SmartLoginConfig config;
    private static boolean isProfile = false;
    private static boolean isEmail = false;
    private WebView webView;
    private List<LinkedinScope> linkedinScopes;
//    private ProgressDialog pd;

    /**
     * Method that generates the url for get the access token from the Service
     *
     * @return Url
     */
    private static String getAccessTokenUrl(String authorizationToken) {
        return ACCESS_TOKEN_URL
                + QUESTION_MARK
                + GRANT_TYPE_PARAM + EQUALS + GRANT_TYPE
                + AMPERSAND
                + RESPONSE_TYPE_VALUE + EQUALS + authorizationToken
                + AMPERSAND
                + CLIENT_ID_PARAM + EQUALS + config.getLinkdinApiKey()
                + AMPERSAND
                + REDIRECT_URI_PARAM + EQUALS + config.getLinkdinRedirectUrl()
                + AMPERSAND
                + SECRET_KEY_PARAM + EQUALS + config.getLinkdinSecretKey();
    }

    /**
     * Method that generates the url for get the authorization token from the Service
     *
     * @return Url
     */
    private static String getAuthorizationUrl() {
        String permissionCreater = "";
        if (isProfile && isEmail) {
            permissionCreater = "&scope=r_emailaddress%20r_liteprofile";
        } else if (isProfile) {
            permissionCreater = "&scope=r_liteprofile";
        } else if (isEmail) {
            permissionCreater = "&scope=r_emailaddress";
        }
        return AUTHORIZATION_URL
                + QUESTION_MARK + RESPONSE_TYPE_PARAM + EQUALS + RESPONSE_TYPE_VALUE
                + AMPERSAND + CLIENT_ID_PARAM + EQUALS + config.getLinkdinApiKey()
                + permissionCreater
                + AMPERSAND + STATE_PARAM + EQUALS + STATE
                + AMPERSAND + REDIRECT_URI_PARAM + EQUALS + config.getLinkdinRedirectUrl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        config = LinkdinLogin.config;
        if (config.getLinkedinScope() != null && config.getLinkedinScope().size() > 0) {
            linkedinScopes = config.getLinkedinScope();
            if (linkedinScopes.contains(LinkedinScope.TOKEN)) {
//                isTokenNeeded = true;
            }
            if (linkedinScopes.contains(LinkedinScope.BASE_PROFILE)) {
                isProfile = true;
            }
            if (linkedinScopes.contains(LinkedinScope.EMAIL)) {
                isEmail = true;
            }

        } else {
            config.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    config.getCallback().onLoginFailure(new SmartLoginException("Use LinkedinScope to pass required filled", LoginType.Linkdin));
                }
            });
            finish();
            return;
        }

        //get the webView from the layout
        webView = (WebView) findViewById(R.id.webView);

        //Request focus for the webview
        webView.requestFocus(View.FOCUS_DOWN);

        //Show a progress dialog to the user
//        pd = ProgressDialog.show(this, "", this.getString(R.string.loading), true);

        //Set a custom web view client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //This method will be executed each time a page finished loading.
                //The only we do is dismiss the progressDialog, in case we are showing any.
                /*if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }*/
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {
                //This method will be called when the Auth proccess redirect to our RedirectUri.
                //We will check the url looking for our RedirectUri.
                if (authorizationUrl.startsWith(config.getLinkdinRedirectUrl())) {
                    Log.i("Authorize", "");
                    Uri uri = Uri.parse(authorizationUrl);
                    //We take from the url the authorizationToken and the state token. We have to check that the state token returned by the Service is the same we sent.
                    //If not, that means the request may be a result of CSRF and must be rejected.
                    String stateToken = uri.getQueryParameter(STATE_PARAM);
                    if (stateToken == null || !stateToken.equals(STATE)) {
                        //Log.e("Authorize", "State token doesn't match");
                        return true;
                    }

                    //If the user doesn't allow authorization to our application, the authorizationToken Will be null.
                    String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
                    if (authorizationToken == null) {
                        Log.i("Authorize", "The user doesn't allow authorization.");
                        config.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                config.getCallback().onLoginFailure(new SmartLoginException("The user doesn't allow authorization", LoginType.Linkdin));
                            }
                        });
                        finish();
                        return true;
                    }
                    Log.i("Authorize", "Auth token received: " + authorizationToken);

                    //Generate URL for requesting Access Token
                    String accessTokenUrl = getAccessTokenUrl(authorizationToken);
                    //Log.e("accessTokenUrl", "shouldOverrideUrlLoading: " + accessTokenUrl);
                    //We make the request in a AsyncTask
                    new PostRequestAsyncTask().execute(accessTokenUrl);

                } else {
                    //Default behaviour
                    Log.i("Authorize", "Redirecting to: " + authorizationUrl);
                    webView.loadUrl(authorizationUrl);
                }
                return true;
            }
        });

        //Get the authorization Url
        String authUrl = getAuthorizationUrl();
        Log.i("Authorize", "Loading Auth Url: " + authUrl);
        //Load the authorization URL into the webView
        webView.loadUrl(authUrl);
    }


    ;

    private class PostRequestAsyncTask extends AsyncTask<String, Void, Boolean> {
        String accessToken;

        @Override
        protected void onPreExecute() {
//            pd = ProgressDialog.show(LinkdinLoginActivity.this, "", LinkdinLoginActivity.this.getString(R.string.loading), true);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            if (urls.length > 0) {
                String url = urls[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpost = new HttpPost(url);
                try {
                    HttpResponse response = httpClient.execute(httpost);
                    if (response != null) {
                        //If status is OK 200
                        if (response.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(response.getEntity());
                            //Convert the string result to a JSON Object
                            JSONObject resultJson = new JSONObject(result);
                            //Extract data from JSON Response
                            int expiresIn = resultJson.has("expires_in") ? resultJson.getInt("expires_in") : 0;
                            accessToken = resultJson.has("access_token") ? resultJson.getString("access_token") : null;

                            if (expiresIn > 0 && accessToken != null) {
                                Log.i("Authorize", "This is the access Token: " + accessToken + ". It will expires in " + expiresIn + " secs");

                                //Calculate date of expiration
                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.SECOND, expiresIn);
                                long expireDate = calendar.getTimeInMillis();

                                ////Store both expires in and access token in shared preferences
                                SharedPreferences preferences = LinkdinLoginActivity.this.getSharedPreferences("user_info", 0);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putLong("expires", expireDate);
                                editor.putString("accessToken", accessToken);
                                editor.commit();


                                return true;
                            }
                        }
                    }
                } catch (IOException e) {
                    //Log.e("Authorize", "Error Http response " + e.getLocalizedMessage());
                } catch (ParseException e) {
                    //Log.e("Authorize", "Error Parsing Http response " + e.getLocalizedMessage());
                } catch (JSONException e) {
                    //Log.e("Authorize", "Error Parsing Http response " + e.getLocalizedMessage());
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean status) {
          /*  if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }*/
            final SmartLoginCallbacks callback = config.getCallback();


            if (status) {
                if (isProfile && isEmail)
                    new LoadDataApiProfile(accessToken, callback).execute();
                else if (isProfile) {
                    new LoadDataApiProfile(accessToken, callback).execute();
                } else if (isEmail) {
                    SmartLinkdinUser smartLinkdinUser = new SmartLinkdinUser();
                    smartLinkdinUser.setToken(accessToken);
                    new LoadDataApiEmail(callback, smartLinkdinUser).execute();
                } else {
                    final SmartLinkdinUser smartLinkdinUser_ = new SmartLinkdinUser();
                    smartLinkdinUser_.setToken(accessToken);
                    config.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onLinkedinLoginSuccess(smartLinkdinUser_);
                        }
                    });
                    finish();
                }
            } else {
                config.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoginFailure(new SmartLoginException("Invalid Request", LoginType.Linkdin));
                    }
                });

            }
        }

    }

    private class LoadDataApiProfile extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        String token;
        SmartLoginCallbacks callback;
        JSONObject jsonObject;

        LoadDataApiProfile(String token, SmartLoginCallbacks callback) {
            this.token = token;
            this.callback = callback;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(config.getActivity());
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please Wait ..");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                //------------------>>
                URL url = new URL(LINKDIN_PROFILE_DATA);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
//                urlConnection.connect();


                /* Define InputStreams to read from the URLConnection. */
                if (urlConnection.getResponseCode() == 200) {
                    InputStream aInputStream = urlConnection.getInputStream();
                    BufferedInputStream aBufferedInputStream = new BufferedInputStream(
                            aInputStream);

                    /* Read bytes to the Buffer until there is nothing more to read(-1) */
                    ByteArrayBuffer aByteArrayBuffer = new ByteArrayBuffer(50);
                    int current = 0;
                    while ((current = aBufferedInputStream.read()) != -1) {
                        aByteArrayBuffer.append((byte) current);
                    }


                    /* Convert the Bytes read to a String. */
                    String aString = new String(aByteArrayBuffer.toByteArray());
                    Log.d("aString", "" + aString);
                    jsonObject = new JSONObject(aString);
                } else {
                    config.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onLoginFailure(new SmartLoginException("unAuthorized Access", LoginType.Linkdin));
                        }
                    });

                }


            } catch (final IOException e) {
                e.printStackTrace();
                config.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoginFailure(new SmartLoginException(e.getMessage(), LoginType.Linkdin));
                    }
                });


            } catch (final JSONException e) {
                e.printStackTrace();
                config.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoginFailure(new SmartLoginException(e.getMessage(), LoginType.Linkdin));
                    }
                });


            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            final SmartLinkdinUser smartLinkdinUser = UserUtil.populateLinkdinUser(jsonObject, token);
            if (smartLinkdinUser != null) {
                smartLinkdinUser.setToken(token);
                if (isEmail) {
                    new LoadDataApiEmail(callback, smartLinkdinUser).execute();
                } else {
                    config.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onLinkedinLoginSuccess(smartLinkdinUser);
                        }
                    });
                    finish();
                }
            } else {
                config.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoginFailure(new SmartLoginException("Invalid", LoginType.Linkdin));
                    }
                });

            }
            finish();
        }
    }

    private class LoadDataApiEmail extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        String token;
        SmartLoginCallbacks callback;
        JSONObject jsonObject;
        SmartLinkdinUser smartLinkdinUser;


        LoadDataApiEmail(SmartLoginCallbacks callback, SmartLinkdinUser smartLinkdinUser) {
            this.token = smartLinkdinUser.getToken();
            this.callback = callback;
            this.smartLinkdinUser = smartLinkdinUser;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(config.getActivity());
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please Wait ..");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                URL url = new URL(LINKDIN_EMAIL_DATA);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
//                urlConnection.connect();


                /* Define InputStreams to read from the URLConnection. */
                if (urlConnection.getResponseCode() == 200) {
                    InputStream aInputStream = urlConnection.getInputStream();
                    BufferedInputStream aBufferedInputStream = new BufferedInputStream(
                            aInputStream);

                    /* Read bytes to the Buffer until there is nothing more to read(-1) */
                    ByteArrayBuffer aByteArrayBuffer = new ByteArrayBuffer(50);
                    int current = 0;
                    while ((current = aBufferedInputStream.read()) != -1) {
                        aByteArrayBuffer.append((byte) current);
                    }


                    /* Convert the Bytes read to a String. */
                    String aString = new String(aByteArrayBuffer.toByteArray());
                    Log.d("aString", "" + aString);
                    jsonObject = new JSONObject(aString);
                } else {
                    config.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onLoginFailure(new SmartLoginException("unAuthorized Access", LoginType.Linkdin));
                        }
                    });

                }


            } catch (final IOException e) {
                config.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoginFailure(new SmartLoginException(e.getMessage(), LoginType.Linkdin));
                    }
                });

                e.printStackTrace();
            } catch (final JSONException e) {
                config.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoginFailure(new SmartLoginException(e.getMessage(), LoginType.Linkdin));
                    }
                });

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            final SmartLinkdinUser smartLinkdinUser_ = UserUtil.populateLinkdinUserEmail(jsonObject, smartLinkdinUser);
            if (smartLinkdinUser_ != null) {
                config.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLinkedinLoginSuccess(smartLinkdinUser_);
                    }
                });

            } else {
                config.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoginFailure(new SmartLoginException("Invalid", LoginType.Linkdin));
                    }
                });

            }
            finish();
        }
    }
}
