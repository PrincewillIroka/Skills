package skills.skills;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private static int TIME_OUT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    /*SharedPreferences aaa = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                    boolean checkStatus = aaa.getBoolean("UserIsLoggedIn", true);

                    if(checkStatus){
                        Intent i = new Intent(SplashScreen.this, MapsActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else{
                        Intent i = new Intent(SplashScreen.this, Login.class);
                        startActivity(i);
                        finish();
                    }*/
                    Intent i = new Intent(SplashScreen.this, MapsActivity.class);
                    startActivity(i);
                    finish();
                }
            }, TIME_OUT);

            SharedPreferences aaa = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
            boolean shouldInsertData = aaa.getBoolean("UserIsLoggedIn", true);

            if (shouldInsertData) {
                //insert your data into the preferences
                SharedPreferences.Editor editor = aaa.edit();
                editor.putString("UserIsLoggedIn", "");
                editor.commit();
                aaa.edit().putBoolean("UserIsLoggedIn", false).apply();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
