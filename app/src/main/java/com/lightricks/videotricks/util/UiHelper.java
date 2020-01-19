package com.lightricks.videotricks.util;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;

public class UiHelper {
    private final Context context;
    private final ViewGroup rootLayout;

    public UiHelper(Context context, ViewGroup rootLayout) {
        this.context = context;
        this.rootLayout = rootLayout;
    }

    public void showLongSnackbar(@StringRes int messageString) {
        showLongSnackbar(context.getString(messageString));
    }

    public void showLongSnackbar(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG)
                .show();
    }
}
