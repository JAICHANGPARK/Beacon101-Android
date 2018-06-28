package com.dreamwalker.beacon101;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

class BeaconItemViewHolder extends RecyclerView.ViewHolder{
    TextView name, address, uuid, major, minor, distance;

    public BeaconItemViewHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.deviceName);
        address =  itemView.findViewById(R.id.deviceAddress);
        uuid =  itemView.findViewById(R.id.deviceUUID);
        major =  itemView.findViewById(R.id.deviceMajor);
        minor =  itemView.findViewById(R.id.deviceMinor);
        distance =  itemView.findViewById(R.id.deviceDistance);
    }
}

public class BeaconDeviceAdapter extends RecyclerView.Adapter<BeaconItemViewHolder>{
    Context context;
    ArrayList<KNUBeacon> beaconArrayList;

    public BeaconDeviceAdapter(Context context, ArrayList<KNUBeacon> beaconArrayList) {
        this.context = context;
        this.beaconArrayList = beaconArrayList;
    }

    @NonNull
    @Override
    public BeaconItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beacon, parent, false);
        return new BeaconItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconItemViewHolder holder, int position) {

        holder.distance.setText(beaconArrayList.get(position).getDistance());
        holder.name.setText(beaconArrayList.get(position).getName());
        holder.address.setText(beaconArrayList.get(position).getAddress());
        holder.uuid.setText(beaconArrayList.get(position).getUuid());
        holder.major.setText(beaconArrayList.get(position).getMajor());
        holder.minor.setText(beaconArrayList.get(position).getMinor());
    }

    @Override
    public int getItemCount() {
        return beaconArrayList.size();
    }
}
