package Adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lalamove.DeliveryDetailsActivity;
import com.lalamove.R;

import java.util.ArrayList;
import java.util.List;

import DBFlow.LocalModel;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    Context context;
    List<LocalModel> localModels;

    final int LOCATION_PERMISSION = 10;

    public HomeAdapter(Context context, List<LocalModel> localModels) {
        this.context = context;
        this.localModels = localModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(localModels.get(position).getImageUrl()).into(holder.imageView);
        holder.description.setText(localModels.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return localModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView description;
        View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = view.findViewById(R.id.imageView);
            description = view.findViewById(R.id.description);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int locationPermission = ContextCompat.checkSelfPermission((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION);
            List<String> listPermissionsNeeded = new ArrayList<>();

            if (locationPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions((Activity) context, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), LOCATION_PERMISSION);

            } else {
                Intent intent = new Intent(context, DeliveryDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", localModels.get(getAdapterPosition()));
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        }
    }


}
