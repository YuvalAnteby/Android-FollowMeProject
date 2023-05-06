package com.dji.GSDemo.GoogleMap.Classes;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

/**
 * Used for the dji api
 */
public class MApplication extends Application {

    private DJIDemoApplication fpvDemoApplication;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (fpvDemoApplication == null) {
            fpvDemoApplication = new DJIDemoApplication();
            fpvDemoApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fpvDemoApplication.onCreate();
    }

}
