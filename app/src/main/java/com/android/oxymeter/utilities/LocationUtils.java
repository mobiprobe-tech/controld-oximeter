package com.android.oxymeter.utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;

import androidx.core.content.ContextCompat;

import com.android.oxymeter.R;
import com.android.oxymeter.interfaces.OnLocationFoundListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;


/**
 * Class for getting user's current location
 * check for Location permissions if SDK >22
 * check if GPS is enable
 */
public class LocationUtils {
    public static final int REQUEST_CHECK_SETTINGS = 738;
    public static final int REQUEST_FIND_LOCATION = 562;

    private static final long INTERVAL = 1000 * 60 * 10; // minutes
    private static final long FASTEST_INTERVAL = 1000 * 60 * 2; // minutes
    private static final long MAX_WAIT_TIME = 1000 * 60 * 30; // minutes
    private static final long EXPIRATION_DURATION = 1000 * 60 * 40; // minutes

    private LocationRequest mLocationRequest;
    private Context context;
    private OnLocationFoundListener listener;
    /**
     * Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private static LocationCallback mLocationCallback;

    private double mLatitude = 0.0;
    private double mLongitude = 0.0;

    public LocationUtils(Context context, OnLocationFoundListener listener) {
        this.listener = listener;
        this.context = context;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);


        createLocationRequest();
        createLocationCallback();

        if (Build.VERSION.SDK_INT < 23) {
            getLastKnownLocation();
        } else {
            checkPermissions();
        }
    }

    /**
     * Create Location Request
     */
    private void createLocationRequest() {

        CommonUtils.myLog("LocationUtils", "createLocationRequest");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
        mLocationRequest.setExpirationDuration(EXPIRATION_DURATION);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void createLocationCallback() {


        CommonUtils.myLog("LocationUtils", "createLocationCallback");

        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                CommonUtils.myLog("LocationUtils", "onLocationResult");

                if (locationResult == null) {
                    setLocation(false);
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...

                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                }

                setLocation(true);
            }

        };
    }


    /**
     * Method for check Runtime permissions
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {

        CommonUtils.myLog("LocationUtils", "checkPermissions");

        String accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String accessCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
        int hasAccessFineLocation = ContextCompat.checkSelfPermission(context, accessFineLocation);
        int hasAccessCoarseLocation = ContextCompat.checkSelfPermission(context, accessCoarseLocation);
        List<String> permissions = new ArrayList<>();
        if (hasAccessFineLocation != PackageManager.PERMISSION_GRANTED) {
            permissions.add(accessFineLocation);
        }
        if (hasAccessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            permissions.add(accessCoarseLocation);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ((Activity) context).requestPermissions(params, REQUEST_FIND_LOCATION);
        } else {
            getLastKnownLocation();//TODO
        }

    }


    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {

        CommonUtils.myLog("LocationUtils", "getLastKnownLocation");

        mFusedLocationClient.getLastLocation().addOnSuccessListener((Activity) context, location -> {

            if (location != null) {
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();

                setLocation(true);

            } else {
                checkLocationSettings();
            }


        });

        mFusedLocationClient.getLastLocation().addOnFailureListener((Activity) context, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult((Activity) context,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                    sendEx.printStackTrace();
                }
            } else {
                setLocation(false);
            }
        });

        mFusedLocationClient.getLastLocation().addOnCanceledListener((Activity) context, new OnCanceledListener() {
            @Override
            public void onCanceled() {
                setLocation(false);
            }
        });

    }

    /**
     * Overridden method for onRequestPermissionResults
     *
     * @param requestCode  Permission Request Code
     * @param grantResults List of Permissions Granted
     */
    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_FIND_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings();
            } else {
                setLocation(false);
            }
        }


    }


    /**
     * Method to check if all required location settings are satisfied, like, GPS is turned on
     */
    private void checkLocationSettings() {

        CommonUtils.myLog("LocationUtils", "checkLocationSettings");

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());


        task.addOnSuccessListener((Activity) context, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            setLocation(true);

        });

        task.addOnFailureListener((Activity) context, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult((Activity) context,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                    sendEx.printStackTrace();
                    setLocation(false);
                }
            } else {
                setLocation(false);
            }
        });

        task.addOnCanceledListener((Activity) context, new OnCanceledListener() {
            @Override
            public void onCanceled() {
                setLocation(false);
            }
        });

    }


    public void onActivityResult(int requestCode, int resultCode) {

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                CommonUtils.showProgress(context, context.getResources().getString(R.string.progress_msg_search));

                final Handler handler = new Handler();
                handler.postDelayed(() -> {

                    try {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, null);
                    } catch (SecurityException unlikely) {
                        CommonUtils.myLog("LocationUtils", "Lost location permission. Could not request updates. " + unlikely);
                        setLocation(false);
                    }
                }, 3000);
            } else {
                setLocation(false);
            }
        }
    }


    /**
     * Method for sending current location to the calling activity
     *
     * @param locationFound TRUE if location found otherwise FALSE
     */
    private void setLocation(boolean locationFound) {

        CommonUtils.dismissProgress();
        stopLocationUpdates();

        if (locationFound) {
            listener.onLocationFound(mLatitude, mLongitude);
        } else {
            listener.onLocationNotFound();

        }

    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

}