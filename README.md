[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.be-hase.grpc-micrometer/grpc-micrometer/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.be-hase.grpc-micrometer/grpc-micrometer)
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
