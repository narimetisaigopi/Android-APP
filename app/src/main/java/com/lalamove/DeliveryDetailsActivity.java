package com.lalamove;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import java.util.ArrayList;
import java.util.List;

import DBFlow.LocalModel;
import DBFlow.Utilities;

public class DeliveryDetailsActivity extends AppCompatActivity implements LostApiClient.ConnectionCallbacks, OnMapReadyCallback {

    LocalModel localModel = null;

    String TAG = "DeliveryDetailsActivity";
    ProgressDialog progressDialog;

    Utilities utilities;

    TextView descriptionTextView;

    SupportMapFragment mapFragment;

    LostApiClient lostApiClient;

    final int LOCATION_PERMISSION = 10;

    GoogleMap googleMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_details);
        initialize();
        if (getIntent().getExtras() != null) {
            localModel = (LocalModel) getIntent().getExtras().getSerializable("data");
            Log.d(TAG, "localModel : " + localModel.toString());
        } else {
            finish();
        }

        showPositions();
        //checkAndRequestPermissions();
    }

    private boolean checkAndRequestPermissions() {
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), LOCATION_PERMISSION);
            return false;
        } else {
            showPositions();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length > 0) {
                showPositions();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void initialize() {
        progressDialog = new ProgressDialog(this);
        utilities = new Utilities(this);

        descriptionTextView = findViewById(R.id.description);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCanceledOnTouchOutside(false);

        getSupportActionBar().setTitle("Delivery Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lostApiClient = new LostApiClient.Builder(this).addConnectionCallbacks(this).build();
        lostApiClient.connect();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected() {
        Location mockLocation = new Location("mock");
        mockLocation.setLatitude(40.7484);
        mockLocation.setLongitude(-73.9857);

        LocationServices.FusedLocationApi.setMockMode(lostApiClient, true);
        LocationServices.FusedLocationApi.setMockLocation(lostApiClient, mockLocation);
    }

    @Override
    public void onConnectionSuspended() {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.d(TAG, "onMapReady");
        showPositions();
    }

    void showPositions() {
        Log.d(TAG, "showPositions");
        descriptionTextView.setText(localModel.getDescription());
        if (googleMap != null) {
            LatLng latLng = new LatLng(Double.parseDouble(localModel.getLat()), Double.parseDouble(localModel.getLng()));
            Log.d(TAG,"latLng : "+latLng.toString());
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Deliver Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

        } else {
            Log.d(TAG, "googleMap is null");
        }

    }
}
