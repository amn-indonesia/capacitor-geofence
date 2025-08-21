package com.amn.capacitor.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.getcapacitor.JSObject;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    GeofencingEvent ge = GeofencingEvent.fromIntent(intent);
    if (ge == null || ge.hasError()) return;

    String action;
    switch (ge.getGeofenceTransition()) {
      case Geofence.GEOFENCE_TRANSITION_ENTER: action = "ENTER"; break;
      case Geofence.GEOFENCE_TRANSITION_EXIT:  action = "EXIT";  break;
      case Geofence.GEOFENCE_TRANSITION_DWELL: action = "DWELL"; break;
      default: return;
    }

    if (ge.getTriggeringGeofences() == null) return;

    for (Geofence gf : ge.getTriggeringGeofences()) {
      JSObject data = new JSObject();
      data.put("identifier", gf.getRequestId());
      data.put("action", action);
      data.put("timestamp", System.currentTimeMillis());
      data.put("platform", "android");
      if (ge.getTriggeringLocation() != null) {
        data.put("latitude", ge.getTriggeringLocation().getLatitude());
        data.put("longitude", ge.getTriggeringLocation().getLongitude());
      }

      GeofencePlugin inst = GeofencePlugin.getInstance();
      if (inst != null) inst.emitToJS(data); else EventStore.append(context, data);
    }

    if (SyncConfigStore.exists(context)) {
      WorkKicker.kick(context);
    }
  }
}
