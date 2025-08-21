package com.amn.capacitor.geofence;

import android.app.PendingIntent;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@CapacitorPlugin(name = "Geofence")
public class GeofencePlugin extends Plugin {

  private GeofencingClient client;
  private PendingIntent geofencePI;
  private static GeofencePlugin instance;
  private static boolean bgRunning = false;

  public static GeofencePlugin getInstance() { return instance; }
  public static void setBgRunning(boolean v) { bgRunning = v; }

  @Override
  public void load() {
    client = LocationServices.getGeofencingClient(getContext());
    instance = this;
  }

  private PendingIntent getGeofencePendingIntent() {
    if (geofencePI == null) {
      Intent intent = new Intent(getContext(), GeofenceBroadcastReceiver.class);
      geofencePI = PendingIntent.getBroadcast(
        getContext(), 9901, intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
      );
    }
    return geofencePI;
  }

  // -------- Inline permission helpers (no external PermissionHelper needed)

  private boolean hasForegroundLocation() {
    return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED;
  }

  private boolean hasBackgroundLocation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
          == PackageManager.PERMISSION_GRANTED;
    }
    return true; // background permission not required < Android 10
  }

  // ================= JS API =================

  @PluginMethod
  public void requestPermissions(PluginCall call) {
    // Permissions should be requested from the app side (Capacitor permission APIs/UX).
    call.resolve();
  }

  @PluginMethod
  public void addGeofences(PluginCall call) {
    JSArray arr = call.getArray("geofences");
    if (arr == null) { call.reject("Missing geofences"); return; }
    if (!hasForegroundLocation() || !hasBackgroundLocation()) {
      call.reject("Location permission (foreground+background) required"); return;
    }

    try {
      List<Geofence> geofences = new ArrayList<>();
      for (int i = 0; i < arr.length(); i++) {
        // ↓↓↓ Convert JSONObject -> JSObject
        JSONObject jo = arr.getJSONObject(i);
        JSObject g = new JSObject(jo.toString());

        String id = g.optString("identifier", null);
        double lat = g.optDouble("latitude");
        double lng = g.optDouble("longitude");
        float radius = (float) g.optDouble("radius");

        if (id == null) continue;

        int transitions = 0;
        if (g.optBoolean("notifyOnEntry", true)) transitions |= Geofence.GEOFENCE_TRANSITION_ENTER;
        if (g.optBoolean("notifyOnExit",  true)) transitions |= Geofence.GEOFENCE_TRANSITION_EXIT;
        if (g.optBoolean("notifyOnDwell", false)) transitions |= Geofence.GEOFENCE_TRANSITION_DWELL;

        int loiter = g.optInt("loiteringDelay", 300000);

        geofences.add(new Geofence.Builder()
          .setRequestId(id)
          .setCircularRegion(lat, lng, radius)
          .setTransitionTypes(transitions)
          .setLoiteringDelay(loiter)
          .setExpirationDuration(Geofence.NEVER_EXPIRE)
          .build());
      }

      GeofencingRequest req = new GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofences(geofences)
        .build();

      client.addGeofences(req, getGeofencePendingIntent())
        .addOnSuccessListener(v -> call.resolve())
        .addOnFailureListener(e -> call.reject(e.getMessage(), e));

    } catch (Exception ex) {
      call.reject(ex.getMessage(), ex);
    }
  }

  @PluginMethod
  public void removeGeofence(PluginCall call) {
    String id = call.getString("identifier");
    if (id == null) { call.reject("identifier required"); return; }
    client.removeGeofences(java.util.Collections.singletonList(id))
      .addOnSuccessListener(v -> call.resolve())
      .addOnFailureListener(e -> call.reject(e.getMessage(), e));
  }

  @PluginMethod
  public void removeAllGeofences(PluginCall call) {
    client.removeGeofences(getGeofencePendingIntent())
      .addOnSuccessListener(v -> call.resolve())
      .addOnFailureListener(e -> call.reject(e.getMessage(), e));
  }

  @PluginMethod
  public void flushPendingEvents(PluginCall call) {
    JSArray arr = EventStore.popAll(getContext());
    JSObject res = new JSObject(); res.put("events", arr);
    call.resolve(res);
  }

  @PluginMethod
  public void configureSync(PluginCall call) {
    String url = call.getString("url");
    JSObject headers = call.getObject("headers");
    if (headers == null) headers = new JSObject();
    if (url == null || url.isEmpty()) { call.reject("url required"); return; }
    SyncConfigStore.save(getContext(), url, headers);
    call.resolve();
  }

  @PluginMethod
  public void schedulePeriodicSync(PluginCall call) {
    Integer m = call.getInt("minutes");
    int minutes = (m != null) ? m : 60;
    if (minutes < 15) minutes = 15; // WorkManager minimum

    PeriodicWorkRequest req = new PeriodicWorkRequest
      .Builder(SyncWorker.class, minutes, TimeUnit.MINUTES)
      .build();

    WorkManager.getInstance(getContext()).enqueueUniquePeriodicWork(
      "geofence-periodic-sync",
      ExistingPeriodicWorkPolicy.UPDATE,
      req
    );
    call.resolve();
  }

  @PluginMethod
  public void cancelPeriodicSync(PluginCall call) {
    WorkManager.getInstance(getContext()).cancelUniqueWork("geofence-periodic-sync");
    call.resolve();
  }

  @PluginMethod
  public void startBackgroundLocation(PluginCall call) {
    if (!hasForegroundLocation() || !hasBackgroundLocation()) {
      call.reject("Location permission (foreground+background) required"); return;
    }

    JSObject opts = call.getData();
    Intent intent = new Intent(getContext(), BgLocationService.class);
    intent.putExtra("distanceFilter", (float) opts.optDouble("distanceFilter", 50.0));
    intent.putExtra("interval",        opts.optInt("interval", 60000));
    intent.putExtra("fastestInterval", opts.optInt("fastestInterval", 30000));
    intent.putExtra("desiredAccuracy", opts.optString("desiredAccuracy", "balanced"));

    androidx.core.content.ContextCompat.startForegroundService(getContext(), intent);
    call.resolve();
  }

  @PluginMethod
  public void stopBackgroundLocation(PluginCall call) {
    Intent intent = new Intent(getContext(), BgLocationService.class);
    getContext().stopService(intent);
    call.resolve();
  }

  @PluginMethod
  public void isBackgroundLocationRunning(PluginCall call) {
    JSObject res = new JSObject(); res.put("running", bgRunning);
    call.resolve(res);
  }

  // Receiver/Service can call this when app is alive
  public void emitToJS(JSObject data) {
    notifyListeners("geofence", data);
  }
}
