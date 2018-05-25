package skills.skills;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity {

    static Context context;
    private static Login instance = new Login();
    EditText phone_number, user_password;
    TextView register, forgotPassword;
    Button log_in_button;
    String uPhoneNumber, uPassword, data;
    ConnectivityManager connectivityManager;

    boolean connected = false;

    String DataParseUrl = "http://neighborskills.co.nf/login.php";

    JSONArray jArray;
    JSONObject json;

    public static Login getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phone_number = findViewById(R.id.phone_number);
        user_password = findViewById(R.id.user_password);
        register = findViewById(R.id.register);
        forgotPassword = findViewById(R.id.forgotPassword);
        log_in_button = findViewById(R.id.log_in_button);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPasswordDialog();
            }
        });

        log_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uPhoneNumber = phone_number.getText().toString();
                uPassword = user_password.getText().toString();

                if (Login.getInstance(getApplicationContext()).isOnline()) {
                    try {
                        if (uPhoneNumber.matches("") || uPassword.matches("")) {
                            Toast.makeText(getApplicationContext(), "Field cannot be empty", Toast.LENGTH_LONG).show();
                        } else {
                            retrieveDataFromServer(uPhoneNumber, uPassword);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enable Internet Connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }//End of OnCreate method

    public void retrieveDataFromServer(final String phoneNumber, final String password) {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            ProgressDialog pds;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pds = new ProgressDialog(Login.this);
                pds.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pds.setMessage("Logging user in.... Please wait.");
                pds.setCancelable(false);
                pds.show();
            }

            @Override
            protected String doInBackground(String... params) {

                String quickPhoneNumber = phoneNumber;
                String quickPassword = password;

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("phoneNumber", quickPhoneNumber));
                nameValuePairs.add(new BasicNameValuePair("password", quickPassword));

                try {
                    HttpClient httpClient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost(DataParseUrl);

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    data = EntityUtils.toString(response.getEntity(), "UTF-8");

                    try {
                        json = new JSONObject(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }
                return "Data Submit Successfully";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                pds.dismiss();
                try {
                    if (json.getString("status").contains("loggedIn")) {
                        userIsLoggedIn();
                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, MapsActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Login.this, "Incorrect Phone Number or Password", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(phoneNumber, password);
    }

    public void showForgotPasswordDialog() {
        try {
            ForgotPasswordDialog f = new ForgotPasswordDialog();
            f.show(getSupportFragmentManager(), "Forgot Password");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void userIsLoggedIn() throws JSONException {
        SharedPreferences aaa = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = aaa.edit();
        editor.putString("UserIsLoggedIn", json.getString("status"));
        editor.putString("name", json.getString("name"));
        editor.putString("address", json.getString("address"));
        editor.putString("email", json.getString("email"));
        editor.putString("phonenumber", json.getString("phonenumber"));
        editor.putString("skills", json.getString("skills"));
        //Convert image to Base 64
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dummy_pic2);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream); //compress to which format you want.
        byte[] byte_arr = stream.toByteArray();
        String getPic = Base64.encodeToString(byte_arr, Base64.DEFAULT);
        editor.putString("profilepic", getPic);
        editor.commit();
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

}
