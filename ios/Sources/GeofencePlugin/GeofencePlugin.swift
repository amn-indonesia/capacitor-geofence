import Foundation
import Capacitor
import CoreLocation
import BackgroundTasks
import UIKit

@objc(GeofencePlugin)
public class GeofencePlugin: CAPPlugin, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private var monitored: [String: CLCircularRegion] = [:]
    private var isBgLocRunning = false

    public override func load() {
        super.load()
        manager.delegate = self
        manager.allowsBackgroundLocationUpdates = true
        manager.pausesLocationUpdatesAutomatically = true
    }

    @objc func requestPermissions(_ call: CAPPluginCall) {
        manager.requestAlwaysAuthorization()
        call.resolve()
    }

    @objc func addGeofences(_ call: CAPPluginCall) {
        guard let arr = call.getArray("geofences", JSObject.self) else {
            call.reject("Missing geofences"); return
        }
        for g in arr {
            guard
              let id = g["identifier"] as? String,
              let lat = g["latitude"] as? Double,
              let lng = g["longitude"] as? Double,
              let radius = g["radius"] as? Double
            else { continue }

            let center = CLLocationCoordinate2D(latitude: lat, longitude: lng)
            let region = CLCircularRegion(center: center, radius: radius, identifier: id)
            region.notifyOnEntry = (g["notifyOnEntry"] as? Bool) ?? true
            region.notifyOnExit  = (g["notifyOnExit"]  as? Bool) ?? true
            manager.startMonitoring(for: region)
            monitored[id] = region
        }
        call.resolve()
    }

    @objc func removeGeofence(_ call: CAPPluginCall) {
        guard let id = call.getString("identifier") else { call.reject("identifier required"); return }
        if let r = monitored[id] {
            manager.stopMonitoring(for: r); monitored.removeValue(forKey: id)
        } else if let r = manager.monitoredRegions.first(where: { $0.identifier == id }) as? CLCircularRegion {
            manager.stopMonitoring(for: r)
        }
        call.resolve()
    }

    @objc func removeAllGeofences(_ call: CAPPluginCall) {
        for r in manager.monitoredRegions {
            if let region = r as? CLCircularRegion { manager.stopMonitoring(for: region) }
        }
        monitored.removeAll()
        call.resolve()
    }

    @objc func flushPendingEvents(_ call: CAPPluginCall) {
        call.resolve(["events": []]) // iOS emits immediately; no separate store needed here.
    }

    @objc func configureSync(_ call: CAPPluginCall) {
        guard let url = call.getString("url") else { call.reject("url required"); return }
        let headers = call.getObject("headers") ?? JSObject()
        SyncConfig.save(url: url, headers: headers)
        call.resolve()
    }

    @objc func schedulePeriodicSync(_ call: CAPPluginCall) {
        let minutes = call.getInt("minutes") ?? 60
        scheduleBGRefresh(inMinutes: minutes)
        call.resolve()
    }

    @objc func cancelPeriodicSync(_ call: CAPPluginCall) {
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: "com.example.capacitor.geofence.refresh")
        call.resolve()
    }

    private func scheduleBGRefresh(inMinutes: Int) {
        let req = BGAppRefreshTaskRequest(identifier: "com.example.capacitor.geofence.refresh")
        req.earliestBeginDate = Date(timeIntervalSinceNow: TimeInterval(inMinutes * 60))
        try? BGTaskScheduler.shared.submit(req)
    }

    // Background location controls
    @objc func startBackgroundLocation(_ call: CAPPluginCall) {
        let useSignificant = call.getBool("useSignificantChanges") ?? true
        let distanceFilter = call.getDouble("distanceFilter") ?? 50
        let desired = call.getString("desiredAccuracy") ?? "balanced"

        switch desired {
        case "high": manager.desiredAccuracy = kCLLocationAccuracyBest
        case "low": manager.desiredAccuracy = kCLLocationAccuracyThreeKilometers
        default: manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
        }
        manager.distanceFilter = distanceFilter
        manager.allowsBackgroundLocationUpdates = true
        manager.pausesLocationUpdatesAutomatically = true

        if useSignificant { manager.startMonitoringSignificantLocationChanges() }
        else { manager.startUpdatingLocation() }

        isBgLocRunning = true
        call.resolve()
    }

    @objc func stopBackgroundLocation(_ call: CAPPluginCall) {
        manager.stopMonitoringSignificantLocationChanges()
        manager.stopUpdatingLocation()
        isBgLocRunning = false
        call.resolve()
    }

    @objc func isBackgroundLocationRunning(_ call: CAPPluginCall) {
        call.resolve(["running": isBgLocRunning])
    }

    // CLLocationManagerDelegate (geofence)
    public func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        if let r = region as? CLCircularRegion { emitAndUpload(action: "ENTER", region: r) }
    }
    public func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        if let r = region as? CLCircularRegion { emitAndUpload(action: "EXIT", region: r) }
    }

    // CLLocationManagerDelegate (location)
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.last else { return }
        let data: JSObject = [
            "identifier": "__bg_location__",
            "action": "LOCATION",
            "timestamp": Int(Date().timeIntervalSince1970 * 1000),
            "platform": "ios",
            "latitude": loc.coordinate.latitude,
            "longitude": loc.coordinate.longitude,
            "accuracy": loc.horizontalAccuracy
        ]
        self.notifyListeners("geofence", data: data)
        EventStore.append(data)
        shortUpload()
    }

    private func emitAndUpload(action: String, region: CLCircularRegion) {
        let data: JSObject = [
            "identifier": region.identifier,
            "action": action,
            "timestamp": Int(Date().timeIntervalSince1970 * 1000),
            "platform": "ios"
        ]
        self.notifyListeners("geofence", data: data)
        EventStore.append(data)
        shortUpload()
    }

    private func shortUpload() {
        var bgTask: UIBackgroundTaskIdentifier = .invalid
        bgTask = UIApplication.shared.beginBackgroundTask(withName: "geofence-upload") {
            UIApplication.shared.endBackgroundTask(bgTask); bgTask = .invalid
        }
        Uploader.uploadPending { _ in
            UIApplication.shared.endBackgroundTask(bgTask); bgTask = .invalid
        }
    }
}
