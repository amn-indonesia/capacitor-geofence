# capacitor-geofence

Get geofence background process

## Install

```bash
npm install capacitor-geofence
npx cap sync
```

## API

<docgen-index>

* [`requestPermissions()`](#requestpermissions)
* [`addGeofences(...)`](#addgeofences)
* [`removeGeofence(...)`](#removegeofence)
* [`removeAllGeofences()`](#removeallgeofences)
* [`flushPendingEvents()`](#flushpendingevents)
* [`configureSync(...)`](#configuresync)
* [`schedulePeriodicSync(...)`](#scheduleperiodicsync)
* [`cancelPeriodicSync()`](#cancelperiodicsync)
* [`startBackgroundLocation(...)`](#startbackgroundlocation)
* [`stopBackgroundLocation()`](#stopbackgroundlocation)
* [`isBackgroundLocationRunning()`](#isbackgroundlocationrunning)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### requestPermissions()

```typescript
requestPermissions() => Promise<void>
```

--------------------


### addGeofences(...)

```typescript
addGeofences(options: AddGeofencesOptions) => Promise<void>
```

| Param         | Type                                                                |
| ------------- | ------------------------------------------------------------------- |
| **`options`** | <code><a href="#addgeofencesoptions">AddGeofencesOptions</a></code> |

--------------------


### removeGeofence(...)

```typescript
removeGeofence(options: RemoveGeofenceOptions) => Promise<void>
```

| Param         | Type                                                                    |
| ------------- | ----------------------------------------------------------------------- |
| **`options`** | <code><a href="#removegeofenceoptions">RemoveGeofenceOptions</a></code> |

--------------------


### removeAllGeofences()

```typescript
removeAllGeofences() => Promise<void>
```

--------------------


### flushPendingEvents()

```typescript
flushPendingEvents() => Promise<{ events: GeofenceEvent[]; }>
```

**Returns:** <code>Promise&lt;{ events: GeofenceEvent[]; }&gt;</code>

--------------------


### configureSync(...)

```typescript
configureSync(config: SyncConfig) => Promise<void>
```

| Param        | Type                                              |
| ------------ | ------------------------------------------------- |
| **`config`** | <code><a href="#syncconfig">SyncConfig</a></code> |

--------------------


### schedulePeriodicSync(...)

```typescript
schedulePeriodicSync(options: PeriodicSyncOptions) => Promise<void>
```

| Param         | Type                                                                |
| ------------- | ------------------------------------------------------------------- |
| **`options`** | <code><a href="#periodicsyncoptions">PeriodicSyncOptions</a></code> |

--------------------


### cancelPeriodicSync()

```typescript
cancelPeriodicSync() => Promise<void>
```

--------------------


### startBackgroundLocation(...)

```typescript
startBackgroundLocation(options?: BgLocationOptions | undefined) => Promise<void>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#bglocationoptions">BgLocationOptions</a></code> |

--------------------


### stopBackgroundLocation()

```typescript
stopBackgroundLocation() => Promise<void>
```

--------------------


### isBackgroundLocationRunning()

```typescript
isBackgroundLocationRunning() => Promise<BgLocationState>
```

**Returns:** <code>Promise&lt;<a href="#bglocationstate">BgLocationState</a>&gt;</code>

--------------------


### Interfaces


#### AddGeofencesOptions

| Prop            | Type                    |
| --------------- | ----------------------- |
| **`geofences`** | <code>Geofence[]</code> |


#### Geofence

| Prop                 | Type                 |
| -------------------- | -------------------- |
| **`identifier`**     | <code>string</code>  |
| **`latitude`**       | <code>number</code>  |
| **`longitude`**      | <code>number</code>  |
| **`radius`**         | <code>number</code>  |
| **`notifyOnEntry`**  | <code>boolean</code> |
| **`notifyOnExit`**   | <code>boolean</code> |
| **`notifyOnDwell`**  | <code>boolean</code> |
| **`loiteringDelay`** | <code>number</code>  |


#### RemoveGeofenceOptions

| Prop             | Type                |
| ---------------- | ------------------- |
| **`identifier`** | <code>string</code> |


#### GeofenceEvent

| Prop             | Type                                              |
| ---------------- | ------------------------------------------------- |
| **`identifier`** | <code>string</code>                               |
| **`action`**     | <code><a href="#transition">Transition</a></code> |
| **`timestamp`**  | <code>number</code>                               |
| **`platform`**   | <code>'ios' \| 'android'</code>                   |
| **`latitude`**   | <code>number</code>                               |
| **`longitude`**  | <code>number</code>                               |
| **`accuracy`**   | <code>number</code>                               |


#### SyncConfig

| Prop          | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`url`**     | <code>string</code>                                             |
| **`headers`** | <code><a href="#record">Record</a>&lt;string, string&gt;</code> |


#### PeriodicSyncOptions

| Prop          | Type                |
| ------------- | ------------------- |
| **`minutes`** | <code>number</code> |


#### BgLocationOptions

| Prop                        | Type                                       |
| --------------------------- | ------------------------------------------ |
| **`distanceFilter`**        | <code>number</code>                        |
| **`interval`**              | <code>number</code>                        |
| **`fastestInterval`**       | <code>number</code>                        |
| **`useSignificantChanges`** | <code>boolean</code>                       |
| **`desiredAccuracy`**       | <code>'low' \| 'balanced' \| 'high'</code> |


#### BgLocationState

| Prop          | Type                 |
| ------------- | -------------------- |
| **`running`** | <code>boolean</code> |


### Type Aliases


#### Transition

<code>'ENTER' | 'EXIT' | 'DWELL' | 'LOCATION'</code>


#### Record

Construct a type with a set of properties K of type T

<code>{ [P in K]: T; }</code>

</docgen-api>
