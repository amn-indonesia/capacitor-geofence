package com.amn.capacitor.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import org.json.JSONException;

public class EventStore {
  private static final String PREF = "geofence_store";
  private static final String KEY = "events";

  public static synchronized void append(Context ctx, JSObject event) {
    SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
     try {
      JSArray arr = new JSArray(sp.getString(KEY, "[]"));
      arr.put(event);
      sp.edit().putString(KEY, arr.toString()).apply();  
    } catch (JSONException e) {
        e.printStackTrace();
    }
  }

  public static synchronized JSArray popAll(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    try {
      String json = sp.getString(KEY, "[]");
      sp.edit().putString(KEY, "[]").apply();
      return new JSArray(json);
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }
  }
}
