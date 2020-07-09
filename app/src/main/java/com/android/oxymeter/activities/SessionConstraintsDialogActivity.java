package com.android.oxymeter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.lifecycle.ViewModelProvider;

import com.android.oxymeter.R;
import com.android.oxymeter.adapters.SpinnerUsersAdapter;
import com.android.oxymeter.fragments.ui.users_tab.UsersViewModel;
import com.android.oxymeter.room_db.Users.UserTable;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;

import java.util.ArrayList;

public class SessionConstraintsDialogActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatSpinner mUserSpinner;
    private String mSelectedUserID;
    private boolean mTimeBoundRecordingIsSelected = false;

    private ArrayList<UserTable> mUsersList;
    private SpinnerUsersAdapter mUsersAdapter;

    private UsersViewModel usersViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_constraints_dialog);

        usersViewModel = new ViewModelProvider(SessionConstraintsDialogActivity.this).get(UsersViewModel.class);

        initializeView();
        setUsersAdapter();
    }

    @Override
    public void setUpToolbar() {
//do nothing
    }

    @Override
    public void initializeView() {

        mUserSpinner = findViewById(R.id.userSpinner);
        AppCompatSpinner mRecordingTypeSpinner = findViewById(R.id.readingTypeSpinner);

        AppCompatButton mDoneBtn = findViewById(R.id.doneBtn);

        mDoneBtn.setOnClickListener(this);

        ArrayAdapter<CharSequence> mRecordingTypeAdapter = ArrayAdapter.createFromResource(SessionConstraintsDialogActivity.this, R.array.recording_type_array_list, R.layout.support_simple_spinner_dropdown_item);

        mRecordingTypeSpinner.setAdapter(mRecordingTypeAdapter);

        mUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedUserID = CommonUtils.checkData(mUsersList.get(position).getmUserID());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });


        mRecordingTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mTimeBoundRecordingIsSelected = position == 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.doneBtn) {

            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRAS_SELECTED_USER_ID, mSelectedUserID);
            intent.putExtra(Constants.EXTRAS_SELECTED_RECORDING_TYPE, mTimeBoundRecordingIsSelected);

            setResult(RESULT_OK, intent);

            SessionConstraintsDialogActivity.this.finish();
        }
    }

    /**
     * Method to set list in users Adapter
     */
    private void setUsersAdapter() {

        usersViewModel.getAllUsers().observe(SessionConstraintsDialogActivity.this, users -> {

            if (mUsersList == null) {
                mUsersList = new ArrayList<>();
            }

            mUsersList.clear();

            if (users.size() > 0) {

                mUsersList.addAll(users);
                mUsersAdapter = new SpinnerUsersAdapter(SessionConstraintsDialogActivity.this, mUsersList);
                mUserSpinner.setAdapter(mUsersAdapter);

            }
        });

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        SessionConstraintsDialogActivity.this.finish();
    }
}
