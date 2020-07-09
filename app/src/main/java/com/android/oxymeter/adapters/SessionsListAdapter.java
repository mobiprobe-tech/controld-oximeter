package com.android.oxymeter.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.android.oxymeter.R;
import com.android.oxymeter.room_db.History.SessionTable;
import com.android.oxymeter.utilities.CommonUtils;

import java.util.List;

public class SessionsListAdapter extends BaseAdapter {

    private Context mContext;
    private List<SessionTable> mList;

    public SessionsListAdapter(Context context, List<SessionTable> list) {

        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
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

            view = LayoutInflater.from(mContext).inflate(R.layout.list_item_session, null);

            viewHolder.mSessionDateTextView = view.findViewById(R.id.startDateTextView);
            viewHolder.mDurationTextView = view.findViewById(R.id.durationTextView);
            viewHolder.mSyncTextView = view.findViewById(R.id.syncTextView);
            viewHolder.mPulseTextView = view.findViewById(R.id.avgPulseTextView);
            viewHolder.mSpO2TextView = view.findViewById(R.id.sPo2TextView);
            viewHolder.mPiTextView = view.findViewById(R.id.piTextView);

            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }


        boolean isSynced = mList.get(position).getmSync() == 1;

        if (isSynced) {
            viewHolder.mSyncTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorGreen));
        } else {
            viewHolder.mSyncTextView.setTextColor(ContextCompat.getColor(mContext, R.color.bloodColor));
        }


        viewHolder.mSyncTextView.setText(isSynced ? mContext.getResources().getString(R.string.synced) : mContext.getResources().getString(R.string.unsynced));
        viewHolder.mSessionDateTextView.setText(CommonUtils.millisToDate(mList.get(position).getmStartTime()));
        viewHolder.mDurationTextView.setText(CommonUtils.millisToMinutes(mList.get(position).getmDuration()));
        viewHolder.mPulseTextView.setText(String.valueOf(mList.get(position).getmAveragePulse()));
        viewHolder.mSpO2TextView.setText(String.valueOf(mList.get(position).getmAverageSpO2()));
        viewHolder.mPiTextView.setText(String.valueOf(mList.get(position).getmAveragePI()));

        return view;
    }

    private class ViewHolder {

        AppCompatTextView mSessionDateTextView, mDurationTextView, mSyncTextView, mPulseTextView, mSpO2TextView, mPiTextView;
    }
}
