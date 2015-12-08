package com.project.tengyuma.travelhonor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by TengyuMa on 11/29/2015.
 */
public class AccountManage extends AppCompatActivity {
    // server to connect to
    protected static final int TRAVELHONOR_PORT = 10721;
    protected static final String TRAVELHONOR_SERVER = "localths.ddns.net";

    // networking
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    boolean connected = false;

    //set TAG for Log.i
    private static final String TAG = "TravelHonorActivity";

    //UI elements
    TextView tvPfUsername = null;
    TextView tvPf = null;
    TextView tvSIUsername = null;
    TextView tvSIPassword = null;

    EditText etUsername = null;
    EditText etPassword = null;
    EditText etSIUsername = null;
    EditText etSIPasswrod = null;

    Button btSignUp = null;
    Button btSignIn = null;
    Button btHelloWorld = null;
    Button btProfile = null;
    Button btSISignIn = null; //sign in button in linear layout sign in
    Button btMedalMuseum = null;

    ImageView[] ivMyMedal = new ImageView[10]; //medal museum
    Integer[] medalCollect = new Integer[10]; //marker icon collect

    //sign flag
    boolean signFlag = false;

    //profile
    String username = "";
    String password = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_manage);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        hideProfile();
        hidellSignIn();
        hideMyMedal();
        connect();

        //initial
        for(int i=0; i<10 ; i++){
            ivMyMedal[i] = null;
        }
        for(int i=0; i<10 ; i++){
            medalCollect[i] = 0;
        }

        medalCollect[0] = R.drawable.alder_terrace_medal_collect;
        medalCollect[1] = R.drawable.fgh_medal_collect;
        medalCollect[2] = R.drawable.vanderbilt_stadium_medal_collect;
        medalCollect[3] = R.drawable.student_life_center_medal_collect;
        medalCollect[4] = R.drawable.medical_center_medal_collect;
        medalCollect[5] = R.drawable.recreation_center_medal_collect;
        medalCollect[6] = R.drawable.stevenson_center_medal_collect;
        medalCollect[7] = R.drawable.central_library_medal_collect;
        medalCollect[8] = R.drawable.law_school_medal_collect;
        medalCollect[9] = R.drawable.vanderbilt_hospital_medal_collect;


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            signFlag = extras.getBoolean("signFlag");
            username = extras.getString("username");
            password = extras.getString("password");
        }

        //find UI elements defined in xml
        tvPfUsername = (TextView) this.findViewById(R.id.tvPfUsername);
        tvPf = (TextView) this.findViewById(R.id.tvPf);
        tvSIUsername = (TextView) this.findViewById(R.id.tvSIUsername);
        tvSIPassword = (TextView) this.findViewById(R.id.tvSIPassword);

        etUsername = (EditText) this.findViewById(R.id.etUsername);
        etPassword = (EditText) this.findViewById(R.id.etPassword);
        etSIUsername = (EditText) this.findViewById(R.id.etSIUsername);
        etSIPasswrod = (EditText) this.findViewById(R.id.etSIPassword);

        btSignUp = (Button) this.findViewById(R.id.btSignUp);
        btSignIn = (Button) this.findViewById(R.id.btSignIn);
        btHelloWorld = (Button) this.findViewById(R.id.btHelloWorld);
        btProfile = (Button) this.findViewById(R.id.btPf);//button for profile
        btSISignIn = (Button) this.findViewById(R.id.btSISignIn);
        btMedalMuseum = (Button) this.findViewById(R.id.btMedalMuseum);

        ivMyMedal[0] = (ImageView) this.findViewById(R.id.ivMyMedal1);
        ivMyMedal[1] = (ImageView) this.findViewById(R.id.ivMyMedal2);
        ivMyMedal[2] = (ImageView) this.findViewById(R.id.ivMyMedal3);
        ivMyMedal[3] = (ImageView) this.findViewById(R.id.ivMyMedal4);
        ivMyMedal[4] = (ImageView) this.findViewById(R.id.ivMyMedal5);
        ivMyMedal[5] = (ImageView) this.findViewById(R.id.ivMyMedal6);
        ivMyMedal[6] = (ImageView) this.findViewById(R.id.ivMyMedal7);
        ivMyMedal[7] = (ImageView) this.findViewById(R.id.ivMyMedal8);
        ivMyMedal[8] = (ImageView) this.findViewById(R.id.ivMyMedal9);
        ivMyMedal[9] = (ImageView) this.findViewById(R.id.ivMyMedal10);




        /*btHelloWorld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sign up button event, supposed to send username and password to the server
                //send("NAME," + etUsername.getText());
            }
        });*/



        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sign up button event, supposed to send new username and password to the server
                send("USERNAME," + etUsername.getText());

            }
        });

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sign in button event, supposed to send user name and password to the server. If the username and password matched, jump to user's profile layout.
                etUsername.setText("");
                etPassword.setText("");
                hideSignUp();
                showllSignIn();
            }
        });

        btProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signFlag) {
                    send("GETPROFILE," + username);
                }
                else if (etSIUsername.equals("")){
                    send("GETPROFILE," + etUsername.getText());
                }
                else {
                    send("GETPROFILE," + etSIUsername.getText());
                }

            }
        });

        btSISignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("SIGNIN," + etSIUsername.getText());
            }
        });

        btMedalMuseum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("GETMYCOLLECT," + username); //here here here not finish!!!
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //create button on toolbar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //set click listener to button in toolbar
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.home_page:
                // User chose the "Sign Up" item, show the app Sign Up UI...
                //hideTravelHonor();
                //shoSignUp();
                Intent intent = new Intent(AccountManage.this, TravelHonorActivity.class);
                //startActivity(intent);
                //Intent i = new Intent(getApplicationContext(), NewActivity.class);
                intent.putExtra("signFlag",signFlag);
                intent.putExtra("username",username);
                intent.putExtra("password",password);
                startActivity(intent);
                disconnect();
                finish();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Connect to the server. This method is safe to call from the UI thread.
     */
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
                //sign status from TravelHonorActivity activity

                send("SIGNIN," + username);
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
                    send("PASSWORD," + etPassword.getText());
                    return;
                }

                if(msg.startsWith("+ERROR,USERNAME")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(msg.startsWith("+OK,Create successfully")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, msg);
                    hideSignUp();
                    showProfile();
                    send("GETPROFILE," + etUsername.getText());

                    return;
                }

                if(msg.startsWith("+ERROR,Error with password")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG, msg);
                    return;
                }

                if(msg.startsWith("+OK,Get profile successfully")) {
                    if (signFlag) {
                        tvPfUsername.setText(username);
                    }
                    else if (etUsername.getText().toString().equals("")) {
                        tvPfUsername.setText(etSIUsername.getText());
                        username = etSIUsername.getText().toString();
                        password = etSIPasswrod.getText().toString();
                    }
                    else {
                        tvPfUsername.setText(etUsername.getText());
                        username = etUsername.getText().toString();
                        password = etPassword.getText().toString();
                    }
                    String profile = msg.substring("+OK,Get profile successfully".length());
                    String[] profileRows = profile.split("#");
                    StringBuilder profileComplete = new StringBuilder();
                    for (String t : profileRows) {
                        profileComplete.append(t);
                        profileComplete.append("\n");
                    }
                    if(profileComplete.length()>0)
                        profileComplete.deleteCharAt(profileComplete.length()-1); // remove last #
                    tvPf.setText(profileComplete);
                    signFlag = true;
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, msg);
                    return;
                }

                if(msg.startsWith("+ERROR,Error with GETPROFILE")) {
                    if (etUsername.getText().toString().equals("")) {
                        tvPfUsername.setText(etSIUsername.getText());
                    }
                    else {
                        tvPfUsername.setText(etUsername.getText());
                    }
                    tvPf.setText("You have no medal for collecting location");
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }
                if(msg.startsWith("+ERROR,Profile is empty")) {
                    if (signFlag) {
                        tvPfUsername.setText(username);
                    }
                    else if (etUsername.getText().toString().equals("")) {
                        tvPfUsername.setText(etSIUsername.getText());
                        username = etSIUsername.getText().toString();
                        password = etSIPasswrod.getText().toString();
                    }
                    else {
                        tvPfUsername.setText(etUsername.getText());
                        username = etUsername.getText().toString();
                        password = etPassword.getText().toString();
                    }
                    tvPf.setText("Oops! You have no medal, keep looking!");
                    signFlag = true;
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if(msg.startsWith("+ERROR,Error with SIGNIN")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if(msg.startsWith("+ERROR,Username doesn't exist")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if(msg.startsWith("+OK,Username is OK")) {
                    if(signFlag) {
                        send("CHECKPASSWORD," + password);
                    }
                    else {
                        send("CHECKPASSWORD," + etSIPasswrod.getText());
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if (msg.startsWith("+OK,Log in successfully")) {
                    if (signFlag) {
                        send("GETPROFILE," + username);
                    }
                    else if (etUsername.getText().toString().equals("")) {
                        send("GETPROFILE," + etSIUsername.getText());
                    }
                    else {
                        send("GETPROFILE," + etUsername.getText());
                    }
                    hideSignUp();
                    hidellSignIn();
                    showProfile();
                    showMyMedal();
                    hideMyAchievement();
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if(msg.startsWith("+ERROR,Password doesn't exist")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if(msg.startsWith("+ERROR,Password isn't correct")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();Log.i(TAG, msg);
                    Log.i(TAG,msg);
                    return;
                }

                if(msg.startsWith("+OK,Get your collect successfully")) {
                    String allMyCollect = msg.substring("+OK,Get your collect successfully".length(), (msg.length() - 1));
                    Log.i(TAG, "1");
                    String[] myCollect = allMyCollect.split("#");
                    Log.i(TAG, "2");
                    Integer myCollectSum= myCollect.length;
                    Integer myCollectId;
                    for (int i=0; i<myCollectSum ; i++) {
                        for (int j=0; j<10; j++) {
                            myCollectId = Integer.valueOf(myCollect[i]);
                            if (myCollectId.equals((j + 1))) {
                                ivMyMedal[j].setImageResource(medalCollect[j]);
                            }
                        }

                        System.out.println(i);
                        Log.i(TAG, msg);
                        Log.i(TAG,myCollect[i]);
                    }
                    if (myCollectSum.equals(10)) {
                        new AlertDialog.Builder(AccountManage.this)
                                .setTitle("Legendary!")
                                .setMessage("You have already collected all medal in the world!\nHere is your hidden achievement medal!")
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
                        tvPfUsername.setText(username + "(Legend)");
                        showMyAchievement();
                    }
                    return;
                }

                // [ ... and so on for other kinds of messages]


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

    /**
     * Hide the hideTravelHonor
     */
    void hideTravelHonor() {
        findViewById(R.id.rlTravelHonor).setVisibility(View.GONE);
    }

    /**
     * Show the showTravelHonor
     */
    void showTravelHonor() {
        findViewById(R.id.rlTravelHonor).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the hideSignUp
     */
    void hideSignUp() {
        findViewById(R.id.llSignUp).setVisibility(View.GONE);
    }

    /**
     * Show the hideSignUp
     */
    void showSignUp() {
        findViewById(R.id.llSignUp).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the llSignIn
     */
    void hidellSignIn() {
        findViewById(R.id.llSignIn).setVisibility(View.GONE);
    }

    /**
     * Show the llSignIn
     */
    void showllSignIn() {
        findViewById(R.id.llSignIn).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the hideProfile
     */

    void hideProfile() {
        findViewById(R.id.llProfile).setVisibility(View.GONE);
    }

    /**
     * Show the hideProfile
     */
    void showProfile() {
        findViewById(R.id.llProfile).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the hideTravelHonor
     */
    void hideMyMedal() {
        findViewById(R.id.llMyMedal).setVisibility(View.GONE);
    }

    /**
     * Show the showTravelHonor
     */
    void showMyMedal() {
        findViewById(R.id.llMyMedal).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the hideTravelHonor
     */
    void hideMyAchievement() {
        findViewById(R.id.llMyAchievement).setVisibility(View.GONE);
    }

    /**
     * Show the showTravelHonor
     */
    void showMyAchievement() {
        findViewById(R.id.llMyAchievement).setVisibility(View.VISIBLE);
    }


}