package com.android.oxymeter.fragments.ui.history_tab;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.oxymeter.R;
import com.android.oxymeter.activities.SessionDetailsActivity;
import com.android.oxymeter.adapters.SessionsListAdapter;
import com.android.oxymeter.adapters.SpinnerUsersAdapter;
import com.android.oxymeter.fragments.ui.users_tab.UsersViewModel;
import com.android.oxymeter.room_db.History.SessionTable;
import com.android.oxymeter.room_db.History.SessionViewModel;
import com.android.oxymeter.room_db.Users.UserTable;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryFragment extends Fragment {

    private SessionViewModel sessionViewModel;

    private TextView mNoRecordLabel;
    private ListView mListView;
    private List<SessionTable> mList;
    private AppCompatSpinner mUserSpinner;

    private UsersViewModel usersViewModel;
    private ArrayList<UserTable> mUsersList;
    private SpinnerUsersAdapter mUsersAdapter;
    private String mSelectedUserID = "", mSelectedUserName = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(SessionViewModel.class);
        usersViewModel = new ViewModelProvider(getActivity()).get(UsersViewModel.class);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = view.findViewById(R.id.listView);

        mNoRecordLabel = view.findViewById(R.id.no_record_label);

        mUserSpinner = view.findViewById(R.id.userSpinner);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_disconnect).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUsersAdapter();

        mUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mSelectedUserID = CommonUtils.checkData(mUsersList.get(position).getmUserID());
                mSelectedUserName = CommonUtils.checkData(mUsersList.get(position).getmName());

                getSessionsList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//do nothing
            }
        });


        mListView.setOnItemClickListener((parent, view, position, id) -> {

            Intent intent = new Intent(getActivity(), SessionDetailsActivity.class);
            intent.putExtra(Constants.EXTRAS_START_TIMESTAMP, mList.get(position).getmStartTime());
            intent.putExtra(Constants.EXTRAS_SELECTED_USER_ID, mList.get(position).getmUserId());
            intent.putExtra(Constants.EXTRAS_REMOTE_SESSION_ID, mList.get(position).getmSessionID());
            intent.putExtra(Constants.EXTRAS_LOCAL_SESSION_ID, mList.get(position).getId());
            intent.putExtra(Constants.EXTRAS_SELECTED_USER_NAME, mSelectedUserName);
            startActivity(intent);

        });

    }

    /**
     * Method to set list in users Adapter
     */
    private void setUsersAdapter() {

        usersViewModel.getAllUsers().observe(this, users -> {

            if (mUsersList == null) {
                mUsersList = new ArrayList<>();
            }

            mUsersList.clear();

            if (users.size() > 0) {

                mUsersList.addAll(users);
                mUsersAdapter = new SpinnerUsersAdapter(getActivity(), mUsersList);
                mUserSpinner.setAdapter(mUsersAdapter);

            }
        });

    }

    private void getSessionsList() {

        sessionViewModel.getAllSessions(mSelectedUserID).observe(HistoryFragment.this, sessionTables -> {

            if (mList == null) {
                mList = new ArrayList<>();
            }

            mList.clear();


            if (sessionTables.size() > 0) {

                mList.addAll(sessionTables);

                mListView.setVisibility(View.VISIBLE);
                mNoRecordLabel.setVisibility(View.GONE);
            } else {
                mListView.setVisibility(View.GONE);
                mNoRecordLabel.setVisibility(View.VISIBLE);
            }

            setDataInAdapter();
        });
    }

    private void setDataInAdapter() {

        SessionsListAdapter mAdapter = new SessionsListAdapter(getActivity(), mList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }

}