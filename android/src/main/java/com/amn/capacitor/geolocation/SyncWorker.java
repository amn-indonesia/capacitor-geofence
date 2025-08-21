package com.amn.capacitor.geofence;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import org.json.JSONObject;

import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SyncWorker extends Worker {
  private final OkHttpClient client = new OkHttpClient();

  public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
    super(context, params);
  }

  @NonNull @Override
  public Result doWork() {
    // Load config
    SyncConfigStore.Pair cfg = SyncConfigStore.load(getApplicationContext());
    if (cfg.url == null) return Result.success();

    // Pop pending events
    JSArray events = EventStore.popAll(getApplicationContext());
    if (events.length() == 0) return Result.success();

    try {
      // Build JSON payload
      JSObject payload = new JSObject();
      payload.put("events", events);

      RequestBody body = RequestBody.create(
          payload.toString(),
          MediaType.parse("application/json; charset=utf-8")
      );

      // Build request with headers
      Request.Builder rb = new Request.Builder()
          .url(cfg.url)
          .post(body)
          .addHeader("Content-Type", "application/json");

      // JSONObject.keys() returns Iterator<String>, not Iterable
      for (Iterator<String> it = cfg.headers.keys(); it.hasNext(); ) {
        String key = it.next();
        rb.addHeader(key, cfg.headers.getString(key));
      }

      Response resp = client.newCall(rb.build()).execute();
      if (resp.isSuccessful()) {
        return Result.success();
      }

      // On HTTP failure: push events back and retry
      for (int i = 0; i < events.length(); i++) {
        JSONObject jo = events.getJSONObject(i);
        EventStore.append(getApplicationContext(), new JSObject(jo.toString()));
      }
      return Result.retry();

    } catch (Exception ex) {
      // On exception: push events back and retry
      try {
        for (int i = 0; i < events.length(); i++) {
          JSONObject jo = events.getJSONObject(i);
          EventStore.append(getApplicationContext(), new JSObject(jo.toString()));
        }
      } catch (Exception ignore) {}
      return Result.retry();
    }
  }
}
