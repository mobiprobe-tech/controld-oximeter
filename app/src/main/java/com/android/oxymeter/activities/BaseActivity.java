package com.android.oxymeter.activities;

import androidx.appcompat.app.AppCompatActivity;

import com.android.oxymeter.utilities.CommonUtils;

public abstract class BaseActivity extends AppCompatActivity {


    /**
     * This method is used for initializing toolbar
     */
    public abstract void setUpToolbar();

    /**
     * This method is used for initializing the view
     */
    public abstract void initializeView();

    @Override
    protected void onStop() {
        super.onStop();
        CommonUtils.closeKeyBoard(BaseActivity.this);
    }
}
