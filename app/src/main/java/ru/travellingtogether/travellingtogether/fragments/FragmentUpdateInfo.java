package ru.travellingtogether.travellingtogether.fragments;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import ru.travellingtogether.travellingtogether.MainActivity;
import ru.travellingtogether.travellingtogether.R;
import ru.travellingtogether.travellingtogether.parsers.UpdateBW;

public class FragmentUpdateInfo extends android.app.Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // declaration of variables for fragment ets
    EditText etUpdUsername, etUpdOldPassword, etUpdNewPassword, etUpdName, etUpdSurname, etUpdPhone;

    // declaration of strings for updateInfo stage
    String oldPassword, newPassword, name, surname, phonenumber;

    // SharedPreferences to contain login session data
    SharedPreferences sPref;

    public FragmentUpdateInfo() {
        // Required empty public constructor
    }

    public static FragmentUpdateInfo newInstance(String param1, String param2) {
        FragmentUpdateInfo fragment = new FragmentUpdateInfo();
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
        View v = inflater.inflate(R.layout.fragment_update_info, container, false);
        etUpdUsername = (EditText) v.findViewById(R.id.etUpdUsername);
        etUpdOldPassword = (EditText) v.findViewById(R.id.etUpdOldPassword);
        etUpdNewPassword = (EditText) v.findViewById(R.id.etUpdNewPassword);
        etUpdName = (EditText) v.findViewById(R.id.etUpdName);
        etUpdSurname = (EditText) v.findViewById(R.id.etUpdSurname);
        etUpdPhone = (EditText) v.findViewById(R.id.etUpdPhone);
        ImageView imgLogoUpd = (ImageView) v.findViewById(R.id.imgLogoUpd);
        imgLogoUpd.setImageResource(R.drawable.car);

        // read SharedPreferences data and set it to ets
        sPref = this.getActivity().getSharedPreferences("logdata", MainActivity.MODE_PRIVATE);
        String usernamePref = sPref.getString(MainActivity.USERNAME, "");
        String namePref = sPref.getString(MainActivity.NAME, "");
        String surnamePref = sPref.getString(MainActivity.SURNAME, "");
        String phonePref = sPref.getString(MainActivity.PHONENUMBER, "");
        etUpdUsername.setText(usernamePref);
        etUpdName.setText(namePref);
        etUpdSurname.setText(surnamePref);
        etUpdPhone.setText(phonePref);

        // username change notification
        etUpdUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), R.string.usernameChange, Toast.LENGTH_SHORT).show();
            }
        });

        // Update button
        Button btnUpdate = (Button) v.findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPassword = etUpdOldPassword.getText().toString();
                newPassword = etUpdNewPassword.getText().toString();
                name = etUpdName.getText().toString();
                surname = etUpdSurname.getText().toString();
                phonenumber = etUpdPhone.getText().toString();

                if (!oldPassword.equals("") && !newPassword.equals("") && !name.equals("") && !surname.equals("") && !phonenumber.equals("")) {
                    if (isOnline()) {
                        // send data to update to database
                        UpdateBW updateBW = new UpdateBW(FragmentUpdateInfo.this, getActivity());
                        updateBW.execute(FragmentUser.username, oldPassword, newPassword, name, surname, phonenumber);
                    } else {
                        Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.fillFields, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.updInfo);
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

    // open FragmentUser, fill sPref with inserted data
    public void updateBWPE() {
        MainActivity.updMarker = "update";
        MainActivity.loggedMarker = null;

        sPref = this.getActivity().getSharedPreferences("logdata", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(MainActivity.NAME, name);
        ed.putString(MainActivity.SURNAME, surname);
        ed.putString(MainActivity.PHONENUMBER, phonenumber);
        ed.commit();

        FragmentUser fuser = new FragmentUser();
        FragmentManager fm = getFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction().replace(R.id.container, fuser).commit();
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