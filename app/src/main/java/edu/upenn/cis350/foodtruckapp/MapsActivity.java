package edu.upenn.cis350.foodtruckapp;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;

    private DatabaseReference databaseRef;
    private FirebaseDatabase database;

    private String TAG = "Map Data";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("Users");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getMarkers();

        // Add a marker in Sydney and move the camera
        LatLng penn = new LatLng(39.952290, -75.197060);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(penn, 15));
        mMap.setMyLocationEnabled(true);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
    }

    /**
     * get markers of vendors to display on the map activity
     */
    private void getMarkers()  {

        // Read from the database
        databaseRef.addValueEventListener(new ValueEventListener() {
            /**
             * listener to get data of vendors from firebase db
             * @param dataSnapshot
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                HashMap<String, Object> values =  (HashMap<String, Object>) dataSnapshot.getValue();
                for (String UserId : values.keySet()) {
                    HashMap<String, Object> userInfo =  (HashMap<String, Object>) values.get(UserId);

                    if (userInfo.get("Type").equals("Vendor")
                            && userInfo.containsKey("Location")
                            && userInfo.containsKey("Name Of Food Truck")
                            && userInfo.containsKey("Type Of Food")
                            && userInfo.containsKey("Active")) {

                        // get info from vendor
                        String vendorName = (String) userInfo.get("Name Of Food Truck");
                        String foodType = (String) userInfo.get("Type Of Food");
                        String coords = (String) userInfo.get("Location");
                        String uniqueID = (String) userInfo.get("UniqueID");
                        boolean isActive = (boolean)userInfo.get("Active");
                        Location l = new Location(coords);

                        // make green marker if active
                        if (isActive) {
                            // add marker to map
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(l.getLat(), l.getLng()))
                                    .snippet(foodType)
                                    .title(vendorName)
                                    .icon(BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_GREEN)))
                                    .setTag(uniqueID);
                        }
                        else {
                        // add marker to map
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(l.getLat(), l.getLng()))
                                .snippet(foodType)
                                .title(vendorName))
                                .setTag(uniqueID);
                        }

                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Log.d("markerOnClick", "marker clicked");
                                String uniqueID = (String) marker.getTag();
                                Intent i = new Intent(MapsActivity.this, VendorProfileForCustomerActivity.class);
                                i.putExtra("vendorUniqueID", uniqueID);
                                startActivity(i);
                            }
                        });

                    }
                }

            }

            /**
             * handles error from db
             * @param error
             */
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * basic location class with coordinates and getters and setters
     */
    public class Location {
        private double lat;
        private double lng;

        private Location(String coords) {
            String[] tks = coords.split(",");
            this.lat = Double.parseDouble(tks[0]);
            this.lng = Double.parseDouble(tks[1]);
        }

        private Location(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

    }
}
