package com.dji.GSDemo.GoogleMap.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dji.GSDemo.GoogleMap.Adapters.FlightAdapter;
import com.dji.GSDemo.GoogleMap.Classes.Flight;
import com.dji.GSDemo.GoogleMap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Activity to show all previous flights that were saved on firestore, user can choose a specific flight and check the info or picture
 * that were taken during that flight
 */
public class AllflightAct extends AppCompatActivity {
    RecyclerView recyclerView;
    FlightAdapter myadapter;
    ArrayList<Flight> flightArrayList = new ArrayList<>();
    String email = "guest";
    Flight flight;
    Button btnNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allflights);
        recyclerView = findViewById(R.id.recycler);
        btnNew = findViewById(R.id.btnNew);
        if (getIntent().getStringExtra("email") != null && !getIntent().getStringExtra("email").equals(""))
            email = getIntent().getStringExtra("email");
        if (email == null || email.equals(""))
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        if (!connectedToDrone())
            btnNew.setVisibility(View.GONE);
        else
            btnNew.setVisibility(View.VISIBLE);
        flight = new Flight();


        getFromFirebase();
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creatNew();
            }
        });
        recyclerView.setHasFixedSize(true);
        myadapter = new FlightAdapter(AllflightAct.this, flightArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myadapter);

    }

    /**
     * Downloads the documents containing the previous flights, sets adapter for the recycler to show the flights
     */
    private void getFromFirebase() {
        FirebaseFirestore.getInstance().collection(email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Flight flight = document.toObject(Flight.class);
                        flightArrayList.add(flight);
                        recyclerView.setAdapter(myadapter);
                    }
                } else
                    Toast.makeText(AllflightAct.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Creates a new flight instance, adds the start time as the time the func was called
     */
    private void creatNew() {
        String month, day, hour, minute;
        flight = new Flight();
        Date currentTime = Calendar.getInstance().getTime();
        int firstDigit = currentTime.getYear() % 100;
        int year = 2000 + firstDigit;
        if ((currentTime.getMonth() + 1) < 10)
            month = "0" + (currentTime.getMonth() + 1);
        else
            month = "" + (currentTime.getMonth() + 1);
        if (currentTime.getDate() < 10)
            day = "0" + currentTime.getDate();
        else
            day = "" + currentTime.getDate();
        if (currentTime.getHours() < 10)
            hour = "0" + currentTime.getHours();
        else
            hour = "" + currentTime.getHours();
        if (currentTime.getMinutes() < 10)
            minute = "0" + currentTime.getMinutes();
        else
            minute = "" + currentTime.getMinutes();
        flight.setDateStart(year + "-" + month + "-" + day + " " + hour + ":" + minute);
        flight.setCountPictures(0);
        flight.setEmail(email);
        Intent intent = new Intent(AllflightAct.this, MainActivity.class);
        intent.putExtra("flight", flight);
        startActivity(intent);
    }

    private boolean connectedToDrone() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            String ssid = info.getSSID();
            return ssid.toLowerCase().contains("spark") || ssid.toLowerCase().contains("mavic");
        } else
            return false;
    }

    @Override
    protected void onResume() {
        if (!connectedToDrone())
            btnNew.setVisibility(View.GONE);
        else
            btnNew.setVisibility(View.VISIBLE);
        super.onResume();
    }

    @Override
    protected void onRestart() {
        if (!connectedToDrone())
            btnNew.setVisibility(View.GONE);
        else
            btnNew.setVisibility(View.VISIBLE);
        super.onRestart();
    }
}
