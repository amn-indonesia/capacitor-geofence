package com.amn.capacitor.geofence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.getcapacitor.JSObject;
import com.google.android.gms.location.*;

public class BgLocationService extends Service {
  private FusedLocationProviderClient client;
  private LocationRequest request;
  private final LocationCallback callback = new LocationCallback() {
    @Override public void onLocationResult(LocationResult result) {
      if (result == null || result.getLastLocation() == null) return;
      JSObject data = new JSObject();
      data.put("identifier", "__bg_location__");
      data.put("action", "LOCATION");
      data.put("timestamp", System.currentTimeMillis());
      data.put("platform", "android");
      data.put("latitude", result.getLastLocation().getLatitude());
      data.put("longitude", result.getLastLocation().getLongitude());
      data.put("accuracy", (double) result.getLastLocation().getAccuracy());

      GeofencePlugin inst = GeofencePlugin.getInstance();
      if (inst != null) inst.emitToJS(data); else EventStore.append(getApplicationContext(), data);

      if (SyncConfigStore.exists(getApplicationContext())) {
        WorkKicker.kick(getApplicationContext());
      }
    }
  };

  @Override public void onCreate() {
    super.onCreate();
    client = LocationServices.getFusedLocationProviderClient(this);
    createChannel();
    startForeground(1002, buildNotification());
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    float distanceFilter = intent != null ? intent.getFloatExtra("distanceFilter", 50f) : 50f;
    long interval = intent != null ? intent.getLongExtra("interval", 60000L) : 60000L;
    long fastest = intent != null ? intent.getLongExtra("fastestInterval", 30000L) : 30000L;
    String desired = intent != null ? intent.getStringExtra("desiredAccuracy") : "balanced";

    int priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY;
    if ("high".equals(desired)) priority = Priority.PRIORITY_HIGH_ACCURACY;
    else if ("low".equals(desired)) priority = Priority.PRIORITY_LOW_POWER;

    request = new LocationRequest.Builder(priority, interval)
      .setMinUpdateIntervalMillis(fastest)
      .setMinUpdateDistanceMeters(distanceFilter)
      .build();

    client.requestLocationUpdates(request, callback, Looper.getMainLooper());
    GeofencePlugin.setBgRunning(true);
    return START_STICKY;
  }

  @Override public void onDestroy() {
    client.removeLocationUpdates(callback);
    GeofencePlugin.setBgRunning(false);
    super.onDestroy();
  }

  @Nullable @Override public IBinder onBind(Intent intent) { return null; }

  private void createChannel() {
    NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationChannel ch = new NotificationChannel("bg_location", "Background Location", NotificationManager.IMPORTANCE_LOW);
    mgr.createNotificationChannel(ch);
  }

  private Notification buildNotification() {
    return new NotificationCompat.Builder(this, "bg_location")
      .setContentTitle("Location tracking active")
      .setContentText("Your location is being updated in background")
      .setSmallIcon(android.R.drawable.ic_menu_mylocation)
      .setOngoing(true)
      .build();
  }
}
