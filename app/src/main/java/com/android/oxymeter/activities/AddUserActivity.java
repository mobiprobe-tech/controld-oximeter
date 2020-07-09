package com.android.oxymeter.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.oxymeter.R;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.web_services.AddSubUser.AddSubUserAPI;
import com.android.oxymeter.web_services.GenericCallback;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class AddUserActivity extends BaseActivity implements View.OnClickListener {

    private TextInputEditText mNameEditText, mEmailEditText, mMobileEditText, mDobEditText, mHeightEditText, mWeightEditText, mBloodGroupEditText;
    private RadioButton mMaleRadioBtn;
    private RadioButton mFemaleRadioBtn;
    private AddSubUserAPI mAddSubUserAPI;
    private String mName, mEmail, mPhone, mGender, mDob, mHeight, mWeight, mBloodGroup;
    private String mSelectedDate;
    private int year;
    private int month;
    private int day;
    private Date minDate, maxDate;
    private ArrayAdapter<CharSequence> mBloodGroupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        setUpToolbar();
        initializeView();

        getCurrentDate();
        setBloodGroupAdapter();
    }

    @Override
    public void setUpToolbar() {

        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setTitle(getResources().getString(R.string.title_add_user));

            toolbar.setNavigationOnClickListener(v -> onBackPressed());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initializeView() {

        mNameEditText = findViewById(R.id.name_editText);
        mEmailEditText = findViewById(R.id.email_editText);
        mMobileEditText = findViewById(R.id.mobile_editText);
        mDobEditText = findViewById(R.id.dob_editText);
        mHeightEditText = findViewById(R.id.height_editText);
        mWeightEditText = findViewById(R.id.weight_editText);
        mBloodGroupEditText = findViewById(R.id.bloodGroup_editText);

        mMaleRadioBtn = findViewById(R.id.male_radioBtn);
        mFemaleRadioBtn = findViewById(R.id.female_radioBtn);

        mDobEditText.setOnClickListener(this);
        mBloodGroupEditText.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.add_user_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_done) {
            CommonUtils.closeKeyBoard(AddUserActivity.this);

            CommonUtils.showAlertWithTwoCustomButton(AddUserActivity.this, "", getResources().getString(R.string.sure_to_add_user), getResources().getString(R.string.yes), getResources().getString(R.string.no), () -> {

                getData();

                if (checkDataValidation()) {
                    if (CommonUtils.isNetworkAvailable(AddUserActivity.this)) {
                        addUser();
                    } else {
                        CommonUtils.showSmallToast(AddUserActivity.this, getResources().getString(R.string.no_internet));
                    }
                }


            }, () -> {
                //do nothing
            });


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        CommonUtils.closeKeyBoard(AddUserActivity.this);

        if (v.getId() == R.id.dob_editText) {

            openDatePickerDialog();

        } else if (v.getId() == R.id.bloodGroup_editText) {

            showSpinnerDialog(mBloodGroupAdapter, mBloodGroupEditText, getResources().getString(R.string.select_blood_group));

        }
    }

    /**
     * Get current date
     */
    private void getCurrentDate() {

        // Get current date by calender
        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        maxDate = c.getTime();

        c.add(Calendar.YEAR, getResources().getInteger(R.integer.max_user_age));

        minDate = c.getTime();

    }

    /**
     * Open calendar
     */
    private void openDatePickerDialog() {

        CommonUtils.closeKeyBoard(AddUserActivity.this);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    year = selectedYear;
                    month = selectedMonth;
                    day = selectedDay;

                    String selectedDay_new = day < 10 ? ("0" + day) : day + "";
                    String selectedMonth_new = (month + 1) < 10 ? ("0" + (month + 1)) : ((month + 1) + "");

                    mSelectedDate = year + "-" + selectedMonth_new + "-" + selectedDay_new;

                    // Show selected date
                    mDobEditText.setText(CommonUtils.getDate(mSelectedDate));


                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());
        datePickerDialog.getDatePicker().setMinDate(minDate.getTime());

        datePickerDialog.show();

    }

    /**
     * Method to set list in blood group Adapter
     */
    private void setBloodGroupAdapter() {
        mBloodGroupAdapter = ArrayAdapter.createFromResource(AddUserActivity.this, R.array.blood_group_array_list, R.layout.support_simple_spinner_dropdown_item);
    }

    /**
     * This method opens dialog with list
     */
    private void showSpinnerDialog(final ArrayAdapter<CharSequence> adapter, final TextInputEditText editText, String title) {
        new AlertDialog.Builder(AddUserActivity.this).setTitle(title).setAdapter(adapter, (dialog, position) -> {
            editText.setText(adapter.getItem(position));

            dialog.dismiss();
        }).create().show();
    }

    /**
     * Method to get data of the new user
     */
    private void getData() {
        mName = CommonUtils.checkData(Objects.requireNonNull(mNameEditText.getText()).toString());
        mEmail = CommonUtils.checkData(Objects.requireNonNull(mEmailEditText.getText()).toString());
        mPhone = CommonUtils.checkData(Objects.requireNonNull(mMobileEditText.getText()).toString());
        mDob = CommonUtils.checkData(mSelectedDate);
        mHeight = CommonUtils.checkData(Objects.requireNonNull(mHeightEditText.getText()).toString());
        mWeight = CommonUtils.checkData(Objects.requireNonNull(mWeightEditText.getText()).toString());
        mBloodGroup = CommonUtils.checkData(Objects.requireNonNull(mBloodGroupEditText.getText()).toString());

        if (mMaleRadioBtn.isChecked()) {
            mGender = CommonUtils.checkData(getResources().getString(R.string.male));
        } else if (mFemaleRadioBtn.isChecked()) {
            mGender = CommonUtils.checkData(getResources().getString(R.string.female));
        } else {
            mGender = CommonUtils.checkData(getResources().getString(R.string.other));
        }

    }

    /**
     * Method to check data validation before updating the user details
     *
     * @return TRUE if data is valid otherwise FALSE
     */
    private boolean checkDataValidation() {

        boolean isValidData = true;

        if (mName.isEmpty()) {

            mNameEditText.requestFocus();

            CommonUtils.showSmallToast(AddUserActivity.this, getResources().getString(R.string.required_field_error, getResources().getString(R.string.name)));
            isValidData = false;

        } else if ((!mEmail.isEmpty()) && (!CommonUtils.isValidEmail(mEmail))) {

            mEmailEditText.requestFocus();

            CommonUtils.showSmallToast(AddUserActivity.this, getResources().getString(R.string.invalid_field_error, getResources().getString(R.string.email)));
            isValidData = false;

        } else if ((!mPhone.isEmpty()) && (!CommonUtils.validPhoneNumber(mPhone, AddUserActivity.this))) {

            mMobileEditText.requestFocus();

            CommonUtils.showSmallToast(AddUserActivity.this, getResources().getString(R.string.invalid_field_error, getResources().getString(R.string.mobile)));
            isValidData = false;

        }

        return isValidData;
    }

    /**
     * Call web API to add new user
     */
    private void addUser() {
        if (CommonUtils.isNetworkAvailable(AddUserActivity.this)) {

            mAddSubUserAPI = new AddSubUserAPI(AddUserActivity.this, true, mName, mEmail, mPhone, mGender, mDob, mHeight, mWeight, mBloodGroup, new GenericCallback() {
                @Override
                public void onSuccess(String successMessage) {
                    mAddSubUserAPI = null;
                    CommonUtils.showSmallToast(AddUserActivity.this, successMessage);

                    updateUsersListBroadcast();
                }

                @Override
                public void onFailure(String errorMessage) {
                    mAddSubUserAPI = null;
                    CommonUtils.showLongToast(AddUserActivity.this, errorMessage);
                }
            });

        } else {
            CommonUtils.showLongToast(AddUserActivity.this, getResources().getString(R.string.no_internet));
        }
    }

    /**
     * local broadcast to update the users list on previous screen
     */
    private void updateUsersListBroadcast() {
        Intent intent = new Intent(Constants.INTENT_ACTION_UPDATE_USERS_LIST);
        intent.putExtra(Constants.KEY_FLAG, 1);
        LocalBroadcastManager.getInstance(AddUserActivity.this).sendBroadcast(intent);

        AddUserActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAddSubUserAPI != null) {
            mAddSubUserAPI.cancelRequestAPI();
        }

    }
}



