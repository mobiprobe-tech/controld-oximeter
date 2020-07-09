package com.android.oxymeter.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.appcompat.widget.AppCompatTextView;

import com.android.oxymeter.R;
import com.android.oxymeter.room_db.Users.UserTable;
import com.android.oxymeter.utilities.CommonUtils;

import java.util.List;

public class UsersListAdapter extends BaseAdapter {

    private Context mContext;
    private List<UserTable> mUsersList;

    public UsersListAdapter(Context context, List<UserTable> usersList) {

        this.mContext = context;
        this.mUsersList = usersList;
    }

    @Override
    public int getCount() {
        return mUsersList == null ? 0 : mUsersList.size();
    }

    @Override
    public Object getItem(int position) {
        return mUsersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ViewHolder viewHolder;

        if (view == null) {

            viewHolder = new ViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_item_user, null);

            viewHolder.mNameTextView = view.findViewById(R.id.titleTextView);
            viewHolder.mGenderTextView = view.findViewById(R.id.genderTextView);
            viewHolder.mAgeTextView = view.findViewById(R.id.ageTextView);
            viewHolder.mEmailTextView = view.findViewById(R.id.emailTextView);
            viewHolder.mPhoneTextView = view.findViewById(R.id.phoneTextView);
            viewHolder.mBloodGroupTextView = view.findViewById(R.id.bloodGroupTextView);

            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String name = CommonUtils.checkData(mUsersList.get(position).getmName());
        String email = CommonUtils.checkData(mUsersList.get(position).getmEmail());
        String gender = CommonUtils.checkData(mUsersList.get(position).getmGender());
        String dob = CommonUtils.checkData(mUsersList.get(position).getmDob());
        String phone = CommonUtils.checkData(mUsersList.get(position).getmPhone());
        String bloodGroup = CommonUtils.checkData(mUsersList.get(position).getmBloodGroup());

        gender = gender.isEmpty() ? "" : "(" + gender.toUpperCase().charAt(0) + ")";

        dob = dob.isEmpty() ? "" : CommonUtils.checkData(CommonUtils.getAge(dob));

        if (!dob.isEmpty()) {
            dob = dob.equalsIgnoreCase("0") ? "" : mContext.getResources().getString(R.string.age, dob);
        }

        if (email.isEmpty()) {
            viewHolder.mEmailTextView.setVisibility(View.GONE);
        } else {
            viewHolder.mEmailTextView.setVisibility(View.VISIBLE);
        }

        viewHolder.mNameTextView.setText(name);
        viewHolder.mEmailTextView.setText(email);
        viewHolder.mGenderTextView.setText(gender);
        viewHolder.mPhoneTextView.setText(phone);
        viewHolder.mAgeTextView.setText(dob);
        viewHolder.mBloodGroupTextView.setText(bloodGroup);

        return view;
    }

    private class ViewHolder {

        AppCompatTextView mNameTextView, mGenderTextView, mAgeTextView, mEmailTextView, mPhoneTextView, mBloodGroupTextView;
    }

}
