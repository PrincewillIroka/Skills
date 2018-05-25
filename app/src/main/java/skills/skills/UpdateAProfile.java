package skills.skills;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateAProfile extends AppCompatActivity {

    static Context context;
    private static int RESULT_LOAD_IMG = 1;
    private static UpdateAProfile instance = new UpdateAProfile();
    RelativeLayout updateProfilePic, updateAddress, updatePhoneNumber, updateEmail, updateSkill;
    TextView cancel, save, nameView, addressView, phoneView, emailView, skillView;
    ImageView picView;
    String getName, getAddress, getPhoneNumber, getEmail, getSkills, getPic;
    String DataParseUrl = "http://neighborskills.co.nf/updateProfile.php";
    String imgDecodableString;
    String name, address, email, phonenumber, skills;
    Bitmap decodedImage;
    ConnectivityManager connectivityManager;

    boolean connected = false;

    public static UpdateAProfile getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_aprofile);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateProfilePic = findViewById(R.id.updateProfilePic);
        updateAddress = findViewById(R.id.updateAddress);
        updatePhoneNumber = findViewById(R.id.updatePhoneNumber);
        updateEmail = findViewById(R.id.updateEmail);
        updateSkill = findViewById(R.id.updateSkill);
        cancel = findViewById(R.id.cancel);
        save = findViewById(R.id.save);
        nameView = findViewById(R.id.nameView);
        addressView = findViewById(R.id.addressView);
        phoneView = findViewById(R.id.phoneView);
        emailView = findViewById(R.id.emailView);
        skillView = findViewById(R.id.skillView);
        picView = findViewById(R.id.picView);

        getFromSharedPreference();
        populateFields();

        updateProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProfilePic();
            }
        });

        updateAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddress();
            }
        });

        updatePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPhone();
            }
        });

        updateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEmail();
            }
        });

        updateSkill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSkill();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFromSharedPreference();
                populateFields();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UpdateAProfile.getInstance(getApplicationContext()).isOnline()) {
                    try {
                        Toast.makeText(getApplicationContext(), "Please enable Internet Connection", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    makeUpdate();
                }

            }
        });

    }

    public void getDataToSend() {
        getName = nameView.getText().toString();
        getAddress = addressView.getText().toString();
        getPhoneNumber = phoneView.getText().toString();
        getEmail = emailView.getText().toString();
        getSkills = skillView.getText().toString();
        //Convert image to Base 64
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) picView.getDrawable());
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream); //compress to which format you want.
        byte[] byte_arr = stream.toByteArray();
        getPic = Base64.encodeToString(byte_arr, Base64.DEFAULT);
    }

    public void SendDataToServer(final String name, final String address, final String email,
                                 final String phoneNumber, final String skills, final String pic) {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            ProgressDialog pds;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pds = new ProgressDialog(UpdateAProfile.this);
                pds.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pds.setMessage("Saving data.... Please wait.");
                //pds.setIndeterminate(true);
                pds.setCancelable(false);
                pds.show();
            }

            @Override
            protected String doInBackground(String... params) {

                String quickName = name;
                String quickAddress = address;
                String quickEmail = email;
                String quickPhoneNumber = phoneNumber;
                String quickSkills = skills;
                String quickPic = pic;

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("name", quickName));
                nameValuePairs.add(new BasicNameValuePair("address", quickAddress));
                nameValuePairs.add(new BasicNameValuePair("email", quickEmail));
                nameValuePairs.add(new BasicNameValuePair("phoneNumber", quickPhoneNumber));
                nameValuePairs.add(new BasicNameValuePair("skills", quickSkills));
                nameValuePairs.add(new BasicNameValuePair("pic", quickPic));


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
                Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_LONG).show();
                pds.dismiss();
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(name, address, email, phoneNumber, skills);
    }

    public void makeUpdate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateAProfile.this);
        builder.setMessage("Are you sure you want to save ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDataToSend();
                SendDataToServer(getName, getAddress, getEmail, getPhoneNumber, getSkills, getPic);
                saveIntoSharedPreference();
                dialogInterface.cancel();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(UpdateAProfile.this, MapsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProfilePic() {
        try {
            //UpdatePicDialog f = new UpdatePicDialog();
            //f.show(getSupportFragmentManager(), "showProfilePic");
            loadImagefromGallery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAddress() {
        try {
            UpdateAddressDialog f = new UpdateAddressDialog();
            f.show(getSupportFragmentManager(), "showAddress()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showPhone() {
        try {
            UpdatePhoneDialog f = new UpdatePhoneDialog();
            f.show(getSupportFragmentManager(), "showPhone");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showEmail() {
        try {
            UpdateEmailDialog f = new UpdateEmailDialog();
            f.show(getSupportFragmentManager(), "showEmail");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSkill() {
        try {
            UpdateSkillDialog f = new UpdateSkillDialog();
            f.show(getSupportFragmentManager(), "showSkill");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadImagefromGallery() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = findViewById(R.id.picView);
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getFromSharedPreference() {
        try {
            SharedPreferences aaa = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
            name = aaa.getString("name", "");
            address = aaa.getString("address", "");
            phonenumber = aaa.getString("phonenumber", "");
            email = aaa.getString("email", "");
            skills = aaa.getString("skills", "");
            //decode base64 string to image
            byte[] imageBytes = Base64.decode(aaa.getString("profilepic", ""), Base64.DEFAULT);
            decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void populateFields() {
        nameView.setText(name);
        addressView.setText(address);
        phoneView.setText(phonenumber);
        emailView.setText(email);
        skillView.setText(skills);
        picView.setImageBitmap(decodedImage);
    }

    public void saveIntoSharedPreference() {
        SharedPreferences aaa = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = aaa.edit();
        editor.putString("name", getName);
        editor.putString("address", getAddress);
        editor.putString("email", getPhoneNumber);
        editor.putString("phonenumber", getEmail);
        editor.putString("skills", getSkills);
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
