package ru.travellingtogether.travellingtogether.fragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import ru.travellingtogether.travellingtogether.R;
import ru.travellingtogether.travellingtogether.parsers.TripCommentsJSONParser;
import ru.travellingtogether.travellingtogether.parsers.TripCreatorJSONParser;
import ru.travellingtogether.travellingtogether.parsers.TripDriverJSONParser;
import ru.travellingtogether.travellingtogether.parsers.TripPassengerJSONParser;

public class FragmentTripList extends android.app.Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // declaration of variable for listView
    ListView list;

    // arrayList to show list to show list of trips
    ArrayList<HashMap<String, String>> mlist = new ArrayList<HashMap<String,String>>();

    // declaration of variable for button
    Button filterTrips;

    // dialogFilter class declaration
    DialogFilter df;

    // strings for a single trip
    public static String username, from, to, day, month, year, hour, minute, name, surname, phone, tripid;

    // strings for extractJson()
    private JSONArray trips = null;
    private static final String JSON_ARRAY ="result";
    private static final String JSON_TRIPID ="tripid";
    private static final String JSON_USERNAME ="username";
    private static final String JSON_FROM = "from";
    private static final String JSON_TO= "to";
    private static final String JSON_DAY = "day";
    private static final String JSON_MONTH = "month";
    private static final String JSON_YEAR = "year";
    private static final String JSON_HOUR = "hour";
    private static final String JSON_MINUTE = "minute";
    private static final String JSON_NAME = "name";
    private static final String JSON_SURNAME = "surname";
    private static final String JSON_PHONENUMBER = "phonenumber";

    public FragmentTripList() {
        // Required empty public constructor
    }

    public static FragmentTripList newInstance(String param1, String param2) {
        FragmentTripList fragment = new FragmentTripList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FragmentUser.userStatus.equals("driver")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.driverTrips);
        }
        if (FragmentUser.userStatus.equals("passenger")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.passengerTrips);
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trip_list, container, false);
        list = (ListView) v.findViewById(R.id.listviewTrip);
        df = new DialogFilter(FragmentTripList.this);

        // Filter trips button
        filterTrips = (Button) v.findViewById(R.id.filterTrips);
        filterTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show TripFilter dialog
                df.show(getFragmentManager(), "df");
            }
        });

        return v;
    }

    public void onResume() {
        super.onResume();
        if (FragmentUser.userStatus.equals("driver")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Passenger requested trips");
        }
        if (FragmentUser.userStatus.equals("passenger")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Trips created by drivers");
        }

        // clear list and extractJson each time fragment resumed
        mlist.clear();
        extractJSON();

        // click on a trip from a list
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get trip data
                tripid = mlist.get(position).get(JSON_TRIPID);
                username = mlist.get(position).get(JSON_USERNAME);
                from = mlist.get(position).get(JSON_FROM);
                to = mlist.get(position).get(JSON_TO);
                day = mlist.get(position).get(JSON_DAY);
                month = mlist.get(position).get(JSON_MONTH);
                year = mlist.get(position).get(JSON_YEAR);
                hour = mlist.get(position).get(JSON_HOUR);
                minute = mlist.get(position).get(JSON_MINUTE);

                if (isOnline()) {
                    // get from database info about trip creator
                    TripCreatorJSONParser tripCreatorJSONParser = new TripCreatorJSONParser(FragmentTripList.this);
                    tripCreatorJSONParser.execute(username);
                } else {
                    Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    // get from database comments for the chosen trip
    public void creatorJSONPE(){
        extractTripCreatorJSON();
        TripCommentsJSONParser tripCommentsJSONParser = new TripCommentsJSONParser(FragmentTripList.this);
        tripCommentsJSONParser.execute(tripid);
    }

    // run FragmentTripDetails
    public void tripCommmentsJSONPE() {
        FragmentTripDetails ftd = new FragmentTripDetails();
        FragmentTransaction ftrans = getFragmentManager().beginTransaction();
        ftrans.replace(R.id.container, ftd);
        ftrans.addToBackStack(null);
        ftrans.commit();
    }

    // extract TripCreator json
    private void extractTripCreatorJSON(){
        try {
            JSONObject phoneJsonObject = new JSONObject(TripCreatorJSONParser.jsonResult);
            JSONArray jsonArray = phoneJsonObject.getJSONArray(JSON_ARRAY);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            name = jsonObject.getString(JSON_NAME);
            surname = jsonObject.getString(JSON_SURNAME);
            phone = jsonObject.getString(JSON_PHONENUMBER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // extracting TripList json string to array
    private void extractJSON(){
        JSONObject jsonObject;
        try {
            if (FragmentUser.userStatus.equals("driver")) {
                jsonObject = new JSONObject(TripPassengerJSONParser.jsonResult);
                trips = jsonObject.getJSONArray(JSON_ARRAY);
            }
            if (FragmentUser.userStatus.equals("passenger")) {
                jsonObject = new JSONObject(TripDriverJSONParser.jsonResult);
                trips  = jsonObject.getJSONArray(JSON_ARRAY);
            }

            for(int i=0; i<trips.length(); i++) {
                JSONObject trip = trips.getJSONObject(i);
                String tripid = trip.getString(JSON_TRIPID);
                String username = trip.getString(JSON_USERNAME);
                String from = trip.getString(JSON_FROM);
                String to = trip.getString(JSON_TO);
                String day = trip.getString(JSON_DAY);
                String month = trip.getString(JSON_MONTH);
                String year = trip.getString(JSON_YEAR);
                String hour = trip.getString(JSON_HOUR);
                String minute = trip.getString(JSON_MINUTE);

                // if current date is more than date of a trip, skip it
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                Date date = sdf.parse(year+month+day+hour+minute);
                Date currentdate = new Date();
                if (date.after(currentdate)) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(JSON_TRIPID, tripid);
                    map.put(JSON_USERNAME, username);
                    map.put(JSON_FROM, from);
                    map.put(JSON_TO, to);
                    map.put(JSON_DAY, day);
                    map.put(JSON_MONTH, month);
                    map.put(JSON_YEAR, year);
                    map.put(JSON_HOUR, hour);
                    map.put(JSON_MINUTE, minute);
                    mlist.add(map);

                    // attach trip to a TripList
                    ListAdapter adapter = new SimpleAdapter(getActivity(), mlist, R.layout.fragment_trip_list_row,
                            new String[]{JSON_FROM, JSON_TO, JSON_DAY, JSON_MONTH, JSON_YEAR, JSON_HOUR, JSON_MINUTE},
                            new int[]{R.id.rowFrom, R.id.rowTo, R.id.rowDay, R.id.rowMonth, R.id.rowYear, R.id.rowHour, R.id.rowMinute,});
                    list.setAdapter(adapter);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // filtering TripList: extracting json and filtering it according to given places names
    public void updateList(String filterFrom, String filterTo) {
        // clear current list
        mlist.clear();
        boolean a = !filterFrom.equals("");
        boolean b = !filterTo.equals("");
        int count = 0;

        try {
            for(int i=0; i<trips.length(); i++) {
                JSONObject trip = trips.getJSONObject(i);
                String tripid = trip.getString(JSON_TRIPID);
                String username = trip.getString(JSON_USERNAME);
                String from = trip.getString(JSON_FROM);
                String to = trip.getString(JSON_TO);
                String day = trip.getString(JSON_DAY);
                String month = trip.getString(JSON_MONTH);
                String year = trip.getString(JSON_YEAR);
                String hour = trip.getString(JSON_HOUR);
                String minute = trip.getString(JSON_MINUTE);

                // if current date is more than date of a trip, skip it
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                Date date = sdf.parse(year+month+day+hour+minute);
                Date currentdate = new Date();
                if (date.after(currentdate)) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(JSON_TRIPID, tripid);
                    map.put(JSON_USERNAME, username);
                    map.put(JSON_FROM, from);
                    map.put(JSON_TO, to);
                    map.put(JSON_DAY, day);
                    map.put(JSON_MONTH, month);
                    map.put(JSON_YEAR, year);
                    map.put(JSON_HOUR, hour);
                    map.put(JSON_MINUTE, minute);

                    if (a && b) {
                        // if user entered both From ant To places name
                        if (from.equals(filterFrom) && to.equals(filterTo)) {
                            // attach trip to a TripList
                            mlist.add(map);
                            ListAdapter adapter = new SimpleAdapter(getActivity(), mlist, R.layout.fragment_trip_list_row,
                                    new String[]{JSON_FROM, JSON_TO, JSON_DAY, JSON_MONTH, JSON_YEAR, JSON_HOUR, JSON_MINUTE},
                                    new int[]{R.id.rowFrom, R.id.rowTo, R.id.rowDay, R.id.rowMonth, R.id.rowYear, R.id.rowHour, R.id.rowMinute,});
                            list.setAdapter(adapter);
                            count++;
                        }
                    } else {
                        // if user entered only one place name
                        if (from.equals(filterFrom) || to.equals(filterTo)) {
                            // attach trip to a TripList
                            mlist.add(map);
                            ListAdapter adapter = new SimpleAdapter(getActivity(), mlist, R.layout.fragment_trip_list_row,
                                    new String[]{JSON_FROM, JSON_TO, JSON_DAY, JSON_MONTH, JSON_YEAR, JSON_HOUR, JSON_MINUTE},
                                    new int[]{R.id.rowFrom, R.id.rowTo, R.id.rowDay, R.id.rowMonth, R.id.rowYear, R.id.rowHour, R.id.rowMinute,});
                            list.setAdapter(adapter);
                            count++;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // if nothing filtered found
        if (count==0) {Toast.makeText(getActivity(), R.string.noTrips, Toast.LENGTH_SHORT).show();}
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