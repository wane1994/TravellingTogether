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
import ru.travellingtogether.travellingtogether.parsers.RegisterBW;

public class FragmentRegister extends android.app.Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // declaration of variables for fragment ets
    EditText etRegUsername, etRegPassword, etRegName, etRegSurname, etRegPhone;

    // declaration of strings for register stage
    String username, password, name, surname, phonenumber;

    // SharedPreferences to contain login session data
    SharedPreferences sPref;

    public FragmentRegister() {
        // Required empty public constructor
    }

    public static FragmentRegister newInstance(String param1, String param2) {
        FragmentRegister fragment = new FragmentRegister();
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
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        etRegUsername = (EditText) v.findViewById(R.id.etRegUsername);
        etRegPassword = (EditText) v.findViewById(R.id.etRegPassword);
        etRegName = (EditText) v.findViewById(R.id.etRegName);
        etRegSurname = (EditText) v.findViewById(R.id.etRegSurname);
        etRegPhone = (EditText) v.findViewById(R.id.etRegPhone);
        ImageView imgLogoReg = (ImageView) v.findViewById(R.id.imgLogoReg);
        imgLogoReg.setImageResource(R.drawable.car);

        // Register button
        Button btnRegRegister = (Button) v.findViewById(R.id.regRegister);
        btnRegRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = etRegUsername.getText().toString();
                password = etRegPassword.getText().toString();
                name = etRegName.getText().toString();
                surname = etRegSurname.getText().toString();
                phonenumber = etRegPhone.getText().toString();

                if (isOnline()) {
                    if (!username.equals("") && !password.equals("") && !name.equals("") && !surname.equals("") && !phonenumber.equals("")) {
                        // send user inserted data to database
                        RegisterBW registerBW = new RegisterBW(FragmentRegister.this, getActivity());
                        registerBW.execute(username, password, name, surname, phonenumber);
                    } else {
                        Toast.makeText(getActivity(), R.string.fillFields, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.TravellingTogether);
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
    public void registerBWPE() {
        MainActivity.regMarker = "regmarker";

        sPref = this.getActivity().getSharedPreferences("logdata", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(MainActivity.USERNAME, username);
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