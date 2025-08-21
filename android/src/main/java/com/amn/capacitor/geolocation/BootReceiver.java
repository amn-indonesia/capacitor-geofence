package com.amn.capacitor.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.concurrent.TimeUnit;
import androidx.work.*;

public class BootReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      if (SyncConfigStore.exists(context)) {
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(SyncWorker.class, 60, TimeUnit.MINUTES).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
          "geofence-periodic-sync",
          ExistingPeriodicWorkPolicy.KEEP,
          req
        );
      }
      // TODO: re-add geofences you persist elsewhere (optional)
    }
  }
}
