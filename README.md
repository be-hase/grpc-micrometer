[![Maven Central](https://img.shields.io/maven-central/v/com.be-hase.grpc-micrometer/grpc-micrometer.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.be-hase.grpc-micrometer%22%20AND%20a:%22grpc-micrometer%22)
[![CircleCI](https://circleci.com/gh/be-hase/grpc-micrometer.svg?style=svg)](https://circleci.com/gh/be-hase/grpc-micrometer)

# grpc-micrometer

Micrometer instrument for gRPC.

## Metrics

- Server
  - Timer of `grpc.server.requests`: Response latency(seconds) of gRPC
  - Counter of `grpc.server.stream.messages.received`: Total number of stream messages received
  - Counter of `grpc.server.stream.messages.sent`: Total number of stream messages sent
- Client
  - Timer of `grpc.client.requests`: Response latency(seconds) of gRPC
  - Counter of `grpc.client.stream.messages.received`: Total number of stream messages received
  - Counter of `grpc.client.stream.messages.sent`: Total number of stream messages sent
    
## Usage

Add dependency in your app.

- groupId: com.be-hase.grpc-micrometer
- artifactId: grpc-micrometer
- version: [![Maven Central](https://img.shields.io/maven-central/v/com.be-hase.grpc-micrometer/grpc-micrometer.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.be-hase.grpc-micrometer%22%20AND%20a:%22grpc-micrometer%22)

### Server

```java
final Server server = ServerBuilder.forPort(6565)
                                   .addService(new HelloService())
                                   .intercept(new MicrometerServerInterceptor(Metrics.globalRegistry))
                                   .build();
```

### Client

```java
return HelloServiceGrpc.newBlockingStub(channel)
                       .withInterceptors(new MicrometerClientInterceptor(Metrics.globalRegistry));

```

### Customize

You can customize like this.

```java
final GrpcMetricsConfigure configure =
        GrpcMetricsConfigure.create()
                            .withLatencyTimerConfigure(builder -> {
                                builder.publishPercentiles(0.5, 0.75, 0.95, 0.99);
                            });

return new MicrometerServerInterceptor(meterRegistry, configure);

```
