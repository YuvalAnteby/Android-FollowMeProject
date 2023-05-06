package com.dji.GSDemo.GoogleMap.Adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dji.GSDemo.GoogleMap.Activities.SaveAct;
import com.dji.GSDemo.GoogleMap.Classes.Preview;
import com.dji.GSDemo.GoogleMap.R;

import java.util.ArrayList;

/**
 * Adapter to show pictures taken during last flight while saving in SaveAct, contains bitmap of the picture and the time the picture was
 * taken at. User has an option to delete the picture from the phone and drone (will not upload the picture to firestore)
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.viewHolder> {
    private Context mContext;
    private ArrayList<Preview> mitemList;


    public RecyclerViewAdapter(Context context, ArrayList<Preview> itemList) {
        mContext = context;
        mitemList = itemList;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_item, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(viewHolder holder, final int position) {
        final Preview currentItem = mitemList.get(position);

        holder.mImageView.setImageBitmap(currentItem.getBitmap());

        holder.tvDate.setText("Taken at: " + currentItem.getDateCreated());

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAct.deleteFileByIndex(position, mContext);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mitemList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView tvDate;
        public Button btnDelete;

        public viewHolder(final View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            btnDelete = (Button) itemView.findViewById(R.id.btnDelete);

        }
    }


}
