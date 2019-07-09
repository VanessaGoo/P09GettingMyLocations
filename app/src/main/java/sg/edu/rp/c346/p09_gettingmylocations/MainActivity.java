package sg.edu.rp.c346.p09_gettingmylocations;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

        Button btnStart, btnStop, btnCheck;
        TextView tvLat, tvLng;
        FusedLocationProviderClient client;
        String folderLocation;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            client = LocationServices.getFusedLocationProviderClient(this);

            btnStart = findViewById(R.id.btnStart);
            btnStop = findViewById(R.id.btnStop);
            btnCheck = findViewById(R.id.btnCheck);
            tvLat = findViewById(R.id.tvLat);
            tvLng = findViewById(R.id.tvLong);

            if (checkPermission()) {

                String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Location";
                File folder = new File(folderLocation);
                if (folder.exists() == false) {
                    boolean result = folder.mkdir();
                    if (result == true) {
                        Log.d("File Read/ Write", "Folder Created");
                    }
                }
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }

            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkPermission() == true) {
                        Task<Location> task = client.getLastLocation();
                        task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    tvLat.setText("Latitude : " + location.getLatitude());
                                    tvLng.setText("Longitude : " + location.getLongitude());
                                    Intent i = new Intent(MainActivity.this, MyService.class);
                                    startService(i);
                                } else {
                                    Toast.makeText(MainActivity.this, "No Last Known Location found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        LocationRequest mLocationRequest = LocationRequest.create();
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        mLocationRequest.setInterval(10000);
                        mLocationRequest.setFastestInterval(5000);
                        mLocationRequest.setSmallestDisplacement(100);
                        LocationCallback mLocationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                if (locationResult != null) {
                                    Location data = locationResult.getLastLocation();
                                    try {
                                        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Location";
                                        File targetFile = new File(folderLocation, "data.txt");
                                        FileWriter writer = new FileWriter(targetFile, true);
                                        writer.write(data.getLatitude() + ", " + data.getLongitude() + "\n");
                                        writer.flush();
                                        writer.close();
                                    } catch (Exception e) {
                                        Log.d("File Start Write", "Failed");
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                        client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    } else {
                        Toast.makeText(MainActivity.this, "Permission not granted to retrieve location info", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                    }
                }
            });
            btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, MyService.class);
                    stopService(i);
                }
            });
            btnCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Location";
                    File targetFile = new File(folderLocation, "data.txt");

                    if (targetFile.exists() == true) {
                        String data = "";
                        try {
                            FileReader reader = new FileReader(targetFile);
                            BufferedReader br = new BufferedReader(reader);
                            String line = br.readLine();
                            while (line != null) {
                                data += line + "\n";
                                line = br.readLine();
                            }
                            br.close();
                            reader.close();
                        } catch (Exception e) {
                            Log.d("File Read", "Failed to read!");
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        private boolean checkPermission() {
            int permissionCheck_Coarse = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int permissionCheck_Fine = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            int permissionCheck_Write = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionCheck_Read = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
                if (permissionCheck_Write == PermissionChecker.PERMISSION_GRANTED && permissionCheck_Read == PermissionChecker.PERMISSION_GRANTED) {
                    return true;
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                    return false;
                }
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                return false;
            }
        }
    }