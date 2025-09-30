JSON schemas
==========
ETSI messages
------------

### [CAM : Cooperative Awareness Message](cam)

Exchanged periodically between vehicles within Intelligent Transportation Systems (ITS) at a frequency ranging
from 1 Hz to 10 Hz. It contains crucial information such as position, speed, acceleration, and other data regarding the
vehicle's status. CAMs enable the construction of a real-time mutual situational awareness, thereby enhancing traffic
safety and efficiency, depending on the number of vehicles that emit them.

### [CPM : Collective Perception Message](cpm)

Periodically transmitted message that contains information from vehicle sensors or roadside sensors.
This data is shared to create an extended cooperative perception of the road environment, allowing for the consideration
of unconnected actors such as vulnerable road users or other vehicles.

### [DENM : Decentralized Emergency Notification Message](denm)

Instant alert message within Intelligent Transportation Systems (ITS). It is emitted when local environmental conditions
change suddenly, such as the presence of obstacles on the road, accidents, roadworks, or adverse weather conditions.
DENMs are designed to notify drivers and automated driving systems in real-time for safer driving.

### [MAPEM : Map Extended Message](mapem)

Contains detailed mapping information, typically updated less frequently, such as every few minutes or when significant
changes occur on the road. It provides vehicles with accurate data on topography, traffic lanes, speed limits, and other
essential features for navigation and safety.

### [SPATEM : Signal Phase And Timing Extended Message](spatem)

Periodically emitted by traffic lights or their controllers. It indicates the current phases of the traffic lights
(green, red, etc.), the remaining time for each phase, and other signalling information.

### [SREM : Signal Request Extended Message](srem)

Allows a vehicle to request prioritisation from a traffic light or its controller.

### [SSEM : Signal Status Extended Message](ssem)

Sent by the traffic light or its controller to inform about the preemption or priority state of the signal controller
with respect to the requests it has received.

Custom messages
------------

### [Bootstrap](bootstrap)

Device management elements, provided at the starting step to connect to the instance.

### [Information](information)

Information about a backend service instance, such as its identifier, its type, or its running status.

### [Neighbourhood](neighbourhood)

List of neighbour service instances, with information such as the area they cover and the protocols they can
be reached with.

### [Region](region)

Region of the service instance, to define the responsibility area of the component.

### [Status](status)

Status of a device, with information such as its operating system, hardware performance, connectivity and GNSS status,
etc.

Versioning
------------
Any change to a message, even minor, means that a new version should be released.

Commits for new versions should be organised as follows:

- `schema/<message_name>: kick-off update to <message_version>` (previous copied version with `<new_version>-dev`
  in the `version` field as the only change)
- `schema/<message_name>: modification 1`
- `...`
- `schema/<message_name>: modification N`
- `schema/<message_name>: close off update to <message_version>` (`-dev` removed from the `version` field)

With potential additional details in each commit log.