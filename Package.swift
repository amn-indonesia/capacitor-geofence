// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorBackgroundGeolocation",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorBackgroundGeolocation",
            targets: ["BackgroundGeolocationPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "BackgroundGeolocationPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/BackgroundGeolocationPlugin"),
        .testTarget(
            name: "BackgroundGeolocationPluginTests",
            dependencies: ["BackgroundGeolocationPlugin"],
            path: "ios/Tests/BackgroundGeolocationPluginTests")
    ]
)