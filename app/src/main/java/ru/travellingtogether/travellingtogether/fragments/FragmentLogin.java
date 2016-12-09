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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import ru.travellingtogether.travellingtogether.MainActivity;
import ru.travellingtogether.travellingtogether.parsers.LoginBW;
import ru.travellingtogether.travellingtogether.parsers.LoginJSONParser;
import ru.travellingtogether.travellingtogether.R;

public class FragmentLogin extends android.app.Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // declaration of variables for fragment ets
    EditText etLogUsername, etLogPassword;

    // declaration of strings for login stage
    String username, password;

    public FragmentLogin() {
        // Required empty public constructor
    }

    public static FragmentLogin newInstance(String param1, String param2) {
        FragmentLogin fragment = new FragmentLogin();
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
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        etLogUsername = (EditText) v.findViewById(R.id.etLogUsername);
        etLogPassword = (EditText) v.findViewById(R.id.etLogPassword);
        ImageView imgLogo = (ImageView) v.findViewById(R.id.imgLogo);
        imgLogo.setImageResource(R.drawable.car);

        // Register button
        Button btnLogRegister = (Button) v.findViewById(R.id.logRegister);
        btnLogRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open FragmentRegister
                FragmentRegister fregister = new FragmentRegister();
                FragmentTransaction ftRegister = getFragmentManager().beginTransaction();
                ftRegister.replace(R.id.container, fregister);
                ftRegister.addToBackStack(null);
                ftRegister.commit();
            }
        });

        // Login button
        Button btnLogin = (Button) v.findViewById(R.id.login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = etLogUsername.getText().toString();
                password = etLogPassword.getText().toString();

                if (isOnline()) {
                    // send username:password to database and check is it correct
                    LoginBW loginBW = new LoginBW(FragmentLogin.this, getActivity());
                    loginBW.execute(username, password);
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.TravellingTogether);
        etLogUsername.setText("");
        etLogPassword.setText("");
        MainActivity.jsonMarker = null;
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

    // if username:password are correct, get user data in json format
    public void loginBWPE() {
        LoginJSONParser loginJSONParser = new LoginJSONParser(FragmentLogin.this);
        loginJSONParser.execute(username, password);
    }

    // open FragmentUser
    public void loginJSONPE(String json) {
        FragmentUser fuser = new FragmentUser();
        FragmentTransaction ftUser = getFragmentManager().beginTransaction();
        ftUser.replace(R.id.container, fuser);
        ftUser.commit();
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