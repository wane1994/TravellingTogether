package ru.travellingtogether.travellingtogether.fragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.travellingtogether.travellingtogether.MainActivity;
import ru.travellingtogether.travellingtogether.parsers.LoginJSONParser;
import ru.travellingtogether.travellingtogether.R;

public class FragmentUser extends android.app.Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // declaration of variables for fragment tvs
    TextView tvUserUsername, tvUserName, tvUserPhonenumber, navHeaderUsername, navHeaderName;

    // SharedPreferences to contain login session data
    SharedPreferences sPref;

    // string for username and user status called in other fragments
    public static String username = null;
    public static String userStatus = null;

    // strings for extractJSON()
    private static final String JSON_ARRAY ="result";
    private static final String JSON_USERNAME = "username";
    private static final String JSON_NAME= "name";
    private static final String JSON_SURNAME = "surname";
    private static final String JSON_PHONENUMBER = "phonenumber";

    public FragmentUser() {
        // Required empty public constructor
    }

    public static FragmentUser newInstance(String param1, String param2) {
        FragmentUser fragment = new FragmentUser();
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
        View v = inflater.inflate(R.layout.fragment_user, container, false);
        tvUserUsername = (TextView) v.findViewById(R.id.tvUserUsername);
        tvUserName = (TextView) v.findViewById(R.id.tvUserName);
        tvUserPhonenumber = (TextView) v.findViewById(R.id.tvUserPhonenumber);
        navHeaderUsername = (TextView)getActivity().findViewById(R.id.navHeaderUsername);
        navHeaderName = (TextView)getActivity().findViewById(R.id.navHeaderName);
        ImageView imgLogoUser = (ImageView) v.findViewById(R.id.imgLogoUser);
        imgLogoUser.setImageResource(R.drawable.car);


        // Driver button
        Button btnDriver = (Button) v.findViewById(R.id.driver);
        btnDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open FragmentDriver
                FragmentDriver fdriver = new FragmentDriver();
                FragmentTransaction ftDriver = getFragmentManager().beginTransaction();
                ftDriver.replace(R.id.container, fdriver);
                ftDriver.addToBackStack(null);
                ftDriver.commit();
            }
        });

        // Passenger button
        Button btnPassenger = (Button) v.findViewById(R.id.passenger);
        btnPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open FragmentPassenger
                FragmentPassenger fpassenger = new FragmentPassenger();
                FragmentTransaction ftPassenger = getFragmentManager().beginTransaction();
                ftPassenger.replace(R.id.container, fpassenger);
                ftPassenger.addToBackStack(null);
                ftPassenger.commit();
            }
        });

        return v;
    }

    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.TravellingTogether);

        // if not logged id, fill sPref with data from received json
        if (MainActivity.loggedMarker == null && MainActivity.regMarker == null && MainActivity.updMarker == null && MainActivity.jsonMarker == null) {
            extractJSON();
        }

        // read SharedPreferences data
        sPref = this.getActivity().getSharedPreferences("logdata", MainActivity.MODE_PRIVATE);
        String usernamePref = sPref.getString(MainActivity.USERNAME, "");
        String namePref = sPref.getString(MainActivity.NAME, "");
        String surnamePref = sPref.getString(MainActivity.SURNAME, "");
        String phonePref = sPref.getString(MainActivity.PHONENUMBER, "");

        // fill username, tvs and header elements with sPref data
        username = usernamePref;
        tvUserUsername.setText(usernamePref);
        tvUserName.setText(namePref + " " + surnamePref);
        tvUserPhonenumber.setText(phonePref);
        if (MainActivity.loggedMarker == null) {
            navHeaderName.setText(namePref + " " + surnamePref);
            navHeaderUsername.setText(usernamePref);
        }
    }

    // extracting json, getting data and inserting it sPref
    private void extractJSON(){
        try {
            JSONObject jsonResult = new JSONObject(LoginJSONParser.jsonResult);
            JSONArray userinfo = jsonResult.getJSONArray(JSON_ARRAY);
            JSONObject jsonObject = userinfo.getJSONObject(0);

            sPref = getActivity().getSharedPreferences("logdata", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString(MainActivity.USERNAME, jsonObject.getString(JSON_USERNAME));
            ed.putString(MainActivity.NAME, jsonObject.getString(JSON_NAME));
            ed.putString(MainActivity.SURNAME, jsonObject.getString(JSON_SURNAME));
            ed.putString(MainActivity.PHONENUMBER, jsonObject.getString(JSON_PHONENUMBER));
            ed.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
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