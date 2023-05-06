package com.dji.GSDemo.GoogleMap.Adapters;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dji.GSDemo.GoogleMap.Activities.MyFlight;
import com.dji.GSDemo.GoogleMap.Classes.Flight;
import com.dji.GSDemo.GoogleMap.R;

import java.util.ArrayList;

/**
 * Adapter for recyclerView on Allflight Activity, will show the name of the flight, email of the user that made it and amount of
 * pictures taken during the flight
 */
public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.viewHolder> {
    private Context mContext;
    private ArrayList<Flight> mitemList;


    public FlightAdapter(Context context, ArrayList<Flight> itemList) {
        mContext = context;
        mitemList = itemList;

    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.flight_row, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(viewHolder holder, final int position) {
        final Flight currentItem = mitemList.get(position);

        holder.tvName.setText(currentItem.getName());
        holder.tvEmail.setText(currentItem.getEmail());
        holder.tvPic.setText("Pics: " + currentItem.getCountPictures());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forItem = new Intent(mContext, MyFlight.class);
                forItem.putExtra("flight_chosen", mitemList.get(position));
                mContext.startActivity(forItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mitemList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        public TextView tvName, tvEmail, tvPic;


        public viewHolder(final View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            tvPic = (TextView) itemView.findViewById(R.id.tvPic);
        }
    }

}
