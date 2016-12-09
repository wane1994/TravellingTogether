package ru.travellingtogether.travellingtogether;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import ru.travellingtogether.travellingtogether.fragments.FragmentDriver;
import ru.travellingtogether.travellingtogether.fragments.FragmentInfo;
import ru.travellingtogether.travellingtogether.fragments.FragmentLogin;
import ru.travellingtogether.travellingtogether.fragments.FragmentPassenger;
import ru.travellingtogether.travellingtogether.fragments.FragmentUpdateInfo;
import ru.travellingtogether.travellingtogether.fragments.FragmentUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // fragments used in MainActivity and NavigationDrawer
    FragmentDriver fdriver;
    FragmentPassenger fpassenger;
    FragmentInfo finfo;
    FragmentLogin flogin;
    FragmentUser fuser;
    FragmentUpdateInfo fupdinfo;

    // variables to set values and listener to NavigationHeader
    TextView navHeaderName, navHeaderUsername;
    LinearLayout headerLL;

    // SharedPreferences to contain login session data
    SharedPreferences sPref;

    // strings for SharedPreferences
    public final static String USERNAME = "username";
    public final static String NAME = "name";
    public final static String SURNAME = "surname";
    public final static String PHONENUMBER = "phonenumber";
    public static String usernamePref, namePref, surnamePref, phonePref;

    // marker strings for login sessions
    public static String loggedMarker = null;
    public static String regMarker = null;
    public static String updMarker = null;
    public static String jsonMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        navHeaderName = (TextView) headerView.findViewById(R.id.navHeaderName);
        navHeaderUsername = (TextView) headerView.findViewById(R.id.navHeaderUsername);
        headerLL = (LinearLayout) headerView.findViewById(R.id.headerLL);
        headerLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open FragmentUser if logged in
                DrawerLayout drawerTT = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerTT.closeDrawer(GravityCompat.START);
                if (FragmentUser.username!=null) {
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fm.beginTransaction().replace(R.id.container, fuser).commit();
                } else {
                    Toast.makeText(MainActivity.this, R.string.logFirst, Toast.LENGTH_SHORT).show();
                }
            }
        });

        fdriver = new FragmentDriver();
        fpassenger = new FragmentPassenger();
        finfo = new FragmentInfo();
        flogin = new FragmentLogin();
        fuser = new FragmentUser();
        fupdinfo = new FragmentUpdateInfo();

        // read SharedPreferences data
        sPref = getSharedPreferences("logdata", MODE_PRIVATE);
        usernamePref = sPref.getString(USERNAME, "0");
        namePref = sPref.getString(NAME, "0");
        surnamePref = sPref.getString(SURNAME, "0");
        phonePref = sPref.getString(PHONENUMBER, "0");

        // if it contains any, open FragmentUser
        if (!usernamePref.equals("0") && !namePref.equals("0") && !surnamePref.equals("0") && !phonePref.equals("0")) {
            loggedMarker = "logged";
            regMarker = null;
            updMarker = null;
            navHeaderName.setText(namePref+" "+surnamePref);
            navHeaderUsername.setText(usernamePref);
            FragmentTransaction ftUser = getFragmentManager().beginTransaction();
            ftUser.replace(R.id.container, fuser);
            ftUser.commit();
        } else {
            FragmentTransaction ftLogin = getFragmentManager().beginTransaction();
            ftLogin.replace(R.id.container, flogin);
            ftLogin.commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 0 ){
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // menu items
        switch (id) {
            case R.id.action_updateinfo:
                // open FragmentUpdateInfo
                if (FragmentUser.username!=null){
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fm.beginTransaction().replace(R.id.container, fupdinfo).addToBackStack(null).commit();
                } else {
                    Toast.makeText(this, R.string.notLogged, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_logout:
                // clear sPref data, open FragmentLogin
                if (FragmentUser.username!=null){
                    FragmentUser.username=null;
                    loggedMarker = null;
                    regMarker = null;
                    updMarker = null;
                    jsonMarker = "marker";
                    sPref = getSharedPreferences("logdata", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString(USERNAME, "0");
                    ed.putString(NAME, "0");
                    ed.putString(SURNAME, "0");
                    ed.putString(PHONENUMBER, "0");
                    ed.commit();

                    navHeaderName.setText(R.string.TravellingTogether);
                    navHeaderUsername.setText(R.string.ttFind);

                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fm.beginTransaction().replace(R.id.container, flogin).commit();
                    Toast.makeText(this, R.string.logOut, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.notLogged, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_exit:
                // close app
                this.finish();
                System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction ftMenu = getFragmentManager().beginTransaction();

        // NavigationDrawer items
        switch (id) {
            case R.id.nav_driver:
                if (FragmentUser.username!=null){
                    ftMenu.replace(R.id.container, fdriver);
                    ftMenu.addToBackStack(null);
                    ftMenu.commit();
                } else {
                    FragmentTransaction ftLogin = getFragmentManager().beginTransaction();
                    ftLogin.replace(R.id.container, flogin);
                    ftLogin.commit();
                    Toast.makeText(this, R.string.logFirst, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_passenger:
                if (FragmentUser.username!=null){
                    ftMenu.replace(R.id.container, fpassenger);
                    ftMenu.addToBackStack(null);
                    ftMenu.commit();
                } else {
                    FragmentTransaction ftLogin = getFragmentManager().beginTransaction();
                    ftLogin.replace(R.id.container, flogin);
                    ftLogin.commit();
                    Toast.makeText(this, R.string.logFirst, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_info:
                ftMenu.replace(R.id.container, finfo);
                ftMenu.addToBackStack(null);
                ftMenu.commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}