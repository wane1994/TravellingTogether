package ru.travellingtogether.travellingtogether.fragments;

import android.Manifest;
import android.app.DatePickerDialog;

import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ru.travellingtogether.travellingtogether.R;
import ru.travellingtogether.travellingtogether.parsers.MyGeocoder;
import ru.travellingtogether.travellingtogether.parsers.TripCreateBW;
import ru.travellingtogether.travellingtogether.parsers.PlacesBW;

public class FragmentMap extends android.app.Fragment implements OnMapReadyCallback {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // variable for mapView
    GoogleMap googleMap;

    private static View v;

    // variables for markers and polyline
    final Marker[] markerFrom = {null};
    final Marker[] markerTo = {null};
    final LatLng[] latLngFrom = {null};
    final LatLng[] latLngTo = {null};
    final Polyline[] line = {null};

    // variables for fragment views
    EditText etDate, etTime;
    AutoCompleteTextView atvFrom, atvTo;

    // call for Google Places Api Web Service parser
    PlacesBW placesBW;

    // variables to send to database
    String tripDay = null, tripMonth = null, tripYear = null, tripHour = null, tripMinute = null;
    String tripFrom = null, tripTo = null;

    // helper variables to get text from AutoCompleteTextViews
    String locationFrom = null, locationTo = null;

    // helper variables for MyGeocoder proper work order
    int atvFromMarker = 0;
    int atvToMarker = 0;

    public FragmentMap() {
        // Required empty public constructor
    }

    public static FragmentMap newInstance(String param1, String param2) {
        FragmentMap fragment = new FragmentMap();
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
        if (v != null) {
            ViewGroup parent = (ViewGroup) v.getParent();
            if (parent != null)
                parent.removeView(v);
        }
        try {
            v = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }


        return v;
    }

    public void onResume() {
        super.onResume();
        if (FragmentUser.userStatus.equals("driver")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.createTripDriver);
        }
        if (FragmentUser.userStatus.equals("passenger")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.createTripPassenger);
        }

        // getting current date and time to show it in TimePicker and DatePickerDialogs
        final Calendar cal = Calendar.getInstance();
        final int currentYear = cal.get(Calendar.YEAR);
        final int currentMonth = cal.get(Calendar.MONTH);
        final int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        final int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        final int currentMinute = cal.get(Calendar.MINUTE);

        // attaching map to googleMap variable
        createMapView();

        // attaching ets to variables
        etDate = (EditText) v.findViewById(R.id.etDate);
        etTime = (EditText) v.findViewById(R.id.etTime);

        tripDay = null; tripMonth = null; tripYear = null; tripHour = null; tripMinute = null;
        tripFrom = null; tripTo = null;

        etDate.setText("");
        etTime.setText("");

        googleMap.clear();

        // attaching AutoCompleteTV to atvFrom variable, calling Google Places parser
        atvFrom = (AutoCompleteTextView) v.findViewById(R.id.atvFrom);
        atvFrom.setText("");
        atvFrom.setThreshold(2);
        atvFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesBW = new PlacesBW(atvFrom, v.getContext());
                placesBW.execute(s.toString());

                // erase locationFrom value, remove marker and polyline
                locationFrom = null;
                if (markerFrom[0] != null) {
                    markerFrom[0].remove();
                    markerFrom[0] = null;
                }
                if (line[0] != null) {
                    line[0].remove();
                    line[0] = null;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        // on item click: set value to tripFrom, create marker on map, polyline(optional)
        atvFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                locationFrom = atvFrom.getText().toString();
                List<Address> addressListFrom = null;
                Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
                atvFromMarker = 1;
                atvToMarker = 0;

                hideKeyboard();

                try {
                    if (markerFrom[0] != null) {
                        markerFrom[0].remove();
                    }
                    addressListFrom = geocoder.getFromLocationName(locationFrom, 1);

                    // check if Geocoder works or not (does not work on Meizu MX5), if yes - keep on
                    if (addressListFrom.size() > 0) {
                        Address addressFrom = addressListFrom.get(0);
                        latLngFrom[0] = new LatLng(addressFrom.getLatitude(), addressFrom.getLongitude());
                        tripFrom = addressListFrom.get(0).getFeatureName();
                        markerFrom[0] = googleMap.addMarker(new MarkerOptions().position(latLngFrom[0]).title("From"));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLngFrom[0]));

                        if (markerTo[0] != null) {
                            if (line[0] != null) {
                                line[0].remove();
                            }
                            line[0] = googleMap.addPolyline(new PolylineOptions().add(latLngFrom[0], latLngTo[0]).width(5).color(Color.BLUE));
                        }

                    } else {
                        if (isOnline()) {
                            // if Geocoder does not work, call for self-created one using Google Maps Geocoding API
                            MyGeocoder myGeocoder = new MyGeocoder(FragmentMap.this, getActivity());
                            myGeocoder.execute(locationFrom);
                        } else {
                            Toast.makeText(getActivity(), "Your internet connection is turned off", Toast.LENGTH_SHORT).show();
                        }

                    }

                } catch (IOException e) {
                    Toast.makeText(getActivity(), R.string.badInternet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // attaching AutoCompleteTV to atvTo variable, calling Google Places parser
        atvTo = (AutoCompleteTextView) v.findViewById(R.id.atvTo);
        atvTo.setText("");
        atvTo.setThreshold(2);
        atvTo.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesBW = new PlacesBW(atvTo, v.getContext());
                placesBW.execute(s.toString());

                // erase locationTo value, remove marker and polyline
                locationTo = null;
                if (markerTo[0] != null) {
                    markerTo[0].remove();
                    markerTo[0] = null;
                }
                if (line[0] != null) {
                    line[0].remove();
                    line[0] = null;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        // on item click: set value to tripTo, create marker on map, polyline(optional)
        atvTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                locationTo = atvTo.getText().toString();
                List<Address> addressListTo = null;
                Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
                atvFromMarker = 0;
                atvToMarker = 1;

                hideKeyboard();

                try {
                    if (markerTo[0] != null) {
                        markerTo[0].remove();
                    }
                    addressListTo = geocoder.getFromLocationName(locationTo, 1);

                    // check if Geocoder works or not (does not work on Meizu MX5), if yes - keep on
                    if (addressListTo.size() > 0) {
                        Address addressTo = addressListTo.get(0);
                        latLngTo[0] = new LatLng(addressTo.getLatitude(), addressTo.getLongitude());
                        tripTo = addressListTo.get(0).getFeatureName();
                        markerTo[0] = googleMap.addMarker(new MarkerOptions().position(latLngTo[0]).title("To").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLngTo[0]));

                        if (markerFrom[0] != null) {
                            if (line[0] != null) {
                                line[0].remove();
                            }
                            line[0] = googleMap.addPolyline(new PolylineOptions().add(latLngFrom[0], latLngTo[0]).width(5).color(Color.BLUE));
                        }

                    } else {
                        if (isOnline()) {
                            // if Geocoder does not work, call for self-created one using Google Maps Geocoding API
                            MyGeocoder myGeocoder = new MyGeocoder(FragmentMap.this, getActivity());
                            myGeocoder.execute(locationTo);
                        } else {
                            Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (IOException e) {
                    Toast.makeText(getActivity(), R.string.badInternet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // set date
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), datePickerListener, currentYear, currentMonth, currentDay);
                dialog.show();
            }
        });

        // set time
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), timePickerListener, currentHour, currentMinute, true);
                dialog.show();
            }
        });

        // button create
        Button btnCreate = (Button) v.findViewById(R.id.btnCreateTrip);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if From and To points are taken with AutoComplete places, send trip values to database
                if ((tripFrom != null) && (tripTo != null)) {

                    if (tripDay != null && tripMonth != null && tripYear != null && tripHour != null && tripMinute != null && tripFrom != null && tripTo != null) {

                        if (isOnline()) {
                            TripCreateBW tripCreateBW = new TripCreateBW(FragmentMap.this, getActivity());
                            tripCreateBW.execute(FragmentUser.userStatus, FragmentUser.username, tripFrom, tripTo, tripDay, tripMonth, tripYear, tripHour, tripMinute);
                        } else {
                            Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        // From and To points have values, but Date or Time not
                        Toast.makeText(getActivity(), R.string.fillDateTime, Toast.LENGTH_SHORT).show();
                    }
                }

                // if not, get tripFrom and tripTo values from TextViews
                else {
                    locationFrom = atvFrom.getText().toString();
                    locationTo = atvTo.getText().toString();

                    if (!locationFrom.equals("") && !locationTo.equals("")) {
                        List<Address> addressListFrom = null;
                        List<Address> addressListTo = null;
                        Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
                        try {
                            addressListFrom = geocoder.getFromLocationName(locationFrom, 1);
                            addressListTo = geocoder.getFromLocationName(locationTo, 1);

                            // check if Geocoder works or not (does not work on Meizu MX5), if yes - keep on
                            if ((addressListFrom.size() > 0) && (addressListTo.size() > 0)) {
                                Address addressFrom = addressListFrom.get(0);
                                latLngFrom[0] = new LatLng(addressFrom.getLatitude(), addressFrom.getLongitude());
                                tripFrom = addressListFrom.get(0).getFeatureName();

                                Address addressTo = addressListTo.get(0);
                                latLngTo[0] = new LatLng(addressTo.getLatitude(), addressTo.getLongitude());
                                tripTo = addressListTo.get(0).getFeatureName();

                                // when From, To, Date, Time has values, send it to database and create markers with polyline on map
                                if (tripDay != null && tripMonth != null && tripYear != null && tripHour != null && tripMinute != null && tripFrom != null && tripTo != null) {
                                    googleMap.addMarker(new MarkerOptions().position(latLngFrom[0]).title("From"));
                                    googleMap.addMarker(new MarkerOptions().position(latLngTo[0]).title("To").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLngFrom[0]));
                                    Polyline line = googleMap.addPolyline(new PolylineOptions().add(latLngFrom[0], latLngTo[0]).width(5).color(Color.BLUE));

                                    if (isOnline()) {
                                        TripCreateBW tripCreateBW = new TripCreateBW(FragmentMap.this, getActivity());
                                        tripCreateBW.execute(FragmentUser.userStatus, FragmentUser.username, tripFrom, tripTo, tripDay, tripMonth, tripYear, tripHour, tripMinute);
                                    } else {
                                        Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    // if some field is empty or contain wrong value
                                    Toast.makeText(getActivity(), R.string.fillFields, Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                // if Geocoder does not work, call for self-created one using Google Maps Geocoding API
                                atvFromMarker = 2;
                                atvToMarker = 0;
                                if (isOnline()) {
                                    MyGeocoder myGeocoderFrom = new MyGeocoder(FragmentMap.this, getActivity());
                                    myGeocoderFrom.execute(locationFrom);
                                } else {
                                    Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                                }

                            }

                        } catch (IOException e) {
                            Toast.makeText(getActivity(), R.string.badInternet, Toast.LENGTH_SHORT).show();
                        } catch (IndexOutOfBoundsException e) {
                            // if From or To views contain wrong values that impossible to find on map
                            Toast.makeText(getActivity(), R.string.noPlace, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // if From or To view is empty
                        Toast.makeText(getActivity(), R.string.fillFromTo, Toast.LENGTH_SHORT).show();
                    }
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

    // hide keyboard on item chosen from atv
    private void hideKeyboard() {
        // Check if no view has focus
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // open FragmentUser
    public void tripcreateBWPE() {
        FragmentUser fuser = new FragmentUser();
        FragmentManager fm = getFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction().replace(R.id.container, fuser).commit();
    }

    // Self-created Geocoder post execute method
    public void myGeocoderPE(String json) {
        try {
            JSONObject jObj = new JSONObject(MyGeocoder.jsonResult);
            String Status = jObj.getString("status");

            if (Status.equalsIgnoreCase("OK")) {
                // getting latitude, longtitude and city name from JSON reply
                Double lat = Double.valueOf(jObj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat"));
                Double lng = Double.valueOf(jObj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng"));
                String city = jObj.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(0).getString("long_name");

                // create marker and polyline(optional) for place chosen from AutoCompleteTextView "From"
                if (atvFromMarker == 1) {
                    tripFrom = city;
                    latLngFrom[0] = new LatLng(lat, lng);
                    markerFrom[0] = googleMap.addMarker(new MarkerOptions().position(latLngFrom[0]).title("From"));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLngFrom[0]));

                    if (markerTo[0] != null) {
                        if (line[0] != null) {
                            line[0].remove();
                        }
                        line[0] = googleMap.addPolyline(new PolylineOptions().add(latLngFrom[0], latLngTo[0]).width(5).color(Color.BLUE));
                    }
                }

                // create marker and polyline(optional) for place chosen from AutoCompleteTextView "To"
                if (atvToMarker == 1) {
                    tripTo = city;
                    latLngTo[0] = new LatLng(lat, lng);
                    markerTo[0] = googleMap.addMarker(new MarkerOptions().position(latLngTo[0]).title("To").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLngTo[0]));

                    if (markerFrom[0] != null) {
                        if (line[0] != null) {
                            line[0].remove();
                        }
                        line[0] = googleMap.addPolyline(new PolylineOptions().add(latLngFrom[0], latLngTo[0]).width(5).color(Color.BLUE));
                    }
                }

                // create marker and polyline for place given in TextViews
                if (atvToMarker == 2) {
                    tripTo = city;
                    latLngTo[0] = new LatLng(lat, lng);

                    // if all fields filled correctly, send data to database
                    if (tripDay != null && tripMonth != null && tripYear != null && tripHour != null && tripMinute != null && tripFrom != null && tripTo != null) {
                        googleMap.addMarker(new MarkerOptions().position(latLngFrom[0]).title("From"));
                        googleMap.addMarker(new MarkerOptions().position(latLngTo[0]).title("To").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLngFrom[0]));
                        Polyline line = googleMap.addPolyline(new PolylineOptions().add(latLngFrom[0], latLngTo[0]).width(5).color(Color.BLUE));

                        TripCreateBW tripCreateBW = new TripCreateBW(FragmentMap.this, getActivity());
                        tripCreateBW.execute(FragmentUser.userStatus, FragmentUser.username, tripFrom, tripTo, tripDay, tripMonth, tripYear, tripHour, tripMinute);

                    } else {
                        // if some field is empty or contain wrong value
                        Toast.makeText(getActivity(), R.string.fillFields, Toast.LENGTH_SHORT).show();
                    }
                }

                // get data for "From marker", call for self-created Geocoder using Google Maps Geocoding API
                if (atvFromMarker == 2) {
                    tripFrom = city;
                    latLngFrom[0] = new LatLng(lat, lng);
                    atvFromMarker = 0;
                    atvToMarker = 2;

                    MyGeocoder myGeocoderTo = new MyGeocoder(FragmentMap.this, getActivity());
                    myGeocoderTo.execute(locationTo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // set value to tripYear, tripMonth, tripDay
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {

            tripYear = String.valueOf(selectedYear);
            if (selectedMonth < 9) {
                tripMonth = 0 + String.valueOf(selectedMonth + 1);
            } else {
                tripMonth = String.valueOf(selectedMonth + 1);
            }

            if (selectedDay < 10) {
                tripDay = 0 + String.valueOf(selectedDay);
            } else {
                tripDay = String.valueOf(selectedDay);
            }

            etDate.setText(tripDay + "." + tripMonth + "." + tripYear);
        }
    };

    // set value to tripHour, tripMinute
    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {

        // when dialog box is closed, below method will be called.
        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
            if (selectedHour < 10) {
                tripHour = 0 + String.valueOf(selectedHour);
            } else {
                tripHour = String.valueOf(selectedHour);
            }

            if (selectedMinute < 10) {
                tripMinute = 0 + String.valueOf(selectedMinute);
            } else {
                tripMinute = String.valueOf(selectedMinute);
            }

            etTime.setText(tripHour + ":" + tripMinute);
        }
    };

    // method to attach map to variable
    private void createMapView() {
        try {
            if (null == googleMap) {
                try {
                    googleMap = ((MapFragment) getChildFragmentManager().findFragmentById(R.id.mapView)).getMap();
                } catch (NullPointerException exception) {
                    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
                    mapFragment.getMapAsync(this);
                    googleMap = mapFragment.getMap();
                }

                if (null == googleMap) {
                    Toast.makeText(getActivity().getApplicationContext(), "Error creating map", Toast.LENGTH_SHORT).show();
                }

                // enabling current location
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);

                // zoom map on Rovaniemi
                LatLng userLocation = new LatLng(66.50394779999999, 25.7293905);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 6));
            }

        } catch (NullPointerException exception) {
            Log.e("mapApp", exception.toString());
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}