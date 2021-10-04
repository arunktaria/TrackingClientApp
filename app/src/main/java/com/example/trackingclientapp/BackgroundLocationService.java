package com.example.trackingclientapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;

public class BackgroundLocationService extends Service {
    LocationManager manager;
    DatabaseReference reference;
    NotificationManager notificationManager;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
       //creating and assing objects values

        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(0);
        locationRequest.setInterval(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        reference = FirebaseDatabase.getInstance().getReference().child("UserLocation");

        super.onCreate();
    }

    //starting the service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String str=intent.getAction();
        if (str.equals("start"))
        {
            //start getting location using method
            startLocationTracing();
            Toast.makeText(this, "service is started!", Toast.LENGTH_SHORT).show();
        }else if (str.equals("stop"))
        {
            stopLocationTracing();
            Toast.makeText(this, "service is stopped!", Toast.LENGTH_SHORT).show();
        }




        return START_STICKY;
    }

    //method for creating notification to aware the user that his/her location is tracking right now
    private void startLocationTracing() {
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getService(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder  builder=new NotificationCompat.Builder(this,"12")
                .setContentText("Location Tracing..")
                .setContentTitle("Location App")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH);



        if (Build.VERSION_CODES.O < Build.VERSION.SDK_INT) {
            NotificationChannel notificationChannel = new NotificationChannel("12", "NotificationLocation", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Running...");
            notificationManager.createNotificationChannel(notificationChannel);

        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationcallback, Looper.myLooper());
        startForeground(12,builder.build());
    }


//call back for taking last location of user
    LocationCallback locationcallback=new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            reference.setValue(locationResult.getLastLocation());
             super.onLocationResult(locationResult);
        }
    };


    //stops when user stop services
    public void stopLocationTracing()
    {
        fusedLocationProviderClient.removeLocationUpdates(locationcallback);
        stopForeground(true);
        stopSelf();

    }

}


