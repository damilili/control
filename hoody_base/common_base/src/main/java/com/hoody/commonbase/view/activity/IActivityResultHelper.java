package com.hoody.commonbase.view.activity;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import java.util.ArrayList;


public interface IActivityResultHelper {
    boolean onActivityResult(Activity activity,int requestCode, int resultCode, Intent data);
    ArrayList<IActivityResultHelper> RESULT_HELPERS=new ArrayList<>();
    public static void registerActivityResultHelper(IActivityResultHelper activityResultHelper) {
        if (!RESULT_HELPERS.contains(activityResultHelper)) {
            RESULT_HELPERS.add(activityResultHelper);
        }
    }
    static void unregisterActivityResultHelper(IActivityResultHelper activityResultHelper) {
        RESULT_HELPERS.remove(activityResultHelper);
    }
    static void processorsgResult(Activity activity,int requestCode, int resultCode, @Nullable Intent data) {
        for (IActivityResultHelper activityResultHelper : RESULT_HELPERS) {
            if (activityResultHelper.onActivityResult(activity,requestCode, resultCode, data)) {
                return;
            }
        }
    }
}
