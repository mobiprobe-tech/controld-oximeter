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

public class SpinnerUsersAdapter extends BaseAdapter {

    private Context mContext;
    private List<UserTable> mUsersList;

    public SpinnerUsersAdapter(Context context, List<UserTable> usersList) {

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

            view = LayoutInflater.from(mContext).inflate(R.layout.list_item_single_title, null);

            viewHolder.mNameTextView = view.findViewById(R.id.titleTextView);

            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.mNameTextView.setText(CommonUtils.checkData(mUsersList.get(position).getmName()));

        return view;
    }

    private class ViewHolder {

        AppCompatTextView mNameTextView;
    }
}
