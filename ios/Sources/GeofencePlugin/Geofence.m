#import <Capacitor/Capacitor.h>

CAP_PLUGIN(GeofencePlugin, "Geofence",
  CAP_PLUGIN_METHOD(requestPermissions, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(addGeofences, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(removeGeofence, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(removeAllGeofences, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(flushPendingEvents, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(configureSync, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(schedulePeriodicSync, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(cancelPeriodicSync, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(startBackgroundLocation, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(stopBackgroundLocation, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(isBackgroundLocationRunning, CAPPluginReturnPromise);
)
