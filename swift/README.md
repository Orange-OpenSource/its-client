# ITSClient

![Build Status](https://github.com/Orange-OpenSource/its-client/workflows/Swift%20ITSClient/badge.svg)

This library is divided in two parts:

- The Core which provides a MQTT client and an OpenTelemetry client.
- The Mobility built on top of the Core which is an ETSI Intelligent Transport System messages implementation using JSON.

## Installation

Add a package by selecting _File/Add Package Dependencies…_ in Xcode’s menu bar.

Search for the _ITSClient_ using the repository URL:

```
https://github.com/Orange-OpenSource/its-client
```

Set the dependency rule (generally _Up to Next Major_) and the project to add the SDK and its dependencies.

**Important:** Due to the mono repo constraints, the tags are suffixed by -swift (for example: 0.1.0-swift).

Choose the `ITSMobility` product and your project target to add the SDK. You can also choose `ITSCore` product if you just want to use the MQTT and OpenTelemetry client but this documentation will focus on the Mobility usage.

## Usage

### General purpose

You just need to import both ITSMobility and ITSCore frameworks in your code and initialize a `Mobility` object.

```swift
import ITSCore
import ITSMobility

let mobility = Mobility()
```

### Start

#### Start with a manual configuration

First, build a `CoreConfiguration` with MQTT and OpenTelemetry configurations.

```swift
let mqttClientConfiguration = MQTTClientConfiguration(host: "host.com",
                                                      port: 1234,
                                                      clientIdentifier: "clientID",
                                                      userName: "username",
                                                      password: "password",
                                                      useSSL: true)
let telemetryURL = URL(string: "https://mytelemetry.com")!
let telemetryClientConfiguration = TelemetryClientConfiguration(url: telemetryURL,
                                                                serviceName: "service")

let coreConfiguration = CoreConfiguration(mqttClientConfiguration: mqttClientConfiguration,
                                          telemetryClientConfiguration: telemetryClientConfiguration)
```

Then, build a `MobilityConfiguration` based on your `CoreConfiguration`.

```swift
let stationID = UInt32.random(in: 0...UInt32.max)
let mobilityConfiguration = MobilityConfiguration(coreConfiguration: coreConfiguration,
                                                   stationID: stationID)
```

Finally, call the `start` method to start the Mobility.

```swift
try await mobility.start(mobilityConfiguration: mobilityConfiguration)
```

#### Start with a bootstrapped configuration

You can also use a bootstrap server to retrieve the configuration.

```swift
let url = URL(string: "https://mybootstrap.com")!
let service = BootstrapService(url: url)
let bootstrapConfiguration = BootstrapConfiguration(identifier: "swift",
                                                    user: "username",
                                                    password: "password",
                                                    role: "external-app")
let bootstrap = try? await service.bootstrap(bootstrapConfiguration: bootstrapConfiguration)
```

Then you can call the other `start` method.

```swift
let stationID = UInt32.random(in: 0...UInt32.max)
try await mobility.start(bootstrap: bootstrap,
                         stationID: stationID)
```

### Receive road objects

#### Receive road alarms

To receive road alarms, you must implement the `RoadAlarmChangeObserver` protocol.

```swift
final class MobilityRoadAlarmChangeObserver: RoadAlarmChangeObserver {
    func didCreate(_ roadAlarm: RoadAlarm) {}

    func didUpdate(_ roadAlarm: RoadAlarm) {}

    func didDelete(_ roadAlarm: RoadAlarm) {}
}
```

Then set the observer to the `Mobility` instance.

```swift
let observer = await MobilityRoadAlarmChangeObserver()
await mobility.setRoadAlarmObserver(observer)
```

**Important:** You must retain the observer.

Now you must update the road alarm region of interest to receive alarms in a zone which depends on a GPS location.

```swift
let latitude = 48.866667
let longitude = 2.333333
try await mobility.updateRoadAlarmRegionOfInterest(latitude: latitude,
                                                   longitude: longitude,
                                                   zoomLevel: 15)
```

#### Receive road users

To receive road users, you must implement the `RoadUserChangeObserver` protocol.

```swift
final class MobilityRoadUserChangeObserver: RoadUserChangeObserver {
    func didCreate(_ roadUser: RoadUser) {}

    func didUpdate(_ roadUser: RoadUser) {}

    func didDelete(_ roadUser: RoadUser) {}
}
```

Then set the observer to the `Mobility` instance.

```swift
let observer = await MobilityRoadUserChangeObserver()
await mobility.setRoadUserObserver(observer)
```

**Important:** You must retain the observer.

Now you must update the road user region of interest to receive users in a zone which depends on a GPS location.

```swift
let latitude = 48.866667
let longitude = 2.333333
try await mobility.updateRoadUserRegionOfInterest(latitude: latitude,
                                                  longitude: longitude,
                                                  zoomLevel: 15)
```

### Send information

You can update the zoom level for the information you sent with the method `setReportZoomLevel`. The default value is 22.

```swift
await mobility.setReportZoomLevel(18)
```

#### Send user position

You can use the `sendUserPosition` method to send your position.

```swift
let latitude = 48.866667
let longitude = 2.333333
try await mobility.sendUserPosition(stationType: .pedestrian,
                                    latitude: latitude,
                                    longitude: longitude,
                                    altitude: 35,
                                    heading: 300,
                                    speed: 20)
```

If you want to send more detailled information, you can send the underlying ETSI object with the `sendCAM` method.

```swift
let now = Date()
let camMessage = CAMMessage(...)
let cam = CAM(message: camMessage,
              sourceUUID: sourceUUID(from: .pedestrian,
              stationID: stationID),
              timestamp: now)
try await mobility.sendCAM(cam)
```

#### Send alarm

You can use the `sendAlarm` method to send an alarm.

```swift
let latitude = 48.866667
let longitude = 2.333333
try await mobility.sendAlarm(stationType: .pedestrian,
                             latitude: latitude,
                             longitude: longitude,
                             altitude: 35,
                             cause: .roadworks(subcause: .majorRoadworks))
```

If you want to send more detailled information, you can send the underlying ETSI object with the `sendDENM` method.

```swift
let now = Date()
let denmMessage = DENMMessage(...)
let denm = DENM(message: denmMessage,
                sourceUUID: "",
                timestamp: now)
try await mobility.sendDENM(denm)
```

### Stop

You can stop `Mobility` with the `stop` method.

```swift
await mobility.stop()
```
