package com.example.mymaps;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText bus;
    private Button button;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<LatLng> latlngs = new ArrayList<>();
    private MarkerOptions markerOptions = new MarkerOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        bus = findViewById(R.id.inBus);
        button = findViewById(R.id.button);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
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

        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View view) {
                AsyncTaskRunner runner = new AsyncTaskRunner();
                String busNumber = bus.getText().toString();
                runner.execute(busNumber);
            }
        });


//        latlngs.add(new LatLng(37.994129, 23.731960));
//        latlngs.add(new LatLng(37.9757, 23.7339));
//
//        int i = 1;
//        for (LatLng point : latlngs) {
//            markerOptions.position(point);
//            markerOptions.title("POI" + i);
//
//            markerOptions.snippet("test");
//            markers.add(mMap.addMarker(markerOptions));
//            i++;
//        }
//        i = 1;
//        for (Marker m : markers) {
//            m.setTag(i);
//            i++;
//        }


        LatLng athens = new LatLng(37.994129, 23.731960);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(athens));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

//        final Button button = findViewById(R.id.hide_button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                for (Marker m : markers) {
//                    if (m.getTag().equals(1)) {
//                        m.setVisible(!m.isVisible());
//                    }
//                }
//                if (markers.get(0).isVisible()) {
//                    button.setText("Hide");
//                } else {
//                    button.setText("Hidden");
//                }
//            }
//        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setTrafficEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //Marker m = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //m.setRotation((float)5);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        private String resp;

        private String busNumber;
        private String[] busLines = {"1151", "821", "750", "817", "818", "974", "1113", "816", "804", "1219", "1220", "938", "831", "819", "1180", "868", "824", "825", "1069", "1077"};
        private String[] busLinesCon = {"021", "022", "024", "025", "026", "027", "032", "035", "036", "036", "036", "040", "046", "049", "051", "054", "057", "060", "1", "10"};

//        private Socket requestSocket;

        @Override
        protected String doInBackground(String... params) {
            Socket requestSocket = null;
            PrintStream out = null;
            Scanner in = null;

            try {
                progressDialog.dismiss();
                requestSocket = new Socket("192.168.1.22", 3421);

                out = new PrintStream(requestSocket.getOutputStream());
                in = new Scanner(requestSocket.getInputStream());

                out.println(-1);
                String broker_buses = in.nextLine();
                String[] tokens = broker_buses.split("], ");

                String broker1_buses = tokens[0];
                String broker2_buses = tokens[1];
                String broker3_buses = tokens[2];

                String broker1 = broker1_buses.substring(1, broker1_buses.indexOf("="));
                broker1_buses = broker1_buses.substring(broker1_buses.indexOf("="));

                String broker2 = broker2_buses.substring(0, broker2_buses.indexOf("="));
                broker2_buses = broker2_buses.substring(broker2_buses.indexOf("="));

                String broker3 = broker3_buses.substring(0, broker3_buses.indexOf("="));
                broker3_buses = broker3_buses.substring(broker3_buses.indexOf("="));

                requestSocket.close();

                if (broker1_buses.contains(busNumber)) {
                    requestSocket = new Socket(broker1.substring(0, broker1.length() - 4), Integer.parseInt(broker1.substring(broker1.length() - 4)));
                } else if (broker2_buses.contains(busNumber)) {
                    requestSocket = new Socket(broker2.substring(0, broker2.length() - 4), Integer.parseInt(broker2.substring(broker2.length() - 4)));
                } else if (broker3_buses.contains(busNumber)) {
                    requestSocket = new Socket(broker3.substring(0, broker3.length() - 4), Integer.parseInt(broker3.substring(broker3.length() - 4)));
                }

                out = new PrintStream(requestSocket.getOutputStream());
                in = new Scanner(requestSocket.getInputStream());

                out.println(1);
                out.flush();

                in.nextLine();

                out.println(busNumber);
                out.flush();

                in.nextLine();

                do {
                    publishProgress(in.nextLine());
                } while (in.nextLine().compareTo("stop") != 0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            resp = "No more Data";
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            mMap.clear();
        }

        @Override
        protected void onPreExecute() {
            busNumber = bus.getText().toString();
            progressDialog = ProgressDialog.show(MapsActivity.this, "Running...", "Searching position for Bus number: " + busNumber);
            for (int i = 0; i < busLinesCon.length; i++) {
                if (busNumber.compareTo(busLinesCon[i]) == 0) {
                    busNumber = busLines[i];
                    break;
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
            LatLng latLng;
            latLng = new LatLng(Double.parseDouble(text[0].substring(0, text[0].indexOf(" "))), Double.parseDouble(text[0].substring(text[0].indexOf(" ") + 1)));

            markerOptions.position(latLng);
            markerOptions.title(busNumber);
            markerOptions.snippet("test");
            mMap.clear();
            mMap.addMarker(markerOptions);
        }
    }
}
