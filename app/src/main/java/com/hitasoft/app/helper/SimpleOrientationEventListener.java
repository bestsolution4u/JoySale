package com.hitasoft.app.helper;

import android.content.Context;
import android.view.OrientationEventListener;

public abstract class SimpleOrientationEventListener extends OrientationEventListener {

    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT_REVERSE = 2;
    public static final int ORIENTATION_LANDSCAPE_REVERSE = 3;


    private int lastOrientation = 0;

    public SimpleOrientationEventListener(Context context) {
        super(context);
    }

    public SimpleOrientationEventListener(Context context, int rate) {
        super(context, rate);
    }

    @Override
    public final void onOrientationChanged(int orientation) {
        if (orientation < 0) {
            return; // Flip screen, Not take account
        }

        int curOrientation;

        if (orientation <= 45) {
            curOrientation = ORIENTATION_PORTRAIT;
        } else if (orientation <= 135) {
            curOrientation = ORIENTATION_LANDSCAPE_REVERSE;
        } else if (orientation <= 225) {
            curOrientation = ORIENTATION_PORTRAIT_REVERSE;
        } else if (orientation <= 315) {
            curOrientation = ORIENTATION_LANDSCAPE;
        } else {
            curOrientation = ORIENTATION_PORTRAIT;
        }
        if (curOrientation != lastOrientation) {
            onChanged(lastOrientation, curOrientation);
            lastOrientation = curOrientation;
        }
    }

    public abstract void onChanged(int lastOrientation, int orientation);
}
