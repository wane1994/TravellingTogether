package ru.travellingtogether.travellingtogether.fragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import ru.travellingtogether.travellingtogether.R;
import ru.travellingtogether.travellingtogether.parsers.TripPassengerJSONParser;

public class FragmentDriver extends android.app.Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FragmentDriver() {
        // Required empty public constructor
    }

    public static FragmentDriver newInstance(String param1, String param2) {
        FragmentDriver fragment = new FragmentDriver();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_driver, container, false);
        ImageView imgDriver = (ImageView) v.findViewById(R.id.imgDriver);
        imgDriver.setImageResource(R.drawable.driverimg);

        // "Create a trip" button
        Button btnDriverTrip = (Button) v.findViewById(R.id.btnDriverCreate);
        btnDriverTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open FragmentMap
                FragmentUser.userStatus = "driver";
                FragmentMap fmap = new FragmentMap();
                FragmentTransaction ftMap = getFragmentManager().beginTransaction();
                ftMap.replace(R.id.container, fmap);
                ftMap.addToBackStack(null);
                ftMap.commit();
            }
        });

        // Requested trip list button
        Button btnPassengerList = (Button) v.findViewById(R.id.btnPassengerList);
        btnPassengerList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    // get from database list of trips created by passenger
                    FragmentUser.userStatus = "driver";
                    TripPassengerJSONParser tripPassengerJSONParser = new TripPassengerJSONParser(FragmentDriver.this, getActivity());
                    tripPassengerJSONParser.execute(FragmentUser.userStatus);
                } else {
                    Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.driver);
    }

    // internet connection state check
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    // open FragmentTrips
    public void passengerJSONPE(String json) {
        FragmentTripList ftlist = new FragmentTripList();
        FragmentTransaction ftrans = getFragmentManager().beginTransaction();
        ftrans.replace(R.id.container, ftlist);
        ftrans.addToBackStack(null);
        ftrans.commit();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}