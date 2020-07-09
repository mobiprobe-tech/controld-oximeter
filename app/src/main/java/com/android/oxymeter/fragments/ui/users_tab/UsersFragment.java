package com.android.oxymeter.fragments.ui.users_tab;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.oxymeter.R;
import com.android.oxymeter.activities.AddUserActivity;
import com.android.oxymeter.activities.ProfileDetailsActivity;
import com.android.oxymeter.adapters.UsersListAdapter;
import com.android.oxymeter.room_db.Users.UserTable;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class UsersFragment extends Fragment implements View.OnClickListener {

    private UsersViewModel usersViewModel;
    private TextView mAddUserLabel;
    private ListView mUsersListView;
    private List<UserTable> mUsersList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usersViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(UsersViewModel.class);


        setHasOptionsMenu(true);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUsersListView = view.findViewById(R.id.listView);

        mAddUserLabel = view.findViewById(R.id.add_user_label);

        FloatingActionButton mFab = view.findViewById(R.id.fab);

        mFab.setOnClickListener(this);

        mUsersListView.setOnItemClickListener((parent, view1, position, id) -> {

            if (CommonUtils.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {

                Intent intent = new Intent(getActivity(), ProfileDetailsActivity.class);
                intent.putExtra(Constants.EXTRAS_SELECTED_USER_ID, mUsersList.get(position).getmUserID());
                intent.putExtra(Constants.EXTRAS_TITLE, getResources().getString(R.string.user_profile));
                startActivity(intent);

            } else {
                CommonUtils.showLongToast(getActivity(), getActivity().getResources().getString(R.string.no_internet));
            }

        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Add an observer on the LiveData returned by getAllSubUsers.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        usersViewModel.getAllSubUsers().observe(getViewLifecycleOwner(), users -> {

            if (mUsersList == null) {
                mUsersList = new ArrayList<>();
            }

            mUsersList.clear();

            if (users.size() > 0) {

                mUsersList.addAll(users);

                setDataInAdapter();

                mUsersListView.setVisibility(View.VISIBLE);
                mAddUserLabel.setVisibility(View.GONE);
            } else {
                mUsersListView.setVisibility(View.GONE);
                mAddUserLabel.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_disconnect).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {

            if (CommonUtils.isNetworkAvailable(Objects.requireNonNull(getActivity()))) {

                Intent intent = new Intent(getActivity(), AddUserActivity.class);
                startActivity(intent);

            } else {
                CommonUtils.showLongToast(getActivity(), getActivity().getResources().getString(R.string.no_internet));
            }

        }
    }

    private void setDataInAdapter() {

        UsersListAdapter mAdapter = new UsersListAdapter(getActivity(), mUsersList);
        mUsersListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }
}