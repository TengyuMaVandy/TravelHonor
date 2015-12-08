package com.project.tengyuma.travelhonor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.jar.Attributes;
import java.util.regex.Pattern;

public class TravelHonorActivity extends AppCompatActivity {

    // server to connect to
    protected static final int TRAVELHONOR_PORT = 10721;
    protected static final String TRAVELHONOR_SERVER = "localths.ddns.net";

    // networking
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    boolean connected = false;

    //google map
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static final String TAG = "TravelHonorActivity"; //set TAG for Log.i
    Marker[] marker = new Marker[10];
    MarkerOptions[] markerOption = new MarkerOptions[10];

    BitmapDescriptor[] medalIcon = new BitmapDescriptor[10]; //marker icon
    BitmapDescriptor[] medalIconCollect = new BitmapDescriptor[10]; //marker icon collect



    //UI elements
    EditText etUsername = null;
    EditText etPassword = null;

    Button btSignUp = null;
    Button btHelloWorld = null;
    Button btMyMedal = null;
    Button btAllMedal = null;

    //Menu
    private static final int MENU_SETTINGS = Menu.FIRST;
    private static final int MENU_SIGN_UP = Menu.FIRST + 1;
    private static final int MENU_PROFILE = Menu.FIRST + 2;
    private static final int MENU_SIGN_OUT = Menu.FIRST + 3;

    //Sign flag. false means sign out, true means sign in
    Boolean signFlag = false;

    //profile
    String username = "";
    String password = "";

    //medal location from server
    Float[][] flLatLng = new Float[10][2];
    LatLng[] medalLatLng = new LatLng[10];
    Integer[] medalLatLngId = new Integer[10];

    //my medal
    Integer[] myCollectLocal = new Integer[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_honor);
        moveLocationButton();
        connect();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        setUpMapIfNeeded();

        //initial
        for(int i=0; i<10 ; i++){
            medalLatLng[i] = null;
        }

        for(int i=0; i<10 ; i++){
            for(int j=0; j<2; j++){
                flLatLng[i][j] = null;
            }
        }

        for(int i=0; i<10 ; i++){
            myCollectLocal[i] = 0;
        }

        for(int i=0; i<10 ; i++){
            medalLatLngId[i] = 0;
        }

        medalIcon[0] = BitmapDescriptorFactory.fromResource(R.drawable.alder_terrace_icon);
        medalIcon[1] = BitmapDescriptorFactory.fromResource(R.drawable.fgh_icon);
        medalIcon[2] = BitmapDescriptorFactory.fromResource(R.drawable.vanderbilt_stadium_icon);
        medalIcon[3] = BitmapDescriptorFactory.fromResource(R.drawable.student_life_center_icon);
        medalIcon[4] = BitmapDescriptorFactory.fromResource(R.drawable.medical_center_icon);
        medalIcon[5] = BitmapDescriptorFactory.fromResource(R.drawable.recreation_center_icon);
        medalIcon[6] = BitmapDescriptorFactory.fromResource(R.drawable.stevenson_center_icon);
        medalIcon[7] = BitmapDescriptorFactory.fromResource(R.drawable.central_library_icon);
        medalIcon[8] = BitmapDescriptorFactory.fromResource(R.drawable.law_school_icon);
        medalIcon[9] = BitmapDescriptorFactory.fromResource(R.drawable.vanderbilt_hospital_icon);

        medalIconCollect[0] = BitmapDescriptorFactory.fromResource(R.drawable.alder_terrace_icon_collect);
        medalIconCollect[1] = BitmapDescriptorFactory.fromResource(R.drawable.fgh_icon_collect);
        medalIconCollect[2] = BitmapDescriptorFactory.fromResource(R.drawable.vanderbilt_stadium_icon_collect);
        medalIconCollect[3] = BitmapDescriptorFactory.fromResource(R.drawable.student_life_center_icon_collect);
        medalIconCollect[4] = BitmapDescriptorFactory.fromResource(R.drawable.medical_center_icon_collect);
        medalIconCollect[5] = BitmapDescriptorFactory.fromResource(R.drawable.recreation_center_icon_collect);
        medalIconCollect[6] = BitmapDescriptorFactory.fromResource(R.drawable.stevenson_center_icon_collect);
        medalIconCollect[7] = BitmapDescriptorFactory.fromResource(R.drawable.central_library_icon_collect);
        medalIconCollect[8] = BitmapDescriptorFactory.fromResource(R.drawable.law_school_icon_collect);
        medalIconCollect[9] = BitmapDescriptorFactory.fromResource(R.drawable.vanderbilt_hospital_icon_collect);


        //sign status from AccountManage activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            signFlag = extras.getBoolean("signFlag");
            username = extras.getString("username");
            password = extras.getString("password");
        }



        //hideSignUp();
        //connect();

        //find UI elements defined in xml
        etUsername = (EditText) this.findViewById(R.id.etUsername);
        etPassword = (EditText) this.findViewById(R.id.etPassword);
        btSignUp = (Button) this.findViewById(R.id.btSignUp);
        btHelloWorld = (Button) this.findViewById(R.id.btHelloWorld);
        btAllMedal = (Button) this.findViewById(R.id.btAllMedal);
        btMyMedal = (Button) this.findViewById(R.id.btMyMedal);

        btHelloWorld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TravelHonorActivity.this, AccountManage.class);
                intent.putExtra("signFlag",signFlag);
                intent.putExtra("username",username);
                intent.putExtra("password",password);
                startActivity(intent);
                disconnect();
                finish();

                //sign up button event, supposed to send username and password to the server
                //send("NAME," + etUsername.getText());
            }
        });

        btAllMedal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0; i<10; i++) {
                    marker[i].remove();
                }
                send("GETALLMEDAL");
            }
        });

        btMyMedal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0; i<10; i++) {
                    marker[i].remove();
                }
                if (signFlag) {
                    btHelloWorld.setText(username);
                    send("GETMYCOLLECT," + username);
                }
            }
        });

        /*btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sign up button event, supposed to send username and password to the server
                //send("USERNAME," + etUsername.getText());
                //send("PASSWORD," + etPassword.getText());
            }
        });*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    public void moveLocationButton() { //move the position of locate button to right bottom connor
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
        View mapView = mapFragment.getView();
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) { //create button on toolbar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
            menu.add(0, MENU_SETTINGS, Menu.NONE, R.string.settings);
            menu.add(0, MENU_SIGN_UP, Menu.NONE, R.string.sign_up);
            if(signFlag) {
                menu.removeItem(MENU_SIGN_UP);
                menu.add(0, MENU_PROFILE, Menu.NONE, R.string.profile);
                menu.add(0, MENU_SIGN_OUT, Menu.NONE, R.string.sign_out);
            }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //set click listener to button in toolbar
        switch (item.getItemId()) {
            case MENU_SETTINGS:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case MENU_SIGN_UP:
                // User chose the "Sign Up" item, show the app Sign Up UI...
                //hideTravelHonor();
                //shoSignUp();
                Intent intent = new Intent(TravelHonorActivity.this, AccountManage.class);
                startActivity(intent);
                finish();
                return true;

            case MENU_PROFILE:
                intent = new Intent(TravelHonorActivity.this, AccountManage.class);
                intent.putExtra("signFlag",signFlag);
                intent.putExtra("username",username);
                intent.putExtra("password",password);
                startActivity(intent);
                disconnect();
                finish();
                return true;

            case MENU_SIGN_OUT:
                signFlag = false;
                username = "";
                password = "";
                btHelloWorld.setText("Name");
                disconnect();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    private void setUpMap() {
        // Add a marker in Sydney, Australia, and move the camera.

        /*final LatLng alderTerrace = new LatLng(35.836952,-86.349122); //pre-set the memorial location, actually this is my apartment's location. //use receive to get all medal from server
        final LatLng vanderbilt = new LatLng(36.144602,-86.803249); //pre-set the memorial location, actually this is vanderbilt university location.*/
        mMap.setMyLocationEnabled(true); //set getting my location is enabled
        UiSettings mMapUi = mMap.getUiSettings();
        //mMapUi.
        //mMapUi.setZoomControlsEnabled(true); //show up the button which can control zoom
        mMapUi.setCompassEnabled(true); //show compass when ?
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() { //set location button click listener, if the location is enough closed to the pre-set location, you will get the memorial medal
            @Override
            public boolean onMyLocationButtonClick() {
                if(signFlag) {
                    LatLng currentLatLng = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()); //get currentLatLng
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16)); //zoom into currentLatLng
                    float[] distance0 = new float[1]; //parameter for distance between current location and pre-set location in meters
                    //float[] distance1 = new float[1];
                    for(int i=0; i<10; i++) {
                        Location.distanceBetween(medalLatLng[i].latitude, medalLatLng[i].longitude, mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude(), distance0);
                        if(distance0[0] < 30) {
                            for (int j = 0; j < 10; j++) {
                                if (myCollectLocal[j].equals((i + 1))) {
                                    Log.i(TAG, myCollectLocal[j].toString());
                                    System.out.println((i + 1));
                                    new AlertDialog.Builder(TravelHonorActivity.this)
                                            .setTitle("Oops!")
                                            .setMessage("You have already collected this medal.\nKeep looking for a new one!")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //do nothing
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // do nothing
                                                }
                                            })
                                            .show(); //If you are closed enough to the pre-set location, the dialog will show up.
                                    break;

                                } else if (j == 9) {
                                    new AlertDialog.Builder(TravelHonorActivity.this)
                                            .setTitle("Congratulation!")
                                            .setMessage("You got your memorial medal!")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //do nothing
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // do nothing
                                                }
                                            })
                                            .show(); //If you are closed enough to the pre-set location, the dialog will show up.

                                    send("COLLECTMEDAL," + username + "#" + (i+1));//here tomorrow go on. Collect medal to serer and set layout picture

                                }
                            }
                        }


                            String stDistance0 = Float.toString(distance0[0]);
                            Log.i(TAG, currentLatLng.toString());
                            Log.i(TAG, stDistance0); //print out the distance between pre-set location and current location
                            // your code...
                        }
                    }

                else {
                    new AlertDialog.Builder(TravelHonorActivity.this)
                            .setTitle("Oops!")
                            .setMessage("Please sign in first to enjoy your collecting!")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) { //go to Account Manage activity
                                    Intent intent = new Intent(TravelHonorActivity.this, AccountManage.class);
                                    intent.putExtra("signFlag",signFlag);
                                    intent.putExtra("username",username);
                                    intent.putExtra("password",password);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show(); //If you are closed enough to the pre-set location, the dialog will show up.
                }



                // wait to set the click event

                return true;
            }
        });
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() { //set location button click listener, if the location is enough closed to the pre-set location, you will get the memorial medal
            @Override
            public void onMyLocationChange(Location location) {
                // wait to set the location change event

            }
        });
        /*BitmapDescriptor alderTerraceIcon = BitmapDescriptorFactory.fromResource(R.drawable.alder_terrace_icon); //use receive to set marker
        mMap.addMarker(new MarkerOptions().position(alderTerrace).title("Marker in Alder Terrace").snippet("My apartment").icon(alderTerraceIcon));
        mMap.addMarker(new MarkerOptions().position(vanderbilt).title("Marker in Vanderbilt").snippet("My University"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(alderTerrace));*/
       // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }



    /**
     * Connect to the server. This method is safe to call from the UI thread.
     */
    //communicate with server here
    void connect() {

        new AsyncTask<Void, Void, String>() {

            String errorMsg = null;

            @Override
            protected String doInBackground(Void... args) {
                Log.i(TAG, "Connect task started");
                try {
                    connected = false;
                    socket = new Socket(TRAVELHONOR_SERVER, TRAVELHONOR_PORT);
                    Log.i(TAG, "Socket created");
                    in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream());

                    connected = true;
                    Log.i(TAG, "Input and output streams ready");

                } catch (UnknownHostException e1) {
                    errorMsg = e1.getMessage();
                } catch (IOException e1) {
                    errorMsg = e1.getMessage();
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
                Log.i(TAG, "Connect task finished");
                if(signFlag) {
                    send("SIGNIN," + username);
                }
                send("GETALLMEDAL");
                return errorMsg;
            }

            @Override
            protected void onPostExecute(String errorMsg) {
                if (errorMsg == null) {
                    Toast.makeText(getApplicationContext(),
                            "Connected to server", Toast.LENGTH_SHORT).show();

                    // start receiving
                    receive();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    // can't connect: close the activity
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void receive() {
        new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... args) {
                Log.i(TAG, "Receive task started");
                try {
                    while (connected) {

                        String msg = in.readLine();

                        if (msg == null) { // other side closed the
                            // connection
                            break;
                        }
                        publishProgress(msg);
                    }

                } catch (UnknownHostException e1) {
                    Log.i(TAG, "UnknownHostException in receive task");
                } catch (IOException e1) {
                    Log.i(TAG, "IOException in receive task");
                } finally {
                    connected = false;
                    try {
                        if (out != null)
                            out.close();
                        if (socket != null)
                            socket.close();
                    } catch (IOException e) {
                        //empty catch now
                    }
                }
                Log.i(TAG, "Receive task finished");
                return null;
            }

            @Override
            protected void onProgressUpdate(String... lines) {
                // the message received from the server is
                // guaranteed to be not null
                String msg = lines[0];

                // TODO: act on messages received from the server
                if(msg.startsWith("+OK,USERNAME")) {

                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(msg.startsWith("+ERROR,USERNAME")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(msg.startsWith("+OK,Create successfully")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                    Log.i(TAG, msg);
                    return;
                }

                if(msg.startsWith("+ERROR,Error with password")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if(msg.startsWith("+OK,Username is OK")) {
                    if(signFlag) {
                        send("CHECKPASSWORD," + password);
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if (msg.startsWith("+OK,Log in successfully")) {
                    if (signFlag) {
                        btHelloWorld.setText(username);
                        send("GETMYCOLLECT," + username);
                    }

                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if (msg.startsWith("+OK,Get all medal successfully")) {
                    String allMedal = msg.substring("+OK,Get all medal successfully".length(), (msg.length()-1));
                    Log.i(TAG,"1");

                    String[] allMedalRows = allMedal.split("#");
                    String[] allMedalUnits;
                    String allMedalLat;
                    String allMedalLng;
                    Log.i(TAG,"2");
                    for (int i=0; i<10 ;i++) {
                        System.out.println(i);
                        Log.i(TAG,allMedalRows[i]);
                    }

                    /*allMedalUnits = allMedalRows[2].split(Pattern.quote("|"));
                    Log.i(TAG,allMedalUnits[0]);
                    Log.i(TAG,allMedalUnits[1]);*/
                    for(int i=0; i<10; i++){
                        allMedalUnits = allMedalRows[i].split(Pattern.quote("|"));

                        Log.i(TAG,"3");
                        Log.i(TAG,allMedalUnits[1]);
                        Log.i(TAG,allMedalUnits[2]);
                        medalLatLngId[i] = Integer.valueOf(allMedalUnits[0]);
                        allMedalLat = allMedalUnits[1];
                        allMedalLng = allMedalUnits[2];

                        flLatLng[i][0] = Float.valueOf(allMedalLat); //assign location's latitude from server to local variable
                        Log.i(TAG, "convert" + flLatLng[i][0].toString());
                        flLatLng[i][1] = Float.valueOf(allMedalLng); //assign location's longitude from server to local variable
                        Log.i(TAG,"convert" + flLatLng[i][1].toString());
                        Log.i(TAG,"4");
                        medalLatLng[i] = new LatLng(flLatLng[i][0],flLatLng[i][1]); //assign location's latitude and longitude to LatLng variable
                        Log.i(TAG, "5");
                        //use receive to set marker
                        markerOption[i] = new MarkerOptions().position(medalLatLng[i]).title("Golden Medal").snippet(allMedalUnits[3]).icon(medalIcon[i]);
                        marker[i] = mMap.addMarker(markerOption[i]);


                        System.out.println(i);
                    }
                     if(signFlag) {
                         for (int i=0; i<10;i++) {
                            for (int j=0; j<10; j++) {
                                if (myCollectLocal[i].equals(medalLatLngId[j])) {
                                    BitmapDescriptor alderTerraceIconCollect = BitmapDescriptorFactory.fromResource(R.drawable.alder_terrace_icon_collect); //use receive to set marker
                                    String snippet = marker[j].getSnippet();
                                    marker[j].remove();
                                    markerOption[j] = new MarkerOptions().position(medalLatLng[j]).title("Collected Golden Medal").snippet(snippet).icon(medalIconCollect[j]);
                                    marker[j] = mMap.addMarker(markerOption[j]);

                                }
                            }
                        }

                     }
                     mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medalLatLng[1], 14));

                    //assign all medal location from server
                    //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if (msg.startsWith("+OK,Get your collect successfully")) {
                    String allMyCollect = msg.substring("+OK,Get your collect successfully".length(), (msg.length() - 1));
                    Log.i(TAG, "1");
                    String[] myCollect = allMyCollect.split("#");
                    Log.i(TAG, "2");
                        Integer myCollectSum= myCollect.length;
                        for (int i=0; i<myCollectSum ; i++) {
                            myCollectLocal[i] = Integer.valueOf(myCollect[i]);
                            for (int j=0; j<10; j++) {
                                if (myCollectLocal[i].equals(medalLatLngId[j])) {
                                    String snippet = marker[j].getSnippet();
                                    marker[j].remove();
                                    markerOption[j] = new MarkerOptions().position(medalLatLng[j]).title("Collected Golden Medal").snippet(snippet).icon(medalIconCollect[j]);
                                    marker[j] = mMap.addMarker(markerOption[j]);

                                }
                            }

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medalLatLng[1],14));

                            System.out.println(i);
                            Log.i(TAG, msg);
                            Log.i(TAG,myCollect[i]);
                        }
                    return;
                }
                // if we haven't returned yet, tell the user that we have an unhandled message
                Toast.makeText(getApplicationContext(), "Unhandled message: "+msg, Toast.LENGTH_SHORT).show();

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void disconnect() {
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    connected = false;
                }
                // make sure that we close the output, not the input
                if (out != null) {
                    out.print("BYE");
                    out.flush();
                    out.close();
                }
                // in some rare cases, out can be null, so we need to close the socket itself
                if (socket != null)
                    try { socket.close();} catch(IOException ignored) {}

                Log.i(TAG, "Disconnect task finished");
            }
        }.start();
    }

    boolean send(String msg) {
        if (!connected) {
            Log.i(TAG, "can't send: not connected");
            return false;
        }

        new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... msg) {
                Log.i(TAG, "sending: " + msg[0]);
                out.println(msg[0]);
                return out.checkError();
            }

            @Override
            protected void onPostExecute(Boolean error) {
                if (!error) {
                    Toast.makeText(getApplicationContext(),
                            "Message sent to server", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error sending message to server",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);

        return true;
    }


     // Hide the "connecting to server" text

    void hideTravelHonor() {
        findViewById(R.id.rlTravelHonor).setVisibility(View.GONE);
    }


     //Show the "connecting to server" text

    void showTravelHonor() {
        findViewById(R.id.rlTravelHonor).setVisibility(View.VISIBLE);
    }


     //Hide the "connecting to server" text

    void hideSignUp() {
        findViewById(R.id.llSignUp).setVisibility(View.GONE);
    }


    //Show the "connecting to server" text

    void shoSignUp() {
        findViewById(R.id.llSignUp).setVisibility(View.VISIBLE);
    }


}
