package com.amn.capacitor.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import com.getcapacitor.JSObject;

import org.json.JSONException;
public class SyncConfigStore {
  private static final String PREF = "geofence_sync_cfg";
  private static final String KEY_URL = "url";
  private static final String KEY_HEADERS = "headers";

  public static void save(Context ctx, String url, JSObject headers) {
    SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    sp.edit()
      .putString(KEY_URL, url)
      .putString(KEY_HEADERS, headers != null ? headers.toString() : "{}")
      .apply();
  }

  public static Pair load(Context ctx) {
    SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    
    JSObject headers;
    String url;
    try {
        url = sp.getString(KEY_URL, null);
        headers = new JSObject(sp.getString(KEY_HEADERS, "{}"));
    } catch (JSONException e) {
        e.printStackTrace();
        url = "";
        headers = new JSObject(); // fallback to empty
    }

    return new Pair(url, headers);
  }

  public static boolean exists(Context ctx) {
    return load(ctx).url != null;
  }

  public static class Pair {
    public final String url;
    public final JSObject headers;
    public Pair(String url, JSObject headers) { this.url = url; this.headers = headers; }
  }
}
