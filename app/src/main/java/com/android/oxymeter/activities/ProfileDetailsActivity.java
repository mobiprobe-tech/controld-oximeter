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
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.oxymeter.R;
import com.android.oxymeter.fragments.ui.users_tab.UsersViewModel;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.web_services.DeleteSubUser.DeleteSubUserAPI;
import com.android.oxymeter.web_services.GenericCallback;
import com.android.oxymeter.web_services.GetUserDetails.GetUserDetailsAPI;
import com.android.oxymeter.web_services.UpdateUser.UpdateUserAPI;
import com.android.oxymeter.web_services.UserDetailsCallback;
import com.android.oxymeter.web_services.UserDetailsResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ProfileDetailsActivity extends BaseActivity implements View.OnClickListener {

    private String mTitle = "", mUserID = "";
    private boolean mEditModeOn = false; //open edit mode
    private boolean mLoggedInUser = false;// whether the selected user is the logged-in user or the sub-user
    private TextInputEditText mNameEditText, mEmailEditText, mMobileEditText, mDobEditText, mHeightEditText, mWeightEditText, mBloodGroupEditText;
    private RadioButton mMaleRadioBtn, mFemaleRadioBtn, mOtherRadioBtn;
    private DeleteSubUserAPI mDeleteSubUserAPI;
    private GetUserDetailsAPI mGetUserDetailsAPI;
    private UserDetailsResponse.Datum mUserDetailsResponse;
    private String mName, mEmail, mPhone, mGender, mDob, mHeight, mWeight, mBloodGroup;
    private UpdateUserAPI mUpdateUserAPI;
    private String mSelectedDate;
    private int year;
    private int month;
    private int day;
    private Date minDate, maxDate;
    private ArrayAdapter<CharSequence> mBloodGroupAdapter;


    private UsersViewModel usersViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        if (getIntent().hasExtra(Constants.EXTRAS_TITLE)) {
            mTitle = getIntent().getStringExtra(Constants.EXTRAS_TITLE);
        }

        if (getIntent().hasExtra(Constants.EXTRAS_SELECTED_USER_ID)) {
            mUserID = getIntent().getStringExtra(Constants.EXTRAS_SELECTED_USER_ID);
        }

        mLoggedInUser = CommonUtils.checkData(mUserID).isEmpty();

        setUpToolbar();
        initializeView();

        getUserDetails();

        getCurrentDate();
        setBloodGroupAdapter();

        usersViewModel = new ViewModelProvider(ProfileDetailsActivity.this).get(UsersViewModel.class);
    }

    @Override
    public void setUpToolbar() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setTitle(mTitle);

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
        mOtherRadioBtn = findViewById(R.id.other_radioBtn);

        setFieldsEditable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.user_profile_menu, menu);

        menu.findItem(R.id.action_edit).setVisible(!mEditModeOn);
        menu.findItem(R.id.action_delete).setVisible(!mLoggedInUser);
        menu.findItem(R.id.action_done).setVisible(mEditModeOn);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete:

                CommonUtils.closeKeyBoard(ProfileDetailsActivity.this);

                CommonUtils.showAlertWithTwoCustomButton(ProfileDetailsActivity.this, "", getResources().getString(R.string.sure_to_del_user), getResources().getString(R.string.yes), getResources().getString(R.string.no), this::deleteUser, () -> {
                    //do nothing
                });

                return true;

            case R.id.action_edit:
                CommonUtils.closeKeyBoard(ProfileDetailsActivity.this);
                setFieldsEditable(true);

                return true;

            case R.id.action_done:
                CommonUtils.closeKeyBoard(ProfileDetailsActivity.this);

                CommonUtils.showAlertWithTwoCustomButton(ProfileDetailsActivity.this, "", getResources().getString(R.string.sure_to_update_user), getResources().getString(R.string.yes), getResources().getString(R.string.no), () -> {

                    getData();

                    if (checkDataValidation()) {
                        if (CommonUtils.isNetworkAvailable(ProfileDetailsActivity.this)) {

                            updateUserDetails();

                        } else {
                            CommonUtils.showLongToast(ProfileDetailsActivity.this, getResources().getString(R.string.no_internet));
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

        CommonUtils.closeKeyBoard(ProfileDetailsActivity.this);

        if (v.getId() == R.id.dob_editText) {

            openDatePickerDialog();

        } else if (v.getId() == R.id.bloodGroup_editText) {

            showSpinnerDialog(mBloodGroupAdapter, mBloodGroupEditText, getResources().getString(R.string.select_blood_group));

        }

    }

    /**
     * Method to set input fields editable or not
     *
     * @param isEditable if TRUE set input fields editable else non-editable
     */
    private void setFieldsEditable(boolean isEditable) {
        mEditModeOn = isEditable;
        invalidateOptionsMenu();

        mDobEditText.setClickable(isEditable);
        mBloodGroupEditText.setClickable(isEditable);

        if (mLoggedInUser) {

            //enable/disable name, email, phone fields according to login type (if required)

            /*SharedPreferences sharedPref = getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
            int loginType = sharedPref.getInt(Constants.TOKEN, 0); // 0= google, 1= facebook, 2 = phone

            if (loginType == 2) {
                mEmailEditText.setEnabled(isEditable);
                mMobileEditText.setEnabled(false);
            } else {
                mEmailEditText.setEnabled(false);
                mMobileEditText.setEnabled(isEditable);
            }

            mNameEditText.setEnabled(isEditable);*/

            mNameEditText.setEnabled(false);
            mEmailEditText.setEnabled(false);
            mMobileEditText.setEnabled(false);
        } else {
            mNameEditText.setEnabled(isEditable);
            mEmailEditText.setEnabled(isEditable);
            mMobileEditText.setEnabled(isEditable);
        }

        mHeightEditText.setEnabled(isEditable);
        mWeightEditText.setEnabled(isEditable);
        mMaleRadioBtn.setEnabled(isEditable);
        mFemaleRadioBtn.setEnabled(isEditable);
        mOtherRadioBtn.setEnabled(isEditable);

        if (isEditable) {
            mDobEditText.setOnClickListener(ProfileDetailsActivity.this);
            mBloodGroupEditText.setOnClickListener(ProfileDetailsActivity.this);

            String gender = mUserDetailsResponse.getGender();

            if (gender.isEmpty()) {
                mMaleRadioBtn.setChecked(true); //default
                mFemaleRadioBtn.setChecked(false);
                mOtherRadioBtn.setChecked(false);
            }
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

        CommonUtils.closeKeyBoard(ProfileDetailsActivity.this);

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
        mBloodGroupAdapter = ArrayAdapter.createFromResource(ProfileDetailsActivity.this, R.array.blood_group_array_list, R.layout.support_simple_spinner_dropdown_item);
    }

    /**
     * This method opens dialog with list
     */
    private void showSpinnerDialog(final ArrayAdapter<CharSequence> adapter, final TextInputEditText editText, String title) {
        new AlertDialog.Builder(ProfileDetailsActivity.this).setTitle(title).setAdapter(adapter, (dialog, position) -> {
            editText.setText(adapter.getItem(position));

            dialog.dismiss();
        }).create().show();
    }

    /**
     * Call web API to get logged-in user details
     */
    private void getUserDetails() {
        if (CommonUtils.isNetworkAvailable(ProfileDetailsActivity.this)) {

            mGetUserDetailsAPI = new GetUserDetailsAPI(ProfileDetailsActivity.this, true, mUserID, new UserDetailsCallback() {

                @Override
                public void onSuccess(UserDetailsResponse.Datum userDetails) {
                    mGetUserDetailsAPI = null;
                    mUserDetailsResponse = new UserDetailsResponse.Datum();
                    mUserDetailsResponse = userDetails;
                    setUserData();
                }

                @Override
                public void onFailure(String errorMessage) {
                    mGetUserDetailsAPI = null;
                    CommonUtils.showLongToast(ProfileDetailsActivity.this, errorMessage);
                    ProfileDetailsActivity.this.finish();
                }
            });

        } else {
            CommonUtils.showLongToast(ProfileDetailsActivity.this, getResources().getString(R.string.no_internet));
            ProfileDetailsActivity.this.finish();
        }
    }

    /**
     * Method to set users details in view
     */
    private void setUserData() {

        String name = CommonUtils.checkData(mUserDetailsResponse.getName());

        if (CommonUtils.checkData(mUserID).isEmpty() && name.equalsIgnoreCase(Constants.MYSELF)) {
            name = "";
        }

        mNameEditText.setText(name);
        mEmailEditText.setText(mUserDetailsResponse.getEmail());
        mMobileEditText.setText(mUserDetailsResponse.getPhone());
        mDobEditText.setText(CommonUtils.getDate(mUserDetailsResponse.getDob()));
        mHeightEditText.setText(mUserDetailsResponse.getHeight());
        mWeightEditText.setText(mUserDetailsResponse.getWeight());
        mBloodGroupEditText.setText(mUserDetailsResponse.getBloodgroup());

        String gender = mUserDetailsResponse.getGender();

        if (gender.isEmpty()) {
            mMaleRadioBtn.setChecked(false);
            mFemaleRadioBtn.setChecked(false);
            mOtherRadioBtn.setChecked(false);
        } else {
            if (gender.equalsIgnoreCase(getResources().getString(R.string.male))) {
                mMaleRadioBtn.setChecked(true);
            } else if (gender.equalsIgnoreCase(getResources().getString(R.string.female))) {
                mFemaleRadioBtn.setChecked(true);
            } else {
                mOtherRadioBtn.setChecked(true);
            }
        }

        mSelectedDate = mUserDetailsResponse.getDob();
    }

    /**
     * Call web API to delete the selected SUB_USER
     */
    private void deleteUser() {
        if (CommonUtils.isNetworkAvailable(ProfileDetailsActivity.this)) {

            mDeleteSubUserAPI = new DeleteSubUserAPI(ProfileDetailsActivity.this, true, mUserID, new GenericCallback() {
                @Override
                public void onSuccess(String successMessage) {
                    mDeleteSubUserAPI = null;
                    CommonUtils.showSmallToast(ProfileDetailsActivity.this, successMessage);
                    // updateUsersListBroadcast();

                    usersViewModel.deleteUser(mUserID);
                    ProfileDetailsActivity.this.finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    mDeleteSubUserAPI = null;
                    CommonUtils.showLongToast(ProfileDetailsActivity.this, errorMessage);
                }
            });

        } else {
            CommonUtils.showLongToast(ProfileDetailsActivity.this, getResources().getString(R.string.no_internet));
        }
    }

    /**
     * Method to get data of the user
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

        if (!mLoggedInUser) {

            if (mName.isEmpty()) {

                mNameEditText.requestFocus();

                CommonUtils.showSmallToast(ProfileDetailsActivity.this, getResources().getString(R.string.required_field_error, getResources().getString(R.string.name)));
                isValidData = false;

            } else if ((!mEmail.isEmpty()) && (!CommonUtils.isValidEmail(mEmail))) {

                mEmailEditText.requestFocus();

                CommonUtils.showSmallToast(ProfileDetailsActivity.this, getResources().getString(R.string.invalid_field_error, getResources().getString(R.string.email)));
                isValidData = false;

            } else if ((!mPhone.isEmpty()) && (!CommonUtils.validPhoneNumber(mPhone, ProfileDetailsActivity.this))) {

                mMobileEditText.requestFocus();

                CommonUtils.showSmallToast(ProfileDetailsActivity.this, getResources().getString(R.string.invalid_field_error, getResources().getString(R.string.mobile)));
                isValidData = false;

            }
        }

        return isValidData;
    }

    /**
     * call web API to update logged user details
     */
    private void updateUserDetails() {
        if (CommonUtils.isNetworkAvailable(ProfileDetailsActivity.this)) {

            mUpdateUserAPI = new UpdateUserAPI(ProfileDetailsActivity.this, true, mUserID, mName, mEmail, mPhone, mGender, mDob, mHeight, mWeight, mBloodGroup, new GenericCallback() {
                @Override
                public void onSuccess(String successMessage) {
                    mUpdateUserAPI = null;
                    CommonUtils.showSmallToast(ProfileDetailsActivity.this, successMessage);

                    updateUsersListBroadcast();
                }

                @Override
                public void onFailure(String errorMessage) {
                    mUpdateUserAPI = null;
                    CommonUtils.showLongToast(ProfileDetailsActivity.this, errorMessage);
                }
            });

        } else {
            CommonUtils.showLongToast(ProfileDetailsActivity.this, getResources().getString(R.string.no_internet));
        }
    }


    /**
     * local broadcast to update the users list on previous screen
     */
    private void updateUsersListBroadcast() {
        Intent intent = new Intent(Constants.INTENT_ACTION_UPDATE_USERS_LIST);
        intent.putExtra(Constants.KEY_FLAG, 1);
        LocalBroadcastManager.getInstance(ProfileDetailsActivity.this).sendBroadcast(intent);
        ProfileDetailsActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGetUserDetailsAPI != null) {
            mGetUserDetailsAPI.cancelRequestAPI();
        }

        if (mDeleteSubUserAPI != null) {
            mDeleteSubUserAPI.cancelRequestAPI();
        }

        if (mUpdateUserAPI != null) {
            mUpdateUserAPI.cancelRequestAPI();
        }
    }


}
