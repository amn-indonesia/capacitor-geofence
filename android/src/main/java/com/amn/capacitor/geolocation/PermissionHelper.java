package com.amn.capacitor.geofence;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
  public static boolean hasForeground(Context ctx) {
    return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED;
  }
  public static boolean hasBackground(Context ctx) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
          == PackageManager.PERMISSION_GRANTED;
    }
    return true;
  }
}
