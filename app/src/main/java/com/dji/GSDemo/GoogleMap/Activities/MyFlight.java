package com.dji.GSDemo.GoogleMap.Activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.dji.GSDemo.GoogleMap.Adapters.PictureAdapter;
import com.dji.GSDemo.GoogleMap.Classes.Flight;
import com.dji.GSDemo.GoogleMap.R;

import java.util.ArrayList;

/**
 * Shows info and pictures of a specific flight, the flight is chosen from list of previous flights that were saved on firestore
 */
public class MyFlight extends AppCompatActivity {
    PictureAdapter myadapter;
    RecyclerView recyclerView;
    Flight flight;
    TextView tvName, tvDate;
    ArrayList<String> linksList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_flight);
        tvDate = findViewById(R.id.tvDates);
        tvName = findViewById(R.id.tvName);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        flight = (Flight) getIntent().getExtras().getSerializable("flight_chosen");
        tvName.setText("" + flight.getName());
        tvDate.setText("Started at: " + flight.getDateStart() + System.getProperty("line.separator") + "Ended: " + flight.getDateEnd());

        if (flight.getLinks().size() > 0) {
            linksList.addAll(flight.getLinks());
            myadapter = new PictureAdapter(MyFlight.this, linksList);
            recyclerView.setAdapter(myadapter);
        } else
            Toast.makeText(this, "No pictures where taken", Toast.LENGTH_SHORT).show();
    }
}
