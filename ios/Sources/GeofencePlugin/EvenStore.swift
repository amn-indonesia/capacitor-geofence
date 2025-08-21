import Foundation
import Capacitor

class EventStore {
    static let key = "geofence_store_events"

    static func append(_ event: JSObject) {
        var arr = getAll()
        arr.append(event)
        save(arr)
    }
    static func popAll() -> [JSObject] {
        let arr = getAll()
        save([])
        return arr
    }
    static func getAll() -> [JSObject] {
        let defaults = UserDefaults.standard
        if let data = defaults.data(forKey: key),
           let array = try? JSONSerialization.jsonObject(with: data) as? [JSObject] {
            return array
        }
        return []
    }
    private static func save(_ arr: [JSObject]) {
        let defaults = UserDefaults.standard
        let data = try? JSONSerialization.data(withJSONObject: arr)
        defaults.set(data, forKey: key)
    }
}
