package com.android.oxymeter.interfaces;

/**
 * Created by Asus on 05-06-2017.
 */

public interface OnLocationFoundListener {
    void onLocationFound(double latitude, double longitude);

    void onLocationNotFound();
}
