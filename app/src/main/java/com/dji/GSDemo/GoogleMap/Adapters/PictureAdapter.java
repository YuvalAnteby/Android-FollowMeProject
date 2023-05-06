package com.dji.GSDemo.GoogleMap.Adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dji.GSDemo.GoogleMap.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter to show the pictures that were taken during a flight in the MyFlight activity, only shows a picture without on click method
 */
public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.viewHolder> {
    private Context mContext;
    private ArrayList<String> mitemList;


    public PictureAdapter(Context context, ArrayList<String> itemList) {
        mContext = context;
        mitemList = itemList;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.picture_row, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(viewHolder holder, final int position) {

        Picasso.get().load(mitemList.get(position)).fit().centerInside().into(holder.mImageView);

    }

    @Override
    public int getItemCount() {
        return mitemList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;


        public viewHolder(final View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
        }
    }


}
