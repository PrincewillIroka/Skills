package skills.skills;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Register extends AppCompatActivity {

    static Context context;
    private static Register instance = new Register();
    private static int TIME_OUT = 10000;
    Button register_button;
    EditText user_name, user_address, user_email, user_skills, user_phone_number, user_password;
    String getName, getAddress, getEmail, getPassword, dLat, dLng, getSkills, getPhoneNumber;
    double lat2;
    double lng2;
    ConnectivityManager connectivityManager;
    boolean connected = false;
    String DataParseUrl = "http://neighborskills.co.nf/register.php";
    ProgressDialog pds1;

    public static Register getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        handleGPS();

        user_name = findViewById(R.id.user_name);
        user_address = findViewById(R.id.user_address);
        user_email = findViewById(R.id.user_email);
        user_skills = findViewById(R.id.user_skills);
        user_phone_number = findViewById(R.id.user_phone_number);
        user_password = findViewById(R.id.user_password);


        user_skills.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        UpdateSkillDialog f = new UpdateSkillDialog();
                        f.show(getSupportFragmentManager(), "showSkill");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });

        register_button = findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dLat = Double.toString(lat2);
                    dLng = Double.toString(lng2);

                    if (!Register.getInstance(getApplicationContext()).isOnline()) {
                        try {
                            Toast.makeText(getApplicationContext(), "Please enable Internet Connection", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (user_name.getText().toString().matches("") || user_address.getText().toString().matches("") ||
                                user_email.getText().toString().matches("") || user_skills.getText().toString().matches("")
                                || user_phone_number.getText().toString().matches("") || user_password.getText().toString().matches("")) {
                            Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_LONG).show();
                        } else {
                            getDataToSend();
                            if (dLat.matches("0.0") || dLng.matches("0.0")) {
                                pds1 = new ProgressDialog(Register.this);
                                pds1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pds1.setMessage("Getting current GPS Coordinates.... Please wait.");
                                pds1.setCancelable(false);
                                pds1.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pds1.dismiss();
                                    }
                                }, TIME_OUT);

                            }
                            SendDataToServer(getName, getAddress, getEmail, getPassword, dLat, dLng,
                                    getPhoneNumber, getSkills);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }//End of OnCreate method

    public void getDataToSend() {
        getName = user_name.getText().toString();
        getAddress = user_address.getText().toString();
        getEmail = user_email.getText().toString();
        getPassword = user_password.getText().toString();
        getPhoneNumber = user_phone_number.getText().toString();
        getSkills = user_skills.getText().toString();

    }

    public void SendDataToServer(final String name, final String address, final String email, final String password,
                                 final String lat, final String lng, final String phoneNumber, final String skills) {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            ProgressDialog pds;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pds = new ProgressDialog(Register.this);
                pds.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pds.setMessage("Registering.... Please wait.");
                //pds.setIndeterminate(true);
                pds.setCancelable(false);
                pds.show();
            }

            @Override
            protected String doInBackground(String... params) {

                String quickName = name;
                String quickAddress = address;
                String quickEmail = email;
                String quickPassword = password;
                String quickLat = lat;
                String quickLng = lng;
                String quickPhoneNumber = phoneNumber;
                String quickSkills = skills;


                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("name", quickName));
                nameValuePairs.add(new BasicNameValuePair("address", quickAddress));
                nameValuePairs.add(new BasicNameValuePair("email", quickEmail));
                nameValuePairs.add(new BasicNameValuePair("password", quickPassword));
                nameValuePairs.add(new BasicNameValuePair("lat", quickLat));
                nameValuePairs.add(new BasicNameValuePair("lng", quickLng));
                nameValuePairs.add(new BasicNameValuePair("phoneNumber", quickPhoneNumber));
                nameValuePairs.add(new BasicNameValuePair("skills", quickSkills));


                try {
                    HttpClient httpClient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost(DataParseUrl);

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();


                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }
                return "Data Submit Successfully";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_LONG).show();
                pds.dismiss();
                clearFields();
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(name, address, email, password, lat, lng, phoneNumber, skills);
    }

    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }

    public void handleGPS() {
        checkGPSConnection();
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 100, 5, locationListener);
    }

    public void checkGPSConnection() {
        try {
            // Get Location Manager and check for GPS & Network location services
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Build the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Services Not Active");
                builder.setMessage("Please enable Location Services");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Show location settings when the user acknowledges the alert dialog
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
                Dialog alertDialog = builder.create();
                //alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearFields() {
        user_name.setText("");
        user_address.setText("");
        user_email.setText("");
        user_password.setText("");
        user_phone_number.setText("");
        user_skills.setText("");
        register_button.requestFocus();
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {

            lat2 = loc.getLatitude();
            lng2 = loc.getLongitude();

        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

}
