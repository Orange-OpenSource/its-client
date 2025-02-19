// swift-tools-version: 6.0
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "ITSClient",
    platforms: [
        .macOS(.v13),
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "ITSCore",
            targets: ["ITSCore"]),
        .library(
            name: "ITSMobility",
            targets: ["ITSMobility"]),
    ],
    dependencies: [
        .package(url: "https://github.com/swift-server-community/mqtt-nio.git", from: "2.11.0"),
        .package(url: "https://github.com/open-telemetry/opentelemetry-swift.git", from: "1.12.1")
    ],
    targets: [
        .target(
            name: "ITSCore",
            dependencies:
            [
                .product(name: "MQTTNIO", package: "mqtt-nio"),
                .product(name: "OpenTelemetryProtocolExporterHTTP", package: "opentelemetry-swift"),
            ]
        ),
        .testTarget(
            name: "ITSCoreTests",
            dependencies: ["ITSCore"]
        ),
        .target(
            name: "ITSMobility",
            dependencies: ["ITSCore"]
        ),
    ]
)
