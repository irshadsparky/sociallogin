package studios.codelight.smartlogin;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import com.sociallogin.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sociallogin.LoginType;
import com.sociallogin.SmartLogin;
import com.sociallogin.SmartLoginCallbacks;
import com.sociallogin.SmartLoginConfig;
import com.sociallogin.SmartLoginFactory;
import com.sociallogin.UserSessionManager;
import com.sociallogin.users.SmartFacebookUser;
import com.sociallogin.users.SmartGoogleUser;
import com.sociallogin.users.SmartLinkdinUser;
import com.sociallogin.users.SmartTwitterUser;
import com.sociallogin.users.SmartUser;
import com.sociallogin.util.LinkedinScope;
import com.sociallogin.util.SmartLoginException;
import com.sociallogin.util.TwitterScope;


public class TestLibraryActivity extends AppCompatActivity implements SmartLoginCallbacks {

    SmartUser currentUser;
    //GoogleApiClient mGoogleApiClient;
    SmartLoginConfig config;
    SmartLogin smartLogin;
    private Button facebookLoginButton, googleLoginButton, customSigninButton, customSignupButton, logoutButton, linkdin_login_button, twitter_login_button;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        generateHashkey();
        bindViews();
        setListeners();

        config = new SmartLoginConfig(this, this);
        //facebook
        config.setFacebookAppId(getString(R.string.facebook_app_id));
        config.setFacebookPermissions(null);
        //google
//        config.setGoogleApiClient(null);
        // TODO: 19-03-2019 For Required token use setClient_id
        config.setClient_id("69330385257-2rsavptvq95t5gfeas8ru1jjq52em4tf.apps.googleusercontent.com");//https://developers.google.com/identity/sign-in/android/start credentials.json
        //linkdin
        config.setLinkedinScope(LinkedinScope.TOKEN, LinkedinScope.BASE_PROFILE, LinkedinScope.EMAIL);//LinkedinScope.BASE_PROFILE,LinkedinScope.EMAIL
        config.setLinkdinApiKey("86yc030e8algad");
        config.setLinkdinSecretKey("UAjhRyAI3BS4r7SD");
        config.setLinkdinRedirectUrl("http://google.com");
//twitter
        config.setTwitterScopes(TwitterScope.TOKEN, TwitterScope.BASE_PROFILE, TwitterScope.EMAIL);
        config.setTwitterConsumerKey("PwwALzoprvft0yLNFDLkUqYrU ");
        config.setTwitterConsumerSecrete("DQ8PhN2JkUBvLWb6YjiOlPShsDl3mJtHU0tsTQD9Krs9yByjjS");


    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = UserSessionManager.getCurrentUser(this);
        refreshLayout();
    }

    private void refreshLayout() {
        currentUser = UserSessionManager.getCurrentUser(this);
        if (currentUser != null) {
            Log.d("Smart Login", "Logged in user: " + currentUser.toString());
            facebookLoginButton.setVisibility(View.GONE);
            googleLoginButton.setVisibility(View.GONE);
            customSigninButton.setVisibility(View.GONE);
            linkdin_login_button.setVisibility(View.GONE);
            customSignupButton.setVisibility(View.GONE);
            emailEditText.setVisibility(View.GONE);
            passwordEditText.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            facebookLoginButton.setVisibility(View.VISIBLE);
            googleLoginButton.setVisibility(View.VISIBLE);
            customSigninButton.setVisibility(View.VISIBLE);
            linkdin_login_button.setVisibility(View.VISIBLE);
            customSignupButton.setVisibility(View.VISIBLE);
            emailEditText.setVisibility(View.VISIBLE);
            passwordEditText.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (smartLogin != null) {
            smartLogin.onActivityResult(requestCode, resultCode, data, config);
        }
    }

    private void setListeners() {
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                smartLogin = SmartLoginFactory.build(LoginType.Facebook);
                smartLogin.facebook(config);
            }
        });

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smartLogin = SmartLoginFactory.build(LoginType.Google);
                smartLogin.google(config);
            }
        });
        linkdin_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smartLogin = SmartLoginFactory.build(LoginType.Linkdin);
                smartLogin.linkdin(config);
            }
        });
        twitter_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smartLogin = SmartLoginFactory.build(LoginType.TWITTER);
                smartLogin.twitter(config);
            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    if (currentUser instanceof SmartFacebookUser) {
                        smartLogin = SmartLoginFactory.build(LoginType.Facebook);
                    } else if (currentUser instanceof SmartGoogleUser) {
                        smartLogin = SmartLoginFactory.build(LoginType.Google);
                    } else if (currentUser instanceof SmartLinkdinUser) {
                        smartLogin = SmartLoginFactory.build(LoginType.Linkdin);
                    }
                    boolean result = smartLogin.logout(TestLibraryActivity.this);
                    if (result) {
                        refreshLayout();
                        Toast.makeText(TestLibraryActivity.this, "User logged out successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void bindViews() {
        facebookLoginButton = (Button) findViewById(R.id.facebook_login_button);
        googleLoginButton = (Button) findViewById(R.id.google_login_button);
        customSigninButton = (Button) findViewById(R.id.custom_signin_button);
        customSignupButton = (Button) findViewById(R.id.custom_signup_button);
        linkdin_login_button = (Button) findViewById(R.id.linkdin_login_button);
        twitter_login_button = (Button) findViewById(R.id.twitter_login_button);
        emailEditText = (EditText) findViewById(R.id.email_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);
        logoutButton = (Button) findViewById(R.id.logout_button);
    }


    @Override
    public void onFacebookLoginSuccess(SmartFacebookUser user) {

        Toast.makeText(this, user.getProfileName(), Toast.LENGTH_SHORT).show();
        refreshLayout();

    }

    @Override
    public void onGoogleLoginSuccess(SmartGoogleUser user) {
        Log.e("1", "onGoogleLoginSuccess: " + user.getToken());
        Log.e("2", "onGoogleLoginSuccess: " + user.getIdToken());
        refreshLayout();

    }

    @Override
    public void onLinkedinLoginSuccess(SmartLinkdinUser user) {
        Log.e("1", "onLinkedinLoginSuccess: " + user.getId());
        Log.e("2", "onLinkedinLoginSuccess: " + user.getFirstName());
        Log.e("3", "onLinkedinLoginSuccess: " + user.getLastName());
        Log.e("4", "onLinkedinLoginSuccess: " + user.getPhotoUrl());
        Log.e("5", "onLinkedinLoginSuccess: " + user.getEmail());
        Log.e("6", "onLinkedinLoginSuccess: " + user.getToken());
    }

    @Override
    public void onTwitterLoginSuccess(SmartTwitterUser user) {
        Log.e("1", "onTwitterLoginSuccess: " + user.getId());
        Log.e("2", "onTwitterLoginSuccess: " + user.getUsername());
        Log.e("3", "onTwitterLoginSuccess: " + user.getScreenName());
        Log.e("4", "onTwitterLoginSuccess: " + user.getPhotoUrl());
        Log.e("5", "onTwitterLoginSuccess: " + user.getEmail());
        Log.e("6", "onTwitterLoginSuccess: " + user.getToken());
    }

    @Override
    public void onLoginFailure(SmartLoginException e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public void generateHashkey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "studios.codelight.smartlogin",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("error", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.d("error", e.getMessage());
        }
    }
}
