package com.android.oxymeter.fragments.ui.data_tab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.oxymeter.R;
import com.android.oxymeter.ble.WaveformView;

import java.util.Objects;


public class DataFragment extends Fragment implements View.OnClickListener {

    private DataViewModel dataViewModel;
    private AppCompatTextView mSpo2MeasurementTextView, mPulseRateTextView, mPiTextView;
    private WaveformView wfvPleth;
    private AppCompatImageView mPlayBtn, mStopBtn;
    private Animation animation;
    private LinearLayoutCompat mStopLayout;
    private AppCompatTextView mTimerTextView;

    private OnDataFragmentInteractionListener mListener;

    public DataFragment() {
        //empty constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnDataFragmentInteractionListener) {
            mListener = (OnDataFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(DataViewModel.class);

        prepareAnimation();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPlayBtn = view.findViewById(R.id.playImageView);
        mStopBtn = view.findViewById(R.id.stopImageView);

        mStopLayout = view.findViewById(R.id.stopLayout);
        mTimerTextView = view.findViewById(R.id.timerTextView);

        mPlayBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);

        mSpo2MeasurementTextView = view.findViewById(R.id.textView_spo2);
        mPulseRateTextView = view.findViewById(R.id.textView_pulse);
        mPiTextView = view.findViewById(R.id.textView_pi);

        wfvPleth = view.findViewById(R.id.wfvPleth);

        mPlayBtn.setVisibility(View.VISIBLE);
        mStopLayout.setVisibility(View.GONE);

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dataViewModel.getSpO2().observe(getViewLifecycleOwner(), integer -> {

            if (integer != null) {
                if (integer > 0 && integer < 127) {
                    mSpo2MeasurementTextView.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.spo2_data, integer));
                }
            } else {
                mSpo2MeasurementTextView.setText("");
            }


        });

        dataViewModel.getPulseRate().observe(getViewLifecycleOwner(), integer -> {

            if (integer != null) {

                if (integer > 0 && integer < 255) {
                    mPulseRateTextView.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.pulse_data, integer));
                }
            } else {
                mPulseRateTextView.setText("");
            }

        });

        dataViewModel.getPi().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {

                if (integer > 0 && integer < 21) {
                    mPiTextView.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.pi_data, integer));

                }
            } else {
                mPiTextView.setText("");
            }
        });

        dataViewModel.getAmp().observe(getViewLifecycleOwner(), integer -> {

            if (integer == null) {
                try {
                    wfvPleth.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                    //do nothing
                }
            } else {
                wfvPleth.addAmp(integer);
            }
        });

        dataViewModel.getTimeElapsed().observe(getViewLifecycleOwner(), s -> {

            if (s != null) {
                mTimerTextView.setText(s);
            } else {
                mTimerTextView.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.default_timer));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.playImageView:
                mListener.onStartSession();
                break;

            case R.id.stopImageView:
                mListener.onStopSession();
                break;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDataFragmentInteractionListener {
        void onStartSession();

        void onStopSession();
    }

    public void setButtonVisibility(boolean isRecording) {

        if (isRecording) {
            mPlayBtn.setVisibility(View.GONE);
            mStopLayout.setVisibility(View.VISIBLE);
            mStopBtn.startAnimation(animation);


        } else {

            mStopBtn.clearAnimation();

           /* animation.cancel();
            animation.reset();*/

            mPlayBtn.setVisibility(View.VISIBLE);
            mStopLayout.setVisibility(View.GONE);
            mTimerTextView.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.default_timer));
        }
    }

    private void prepareAnimation() {
        animation = new AlphaAnimation((float) 1.0, 0); // Change alpha from fully visible to invisible
        animation.setDuration(400); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter
        // animation
        // rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation
        // infinitely
        animation.setRepeatMode(Animation.REVERSE);
    }
}