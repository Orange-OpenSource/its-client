# Changes

- ITSClient SDK:
  - Core: new awesome method
  - Mobility: fixed something that was broken
- Sample application:
  - UI: add a button to do something
  - Service: enhance position retrieval

Close #XXX

Close #YYY

# Test

## How to test

1. Prepare a test environment:
   1. Be sure to have unrestricted access to _test.mosquitto.org_ (IPv4 and IPv6)
   2. In a terminal, prepare a collector implementing
      the OpenTelemetry API, on localhost. If you don't have one,
      you may use an existing one, like:
      ```
      $ docker container run \
          --rm \
          -p 16686:16686 \
          -p 4318:4318 \
          jaegertracing/all-in-one:1.58
      ```
      then open a browser on the Jaegger UI
      (or that of your own collector if you have one):
      ```
      http://localhost:16686/
      ```
2. Run tests
   1. Run unit tests using ITSClient-UnitTests test plan in Xcode.
   2. Run integration tests using ITSClient-UnitTests test plan in Xcode.
   3. Test the sample application running it on a simulator or a physical device.
