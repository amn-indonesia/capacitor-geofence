package com.amn.capacitor.geofence;

import android.content.Context;
import androidx.work.*;

public class WorkKicker {
  public static void kick(Context context) {
    OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SyncWorker.class)
      .setConstraints(new Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED).build())
      .build();
    WorkManager.getInstance(context).enqueue(req);
  }
}
