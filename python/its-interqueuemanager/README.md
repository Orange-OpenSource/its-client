its-interqueuemanager
=====================

The _its-interqueuemanager_ application relays ITS messages sent on the
local incoming queue, to the local output queue and inter-queue (for its
neighbours to listen on), and the ITS messages sent on the inter-queues
of its neighbours, to the local output queue.

Configuration
-------------

See the `its-iqm.cfg` file for an example configuration file.

The `neighbours.cfg` and `neighbours.jon` are examples of the
configuration of the neighbours of the IQM; the first if for local or
HTTP authority, the second for an MQTT authority.
