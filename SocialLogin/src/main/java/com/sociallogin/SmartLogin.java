package com.sociallogin;

import android.content.Context;
import android.content.Intent;

/**
 * Copyright (c) 2017 Codelight Studios
 * Created by irshad on 22/04/17.
 */

public abstract class SmartLogin {

    public abstract void facebook(SmartLoginConfig config);

    public abstract void google(SmartLoginConfig config);

    public abstract void linkdin(SmartLoginConfig config);

    public abstract void twitter(SmartLoginConfig config);

    public abstract boolean logout(Context context);

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data, SmartLoginConfig config);

}
