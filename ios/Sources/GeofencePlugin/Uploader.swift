import Foundation
import Capacitor

class Uploader {
    static func uploadPending(completion: @escaping (Bool)->Void) {
        let (urlOpt, headers) = SyncConfig.load()
        guard let urlStr = urlOpt, let url = URL(string: urlStr) else {
            completion(true); return
        }

        let events = EventStore.popAll()
        if events.isEmpty { completion(true); return }

        let payload: JSObject = ["events": events]
        guard let body = try? JSONSerialization.data(withJSONObject: payload) else {
            completion(false); return
        }

        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.httpBody = body
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        for (k,v) in headers { if let s = v as? String { req.setValue(s, forHTTPHeaderField: k) } }

        URLSession.shared.dataTask(with: req) { _, resp, err in
            if let http = resp as? HTTPURLResponse, 200..<300 ~= http.statusCode, err == nil {
                completion(true)
            } else {
                for ev in events { EventStore.append(ev) }
                completion(false)
            }
        }.resume()
    }
}
