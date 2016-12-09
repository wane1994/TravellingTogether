package ru.travellingtogether.travellingtogether.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ru.travellingtogether.travellingtogether.R;
import ru.travellingtogether.travellingtogether.parsers.AddCommentBW;
import ru.travellingtogether.travellingtogether.parsers.TripCommentsJSONParser;

public class FragmentTripDetails extends android.app.Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // arrayList to show list to show list of trips
    ArrayList<HashMap<String, String>> mlist = new ArrayList<HashMap<String,String>>();

    // declaration of variable for listView
    ListView list;

    // declaration of variable for tvs and et
    TextView detailsFrom, detailsTo, detailsDate, detailsStatus, detailsName, detailsPhone;
    EditText etAddComment;

    // string for a given comment
    String commenttoadd;

    // strings for extractJson()
    private JSONArray comments = null;
    private static final String JSON_ARRAY ="result";
    private static final String JSON_USERNAME ="username";
    private static final String JSON_COMMENT ="comment";

    public FragmentTripDetails() {
        // Required empty public constructor
    }

    public static FragmentTripDetails newInstance(String param1, String param2) {
        FragmentTripDetails fragment = new FragmentTripDetails();
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
        View v = inflater.inflate(R.layout.fragment_trip_details, container, false);
        list = (ListView) v.findViewById(R.id.listviewComments);
        detailsFrom = (TextView) v.findViewById(R.id.detailsFrom);
        detailsTo = (TextView) v.findViewById(R.id.detailsTo);
        detailsDate = (TextView) v.findViewById(R.id.detailsDate);
        detailsStatus = (TextView) v.findViewById(R.id.detailsStatus);
        detailsName = (TextView) v.findViewById(R.id.detailsName);
        detailsPhone = (TextView) v.findViewById(R.id.detailsPhone);
        etAddComment = (EditText) v.findViewById(R.id.etAddComment);

        // Add comment button
        Button btnAddComment = (Button) v.findViewById(R.id.btnAddComment);
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commenttoadd = etAddComment.getText().toString();

                if (!commenttoadd.equals("")) {
                    if (isOnline()) {
                        // send commend to database
                        AddCommentBW addCommentBW = new AddCommentBW(FragmentTripDetails.this, getActivity());
                        addCommentBW.execute(FragmentTripList.tripid, FragmentUser.username, commenttoadd);
                    } else {
                        Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                    }
                    hideKeyboard();
                } else {
                    Toast.makeText(getActivity(), R.string.emptyComment, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    public void onResume() {
        super.onResume();
        if (FragmentUser.userStatus.equals("driver")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.requestedTrip);
        }
        if (FragmentUser.userStatus.equals("passenger")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.forthcomingTrip);
        }

        // attaching values from FragmentTripList chosen item
        String date = FragmentTripList.day+"."+FragmentTripList.month+"."+FragmentTripList.year+", "+FragmentTripList.hour+":"+FragmentTripList.minute;
        String name = FragmentTripList.name+" "+FragmentTripList.surname+" ("+FragmentTripList.username+")";
        detailsFrom.setText(FragmentTripList.from);
        detailsTo.setText(FragmentTripList.to);
        detailsDate.setText(date);
        detailsName.setText(name);
        detailsPhone.setText(FragmentTripList.phone);

        if (FragmentUser.userStatus.equals("driver")){
            detailsStatus.setText(R.string.tripStatusPassenger);
        } else {
            detailsStatus.setText(R.string.tripStatusDriver);
        }

        // clear comments list and extractJson each time fragment resumed
        mlist.clear();
        extractCommentJSON();
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

    // hide keyboard on comment added
    private void hideKeyboard() {
        // Check if no view has focus
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // add new comment to a listView
    public void addcommentBWPE(){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(JSON_USERNAME, FragmentUser.username);
        map.put(JSON_COMMENT, commenttoadd);
        mlist.add(map);

        ListAdapter adapter = new SimpleAdapter(getActivity(), mlist, R.layout.fragment_trip_detail_row,
                new String[] {JSON_USERNAME, JSON_COMMENT},
                new int[]{R.id.tvCommentUsername, R.id.tvCommentComment,});
        list.setAdapter(adapter);
        list.setSelection(comments.length());

        etAddComment.setText("");
    }

    // extracting CommentsList json string to array
    private void extractCommentJSON(){
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(TripCommentsJSONParser.jsonResult);
            comments = jsonObject.getJSONArray(JSON_ARRAY);

            for(int i=0; i<comments.length(); i++) {
                JSONObject comment = comments.getJSONObject(i);
                String username = comment.getString(JSON_USERNAME);
                String comm = comment.getString(JSON_COMMENT);

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(JSON_USERNAME, username);
                map.put(JSON_COMMENT, comm);
                mlist.add(map);

                // attach comment to a list
                ListAdapter adapter = new SimpleAdapter(getActivity(), mlist, R.layout.fragment_trip_detail_row,
                        new String[] {JSON_USERNAME, JSON_COMMENT},
                        new int[]{R.id.tvCommentUsername, R.id.tvCommentComment,});
                list.setAdapter(adapter);
            }

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