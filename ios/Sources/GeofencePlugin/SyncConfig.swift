import Foundation
import Capacitor

class SyncConfig {
    static let key = "geofence_sync_cfg"
    static func save(url: String, headers: JSObject) {
        let cfg: JSObject = ["url": url, "headers": headers]
        let data = try? JSONSerialization.data(withJSONObject: cfg)
        UserDefaults.standard.set(data, forKey: key)
    }
    static func load() -> (String?, JSObject) {
        if let data = UserDefaults.standard.data(forKey: key),
           let obj = try? JSONSerialization.jsonObject(with: data) as? JSObject {
            let url = obj["url"] as? String
            let headers = (obj["headers"] as? JSObject) ?? JSObject()
            return (url, headers)
        }
        return (nil, JSObject())
    }
}
