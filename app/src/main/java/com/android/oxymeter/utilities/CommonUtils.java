package com.android.oxymeter.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.android.oxymeter.BuildConfig;
import com.android.oxymeter.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

/**
 * THIS CLASS CONTAINS ALL the common methods/values used throughout the
 * application
 */
public class CommonUtils {

    private static ProgressDialog progress_Dialog = null;

    /**
     * A dialog showing a progress indicator and an optional text message or
     * view. Only a text message or a view can be used at the same time.
     *
     * @param context - context of the activity on which Progress dialog need to be
     *                shown
     * @param message - message to be shown in progress Dialog
     */
    public static void showProgress(Context context, String message) {

        // check if progress dialog is already visible. If visible, then remove it.
        dismissProgress();

        progress_Dialog = new ProgressDialog(context);
        progress_Dialog.setCancelable(false);
        progress_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress_Dialog.setMessage(message);
        progress_Dialog.show();
    }

    /**
     * Dismiss this dialog, removing it from the screen. This method can be
     * invoked safely from any thread. Note that you should not override this
     * method to do cleanup when the dialog is dismissed
     */
    public static void dismissProgress() {

        if (progress_Dialog != null) {
            try {
                if (progress_Dialog.isShowing()) {
                    progress_Dialog.dismiss();
                    progress_Dialog = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to close the soft keypad
     *
     * @param activity current activity
     */
    public static void closeKeyBoard(FragmentActivity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(Objects.requireNonNull(activity.getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // do nothing
        }
    }

    /**
     * Method used to show Small Toast.
     *
     * @param context context of the current activity
     * @param message message to be shown on Toast
     */
    public static void showSmallToast(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Method used to show Long Toast.
     *
     * @param context context of the current activity
     * @param message message to be shown on Toast
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    /**
     * Method for displaying Logs. Set "isDebuggerOn" to false if you do not
     * want to display Logs else set it to true
     *
     * @param tag     Tag of the Log
     * @param message message to be displayed in Log
     * @author ngoyal
     */
    public static void myLog(String tag, String message) {
        boolean isDebuggerOn = !BuildConfig.DEBUG;
        isDebuggerOn=true;
        if (isDebuggerOn) {
            try {
                Log.d(tag, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Method to return empty string in case the result from server is null or empty
     *
     * @param string the string to be examined
     * @return empty string if the provided value is empty or null, else the value itself
     */
    public static String checkData(String string) {
        if (string == null || TextUtils.isEmpty(string.trim())) {
            return "";
        }

        return string.trim();
    }


    /**
     * This method check the network availability in the device whether its from
     * mobile data or Wi-Fi or any other medium
     *
     * @param context context of the current Activity
     * @return TRUE, if Internet connection is available, FALSE otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }


    /**
     * This method is used to enable vertical scrolling for multiple line edit text.
     * If inside a scroll view then this method will disable the parent scroll and will allow the user to scroll the data inside the edit text.
     *
     * @param view Name of the view for which scrolling is to be enabled
     * @param id   ID of the view
     */
    public static void enableVerticalScroll(View view, final int id) {

        view.setVerticalScrollBarEnabled(true);
        view.setHorizontalScrollBarEnabled(true);
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (v.getId() == id) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    public interface OnAlertOkClickListener {
        void onOkButtonClicked();
    }

    public interface onAlertCancelClickListener {
        void onCancelButtonClicked();
    }

    public static void showAlertWithTwoCustomButton(Context context, String title, String message, String positiveBtnText, String negativeBtnText, final OnAlertOkClickListener onAlertOkClickListener, final onAlertCancelClickListener onCancelButtonClicked) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveBtnText, (dialog, which) -> onAlertOkClickListener.onOkButtonClicked());
        builder.setNegativeButton(negativeBtnText, (dialog, which) -> onCancelButtonClicked.onCancelButtonClicked());
        builder.setCancelable(false);
        builder.show();
    }

    public static void showAlertWithSingleCustomButton(Context context, String title, String message, String positiveBtnText, final OnAlertOkClickListener onAlertOkClickListener) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveBtnText, (dialog, which) -> onAlertOkClickListener.onOkButtonClicked());
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * convert from an integer to UUID
     *
     * @param i integer
     * @return UUID
     */
    public static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    /**
     * Method to check email is valid or not
     *
     * @param email email address which is to be verified
     * @return TRUE if valid otherwise FALSE
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean validPhoneNumber(String phoneNo, Context context) {
        return phoneNo.length() >= context.getResources().getInteger(R.integer.phone_min_length) && !(phoneNo.charAt(0) == '0' || phoneNo.contains("+")) && isPhoneValid(phoneNo);
    }

    private static boolean isPhoneValid(CharSequence phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    /**
     * @param type method used by user to login
     * @return 0 if Login via Google, 1 if Login via Facebook, 0 if Login via Phone
     */
    public static int getLoginType(String type) {

        switch (type.toLowerCase()) {
            case "google.com":
                return 0;

            case "facebook.com":
                return 1;

            default:
                return 2;
        }

    }

    /**
     * Get Date from Date-Time
     *
     * @param dateString date/time string from server (yyyy-MM-dd)
     * @return date in required date format (dd-MMM-yyyy)
     */
    @SuppressLint("SimpleDateFormat")
    public static String getDate(String dateString) {

        myLog("convertDateFormat", "Source DateTime: " + dateString);

        if (dateString == null || dateString.equalsIgnoreCase(""))
            return dateString;

        SimpleDateFormat srcDateFormat;

        srcDateFormat = new SimpleDateFormat("yyyy-MM-dd");// 2016-11-28

        String requiredDate = "";
        SimpleDateFormat reqDateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy");  // output: 18-July-2016

        try {
            Date varDate = srcDateFormat.parse(dateString);
            requiredDate = reqDateTimeFormat.format(varDate);

        } catch (Exception e) {
            e.printStackTrace();
        }

        myLog("convertDateFormat", "requiredDate DateTime: " + requiredDate);
        return requiredDate;

    }

    /**
     * Method for getting age from Date of birth
     *
     * @param dateOfBirth yyyy-MM-dd
     * @return age of the user
     */
    public static String getAge(String dateOfBirth) {

        if (dateOfBirth.isEmpty()) {
            return "";
        }

        int year = Integer.valueOf(dateOfBirth.split("-")[0]);
        int month = Integer.valueOf(dateOfBirth.split("-")[1]);
        int day = Integer.valueOf(dateOfBirth.split("-")[2]);

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        // calculate months
        int months = 0;
        if (age < 1) {
            months = calculateMonth(today.get(Calendar.MONTH), dob.get(Calendar.MONTH));

        }

        int days = 0;

        if (months < 1) {
            days = calculateDay(today.get(Calendar.DAY_OF_MONTH), dob.get(Calendar.DAY_OF_MONTH));

        }

        Integer ageInt = age < 1 ? 0 : age;

        return ageInt.toString();
    }


    public static int calculateMonth(int endMonth, int startMonth) {
        int ageInMonths;
        if (endMonth >= startMonth) {
            ageInMonths = endMonth - startMonth;
        } else {
            ageInMonths = endMonth - startMonth;
            ageInMonths = 12 + ageInMonths;
        }

        return ageInMonths;
    }

    public static int calculateDay(int endDay, int startDay) {
        int ageInDays;
        if (endDay >= startDay) {
            ageInDays = endDay - startDay;
        } else {
            ageInDays = endDay - startDay;
            ageInDays = 30 + ageInDays;
//resMonth--;
        }

        return ageInDays;
    }

    public static String millisToDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return checkData(formatter.format(calendar.getTime()));
    }

    public static String millisToMinutes(long millis) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        return checkData(df.format(new Date(millis)));

    }
}
