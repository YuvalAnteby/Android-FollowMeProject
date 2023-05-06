package com.dji.GSDemo.GoogleMap.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.dji.GSDemo.GoogleMap.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * First activity, Checks if user has signed to the app using a google account, will move to ConnectionActivity after 2 seconds
 */
public class Splash extends AppCompatActivity {

    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();

        ImageView photo = (ImageView) findViewById(R.id.imageView);
        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.mytransition);
        photo.startAnimation(myanim);
        final Intent k = new Intent(getBaseContext(), ConnectionActivity.class);
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (mAuth.getCurrentUser() != null) {
                        k.putExtra("Signed", true);
                        startActivity(k);
                        finish();
                    } else {
                        k.putExtra("Signed", false);
                        startActivity(k);
                        finish();
                    }
                }
            }
        };
        timer.start();
    }

}
