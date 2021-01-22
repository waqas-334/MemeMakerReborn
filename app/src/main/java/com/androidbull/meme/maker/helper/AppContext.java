package com.androidbull.meme.maker.helper;

import android.annotation.SuppressLint;
import android.content.Context;

public class AppContext {



    @SuppressLint("StaticFieldLeak")
    private static AppContext mInstance;
    private Context context;

    public static AppContext getInstance() {
        if (mInstance == null) mInstance = getSync();
        return mInstance;
    }

    private static synchronized AppContext getSync() {
        if (mInstance == null) mInstance = new AppContext();
        return mInstance;
    }

    public void initialize(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }


}