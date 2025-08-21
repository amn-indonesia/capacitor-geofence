import { registerPlugin } from '@capacitor/core';

export type Transition = 'ENTER' | 'EXIT' | 'DWELL' | 'LOCATION';

export interface Geofence {
  identifier: string;
  latitude: number;
  longitude: number;
  radius: number;            // meters
  notifyOnEntry?: boolean;   // default true
  notifyOnExit?: boolean;    // default true
  notifyOnDwell?: boolean;   // Android only
  loiteringDelay?: number;   // ms, Android
}

export interface AddGeofencesOptions { geofences: Geofence[]; }
export interface RemoveGeofenceOptions { identifier: string; }

export interface GeofenceEvent {
  identifier: string;
  action: Transition;
  timestamp: number;
  platform: 'ios' | 'android';
  latitude?: number;
  longitude?: number;
  accuracy?: number;
}

export interface SyncConfig {
  url: string;
  headers?: Record<string, string>;
}

export interface PeriodicSyncOptions { minutes: number; }

export interface BgLocationOptions {
  distanceFilter?: number;           // meters
  interval?: number;                 // ms (Android hint)
  fastestInterval?: number;          // ms (Android hint)
  useSignificantChanges?: boolean;   // iOS only
  desiredAccuracy?: 'low' | 'balanced' | 'high'; // both
}

export interface BgLocationState { running: boolean; }

export interface GeofencePlugin {
  requestPermissions(): Promise<void>;
  addGeofences(options: AddGeofencesOptions): Promise<void>;
  removeGeofence(options: RemoveGeofenceOptions): Promise<void>;
  removeAllGeofences(): Promise<void>;
  flushPendingEvents(): Promise<{ events: GeofenceEvent[] }>;
  configureSync(config: SyncConfig): Promise<void>;
  schedulePeriodicSync(options: PeriodicSyncOptions): Promise<void>;
  cancelPeriodicSync(): Promise<void>;
  startBackgroundLocation(options?: BgLocationOptions): Promise<void>;
  stopBackgroundLocation(): Promise<void>;
  isBackgroundLocationRunning(): Promise<BgLocationState>;
}

export const Geofence = registerPlugin<GeofencePlugin>('Geofence');

export function onGeofence(cb: (e: GeofenceEvent) => void) {
  window.addEventListener('geofence', (ev: any) => cb(ev.detail as GeofenceEvent));
}
