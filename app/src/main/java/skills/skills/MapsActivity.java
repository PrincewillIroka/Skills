package skills.skills;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, LocationListener, NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    GoogleApiClient mGoogleApiClient;
    Marker mLocationMarker;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    ImageView nav, clear;
    AutoCompleteTextView autocomplete_skills;
    TextView found;

    DrawerLayout drawerLayout;

    String DataParseUrl = "http://neighborskills.co.nf/searchForSkill.php";

    JSONArray jArray;
    JSONObject json, jsonh;
    String data, used_skill;
    String frane1, frane2, frane3, frane4, frane5;

    LatLng CURRENT_LOCATION;

    Menu menu;
    MenuItem target, target1, target2;

    View hview;

    int count = 0;
    ArrayList<String> mylist = new ArrayList<String>();
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkInternetConnection();
        checkGPSConnection();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setPopupTheme(R.style.AppTheme_NoActionBar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menu = navigationView.getMenu();
        target = menu.findItem(R.id.sign_in);
        target1 = menu.findItem(R.id.sign_out);
        target2 = menu.findItem(R.id.update_profile);

        controlUserAccess();//Method to check whether UserIsLoggedIn

        nav = findViewById(R.id.nav);
        autocomplete_skills = findViewById(R.id.autocomplete_skills);
        clear = findViewById(R.id.clear);
        found = findViewById(R.id.found);

        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
                hview = getWindow().getCurrentFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(hview.getWindowToken(), 0);
            }
        });

        String[] skills = getResources().getStringArray(R.array.skills_array);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, skills);
        autocomplete_skills.setAdapter(adapter);

        autocomplete_skills.setThreshold(1);
        autocomplete_skills.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                try {
                    hview = getWindow().getCurrentFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(hview.getWindowToken(), 0);
                    checkInternetConnection();
                    checkGPSConnection();
                    retrieveDataFromServer((String) parent.getItemAtPosition(position));
                    used_skill = (String) parent.getItemAtPosition(position);
                    //useItemToSearch((String) parent.getItemAtPosition(position));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        autocomplete_skills.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (autocomplete_skills.getText().length() == 0) {
                    clear.setVisibility(View.INVISIBLE);
                } else {
                    clear.setVisibility(View.VISIBLE);
                }

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                autocomplete_skills.setText("");
                clear.setVisibility(View.INVISIBLE);
            }
        });


    }//End of OnCreate method

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.update_profile) {
            Intent intent = new Intent(MapsActivity.this, UpdateAProfile.class);
            startActivity(intent);
        } else if (id == R.id.about_app) {
            Intent intent = new Intent(MapsActivity.this, AboutApp.class);
            startActivity(intent);
        } else if (id == R.id.contact_us) {
            Intent intent = new Intent(MapsActivity.this, ContactUs.class);
            startActivity(intent);
        } else if (id == R.id.sign_in) {
            Intent intent = new Intent(MapsActivity.this, Login.class);
            startActivity(intent);
        } else if (id == R.id.sign_out) {
            checkInternetConnection();
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage("Are you sure you want to sign out ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences aaa = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = aaa.edit();
                    editor.putString("UserIsLoggedIn", "notLoggedIn");
                    editor.commit();
                    dialogInterface.cancel();
                    controlUserAccess();//Remove Sign In button from Nav View
                    finish();//Refresh the activity
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMarkerClickListener(this);
        //mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        //mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        // Use the GoogleApiClient.Builder class to create an instance of the
        // Google Play Services API client//
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        // Connect to Google Play Services, by calling the connect() method//
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(1);
        //mLocationRequest.setFastestInterval(1);

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Retrieve the user’s last known location//
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    // Once the user has granted or denied your permission request, the Activity’s
    // onRequestPermissionsResult method will be called, and the system will pass
    // the results of the ‘grant permission’ dialog, as an int//

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;

        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }

        double lat2 = location.getLatitude();
        double lng2 = location.getLongitude();
        CURRENT_LOCATION = new LatLng(lat2, lng2);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(CURRENT_LOCATION).zoom(17).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // To help preserve the device’s battery life, you’ll typically want to use
        // removeLocationUpdates to suspend location updates when your app is no longer
        // visible onscreen//
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                // If the request is cancelled, the result array will be empty (0)//
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // If the user has granted your permission request, then your app can now perform all its
                    // location-related tasks, including displaying the user’s location on the map//
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // If the user has denied your permission request, then at this point you may want to
                    // disable any functionality that depends on this permission//
                }
                return;
            }
        }
    }

    public void checkInternetConnection() {
        try {

            ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

            if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Internet Connection Not Active");
                builder.setMessage("Please enable Internet Connection");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Show location settings when the user acknowledges the alert dialog
                        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
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

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        try {
            String place = marker.getTitle();

            switch (place) {
                case "Cleaner":

                    break;
                case "Computer Engineer":

                    break;
                case "Electrician":

                    break;
                case "Gardener":

                    break;
                case "Nurse":

                    break;
                case "Plumber":

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addNeighbour() throws JSONException {

        mMap.clear();

        jArray = new JSONArray(data);

        LatLng ap;
        Double myDouble, myDouble2;

        for (int i = 0; i < jArray.length(); i++) {

            json = jArray.getJSONObject(i);
            frane1 = json.getString("name");
            frane2 = json.getString("address");
            frane3 = json.getString("email");
            myDouble = new Double(json.getString("lat"));
            myDouble2 = new Double(json.getString("lng"));
            frane4 = json.getString("phonenumber");
            frane5 = json.getString("profilepic");
            //Convert profilepic to base64 below
            byte[] decodedString = Base64.decode(frane5, Base64.DEFAULT);
            ByteArrayInputStream bis = new ByteArrayInputStream(decodedString);
            Bitmap bp = BitmapFactory.decodeStream(bis); //decode stream to a bitmap image
            clear.setImageBitmap(bp);

            ap = new LatLng(myDouble, myDouble2);
            String snippet = frane1 + "_" + frane2 + "_" + frane3 + "_" + frane4;
            mMap.addMarker(new MarkerOptions().position(ap).title(used_skill).snippet(snippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            //mMap.addPolyline(new PolylineOptions().add(ap, CURRENT_LOCATION).color(Color.GREEN));
            //count = count + 1;
            mylist.add(frane1);
        }

        //jsonh = jArray.getJSONObject(jArray.length()-1);
        //frane5 = jsonh.getString("name");
        found.setText(mylist.size() + " found");
        mylist.clear();


        CameraPosition cameraPosition = new CameraPosition.Builder().target(CURRENT_LOCATION).zoom(2).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    public void controlUserAccess() {
        try {
            SharedPreferences aaa = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
            String go = "loggedIn";
            if (aaa.getString("UserIsLoggedIn", "").contains(go)) {
                target.setVisible(false);
                target1.setVisible(true);
                target2.setVisible(true);
            } else {
                target.setVisible(true);
                target1.setVisible(false);
                target2.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void retrieveDataFromServer(final String skill) {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, Void> {

            ProgressDialog pds;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pds = new ProgressDialog(MapsActivity.this);
                pds.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pds.setMessage("Searching.... Please wait.");
                pds.setCancelable(false);
                pds.show();
            }

            @Override
            protected Void doInBackground(String... params) {

                String quickSkill = skill;

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("skill", quickSkill));

                try {
                    HttpClient httpClient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost(DataParseUrl);

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    data = EntityUtils.toString(response.getEntity(), "UTF-8");

                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                try {
                    addNeighbour();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pds.dismiss();

            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(skill);
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;
        TextView tvTitle, a, b, c, d;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            tvTitle = myContentsView.findViewById(R.id.title);
            a = myContentsView.findViewById(R.id.a);
            b = myContentsView.findViewById(R.id.b);
            c = myContentsView.findViewById(R.id.c);
            d = myContentsView.findViewById(R.id.d);
            tvTitle.setText(marker.getTitle());
            String str = marker.getSnippet();
            final String[] str2 = str.split("_");
            a.setText(str2[0]);
            b.setText(str2[1]);
            c.setText(str2[2]);
            d.setText(str2[3]);

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }


}
