package com.be_hase.grpc.micrometer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.be_hase.grpc.micrometer.example.hello.HelloReply;
import com.be_hase.grpc.micrometer.example.hello.HelloRequest;
import com.be_hase.grpc.micrometer.example.hello.HelloServiceGrpc;

import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcServerRule;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class IntegrateTests {
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private HelloServiceGrpc.HelloServiceBlockingStub blockingStub;
    private HelloServiceGrpc.HelloServiceStub stub;
    @Rule
    public GrpcServerRule grpcServerRule = new GrpcServerRule().directExecutor();

    @Before
    public void before() {
        meterRegistry = new SimpleMeterRegistry();

        // server
        grpcServerRule.getServiceRegistry()
                      .addService(ServerInterceptors.intercept(
                              new HelloService(),
                              new MicrometerServerInterceptor(meterRegistry)));

        // client
        blockingStub =
                HelloServiceGrpc.newBlockingStub(grpcServerRule.getChannel())
                                .withInterceptors(new MicrometerClientInterceptor(meterRegistry));
        stub = HelloServiceGrpc.newStub(grpcServerRule.getChannel())
                               .withInterceptors(new MicrometerClientInterceptor(meterRegistry));
    }

    @Test
    public void unary() {
        // When
        blockingStub.sayHello(HelloRequest.newBuilder().setName("name").build());
        try {
            blockingStub.sayHello(HelloRequest.newBuilder().setName("error").build());
        } catch (Exception ignored) {
        }

        // Then
        // Server
        final Timer serverOKTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHello"),
                Tag.of("methodType", "UNARY"),
                Tag.of("statusCode", "OK")
        ));
        assertThat(serverOKTimer.count()).isEqualTo(1);
        final Timer serverInvalidTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHello"),
                Tag.of("methodType", "UNARY"),
                Tag.of("statusCode", "INVALID_ARGUMENT")
        ));
        assertThat(serverInvalidTimer.count()).isEqualTo(1);

        // Client
        final Timer clientOKTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHello"),
                Tag.of("methodType", "UNARY"),
                Tag.of("statusCode", "OK")
        ));
        assertThat(clientOKTimer.count()).isEqualTo(1);
        final Timer clientInvalidTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHello"),
                Tag.of("methodType", "UNARY"),
                Tag.of("statusCode", "INVALID_ARGUMENT")
        ));
        assertThat(clientInvalidTimer.count()).isEqualTo(1);
    }

    @Test
    public void serverStream() {
        // When
        blockingStub.sayHelloServerStream(HelloRequest.newBuilder().setName("name").build())
                    .forEachRemaining(it -> {
                    });
        try {
            blockingStub.sayHelloServerStream(HelloRequest.newBuilder().setName("error").build())
                        .forEachRemaining(it -> {
                        });
        } catch (Exception ignored) {
        }

        // Then
        // Server
        final Timer serverOKTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloServerStream"),
                Tag.of("methodType", "SERVER_STREAMING"),
                Tag.of("statusCode", "OK")
        ));
        assertThat(serverOKTimer.count()).isEqualTo(1);
        final Timer serverInvalidTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloServerStream"),
                Tag.of("methodType", "SERVER_STREAMING"),
                Tag.of("statusCode", "INVALID_ARGUMENT")
        ));
        assertThat(serverInvalidTimer.count()).isEqualTo(1);
        final Counter serverSentCounter =
                meterRegistry.counter("grpc.server.stream.messages.sent", Arrays.asList(
                        Tag.of("service", "hello.HelloService"),
                        Tag.of("method", "SayHelloServerStream"),
                        Tag.of("methodType", "SERVER_STREAMING")
                ));
        assertThat(serverSentCounter.count()).isEqualTo(3);

        // Client
        final Timer clientOKTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloServerStream"),
                Tag.of("methodType", "SERVER_STREAMING"),
                Tag.of("statusCode", "OK")
        ));
        assertThat(clientOKTimer.count()).isEqualTo(1);
        final Timer clientInvalidTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloServerStream"),
                Tag.of("methodType", "SERVER_STREAMING"),
                Tag.of("statusCode", "INVALID_ARGUMENT")
        ));
        assertThat(clientInvalidTimer.count()).isEqualTo(1);
        final Counter clientReceivedCounter =
                meterRegistry.counter("grpc.client.stream.messages.received", Arrays.asList(
                        Tag.of("service", "hello.HelloService"),
                        Tag.of("method", "SayHelloServerStream"),
                        Tag.of("methodType", "SERVER_STREAMING")
                ));
        assertThat(clientReceivedCounter.count()).isEqualTo(3);
    }

    @Test
    public void clientStream() {
        // When
        final StreamObserver<HelloReply> responseoObserver = new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply value) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };

        final StreamObserver<HelloRequest> request = stub.sayHelloClientStream(responseoObserver);
        request.onNext(HelloRequest.newBuilder().setName("name").build());
        request.onNext(HelloRequest.newBuilder().setName("name").build());
        request.onCompleted();

        final StreamObserver<HelloRequest> emptyRequest = stub.sayHelloClientStream(responseoObserver);
        emptyRequest.onCompleted();

        final StreamObserver<HelloRequest> errorRequest = stub.sayHelloClientStream(responseoObserver);
        errorRequest.onError(new RuntimeException("error"));

        // Then
        // Server
        final Timer serverOKTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloClientStream"),
                Tag.of("methodType", "CLIENT_STREAMING"),
                Tag.of("statusCode", "OK")
        ));
        assertThat(serverOKTimer.count()).isEqualTo(1);
        final Timer serverInvalidTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloClientStream"),
                Tag.of("methodType", "CLIENT_STREAMING"),
                Tag.of("statusCode", "INVALID_ARGUMENT")
        ));
        assertThat(serverInvalidTimer.count()).isEqualTo(1);
        final Timer serverCanceledTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloClientStream"),
                Tag.of("methodType", "CLIENT_STREAMING"),
                Tag.of("statusCode", "CANCELLED")
        ));
        assertThat(serverCanceledTimer.count()).isEqualTo(1);
        final Counter serverReceivedCounter =
                meterRegistry.counter("grpc.server.stream.messages.received", Arrays.asList(
                        Tag.of("service", "hello.HelloService"),
                        Tag.of("method", "SayHelloClientStream"),
                        Tag.of("methodType", "CLIENT_STREAMING")
                ));
        assertThat(serverReceivedCounter.count()).isEqualTo(2);

        // Client
        final Timer clientOKTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloClientStream"),
                Tag.of("methodType", "CLIENT_STREAMING"),
                Tag.of("statusCode", "OK")
        ));
        assertThat(clientOKTimer.count()).isEqualTo(1);
        final Timer clientInvalidTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloClientStream"),
                Tag.of("methodType", "CLIENT_STREAMING"),
                Tag.of("statusCode", "INVALID_ARGUMENT")
        ));
        assertThat(clientInvalidTimer.count()).isEqualTo(1);
        final Timer clientCanceledTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloClientStream"),
                Tag.of("methodType", "CLIENT_STREAMING"),
                Tag.of("statusCode", "CANCELLED")
        ));
        assertThat(clientCanceledTimer.count()).isEqualTo(1);
        final Counter clientSentCounter =
                meterRegistry.counter("grpc.client.stream.messages.sent", Arrays.asList(
                        Tag.of("service", "hello.HelloService"),
                        Tag.of("method", "SayHelloClientStream"),
                        Tag.of("methodType", "CLIENT_STREAMING")
                ));
        assertThat(clientSentCounter.count()).isEqualTo(2);
    }

    @Test
    public void bidiStream() {
        final StreamObserver<HelloReply> responseoObserver = new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply value) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };

        final StreamObserver<HelloRequest> request = stub.sayHelloBidiStream(responseoObserver);
        request.onNext(HelloRequest.newBuilder().setName("name").build());
        request.onNext(HelloRequest.newBuilder().setName("name").build());
        request.onCompleted();

        final StreamObserver<HelloRequest> invalidRequest = stub.sayHelloBidiStream(responseoObserver);
        invalidRequest.onNext(HelloRequest.newBuilder().setName("error").build());

        final StreamObserver<HelloRequest> errorRequest = stub.sayHelloBidiStream(responseoObserver);
        errorRequest.onError(new RuntimeException("error"));

        meterRegistry.getMeters().forEach(it -> System.out.println(it.getId()));

        // Then
        // Server
        final Timer serverOKTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloBidiStream"),
                Tag.of("methodType", "BIDI_STREAMING"),
                Tag.of("statusCode", "OK")
        ));
        assertThat(serverOKTimer.count()).isEqualTo(1);
        final Timer serverInvalidTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloBidiStream"),
                Tag.of("methodType", "BIDI_STREAMING"),
                Tag.of("statusCode", "INVALID_ARGUMENT")
        ));
        assertThat(serverInvalidTimer.count()).isEqualTo(1);
        final Timer serverCanceledTimer = meterRegistry.timer("grpc.server.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloBidiStream"),
                Tag.of("methodType", "BIDI_STREAMING"),
                Tag.of("statusCode", "CANCELLED")
        ));
        assertThat(serverCanceledTimer.count()).isEqualTo(1);
        final Counter serverSentCounter =
                meterRegistry.counter("grpc.server.stream.messages.sent", Arrays.asList(
                        Tag.of("service", "hello.HelloService"),
                        Tag.of("method", "SayHelloBidiStream"),
                        Tag.of("methodType", "BIDI_STREAMING")
                ));
        assertThat(serverSentCounter.count()).isEqualTo(4);
        final Counter serverReceivedCounter =
                meterRegistry.counter("grpc.server.stream.messages.received", Arrays.asList(
                        Tag.of("service", "hello.HelloService"),
                        Tag.of("method", "SayHelloBidiStream"),
                        Tag.of("methodType", "BIDI_STREAMING")
                ));
        assertThat(serverReceivedCounter.count()).isEqualTo(3);

        // Client
        final Timer clientOKTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloBidiStream"),
                Tag.of("methodType", "BIDI_STREAMING"),
                Tag.of("statusCode", "OK")
        ));
        assertThat(clientOKTimer.count()).isEqualTo(1);
        final Timer clientInvalidTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloBidiStream"),
                Tag.of("methodType", "BIDI_STREAMING"),
                Tag.of("statusCode", "INVALID_ARGUMENT")
        ));
        assertThat(clientInvalidTimer.count()).isEqualTo(1);
        final Timer clientCanceledTimer = meterRegistry.timer("grpc.client.requests", Arrays.asList(
                Tag.of("service", "hello.HelloService"),
                Tag.of("method", "SayHelloBidiStream"),
                Tag.of("methodType", "BIDI_STREAMING"),
                Tag.of("statusCode", "CANCELLED")
        ));
        assertThat(clientCanceledTimer.count()).isEqualTo(1);
        final Counter clientSentCounter =
                meterRegistry.counter("grpc.client.stream.messages.sent", Arrays.asList(
                        Tag.of("service", "hello.HelloService"),
                        Tag.of("method", "SayHelloBidiStream"),
                        Tag.of("methodType", "BIDI_STREAMING")
                ));
        assertThat(clientSentCounter.count()).isEqualTo(3);
        final Counter clientReceivedCounter =
                meterRegistry.counter("grpc.client.stream.messages.received", Arrays.asList(
                        Tag.of("service", "hello.HelloService"),
                        Tag.of("method", "SayHelloBidiStream"),
                        Tag.of("methodType", "BIDI_STREAMING")
                ));
        assertThat(clientReceivedCounter.count()).isEqualTo(4);
    }

    public static class HelloService extends HelloServiceGrpc.HelloServiceImplBase {
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            final String name = request.getName();
            if ("error".equals(name)) {
                final StatusException exception =
                        Status.INVALID_ARGUMENT.withDescription("invalid argument")
                                               .asException();
                responseObserver.onError(exception);
                return;
            }

            final HelloReply reply = HelloReply.newBuilder()
                                               .setMessage("Hello " + name)
                                               .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void sayHelloServerStream(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            final String name = request.getName();
            if ("error".equals(name)) {
                final StatusException exception =
                        Status.INVALID_ARGUMENT.withDescription("invalid argument")
                                               .asException();
                responseObserver.onError(exception);
                return;
            }

            final HelloReply reply = HelloReply.newBuilder()
                                               .setMessage("Hello " + name)
                                               .build();
            responseObserver.onNext(reply);
            responseObserver.onNext(reply);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<HelloRequest> sayHelloClientStream(StreamObserver<HelloReply> responseObserver) {
            final List<String> requests = new ArrayList<>();

            return new StreamObserver<HelloRequest>() {
                @Override
                public void onNext(HelloRequest request) {
                    requests.add(request.getName());
                }

                @Override
                public void onError(Throwable t) {
                    final StatusException exception =
                            Status.CANCELLED.withDescription(t.getMessage())
                                            .asException();
                    responseObserver.onError(exception);
                }

                @Override
                public void onCompleted() {
                    if (requests.isEmpty()) {
                        final StatusException exception =
                                Status.INVALID_ARGUMENT.withDescription("invalid argument")
                                                       .asException();
                        responseObserver.onError(exception);
                        return;
                    }

                    final HelloReply reply =
                            HelloReply.newBuilder()
                                      .setMessage("Hello " + String.join("", requests))
                                      .build();
                    responseObserver.onNext(reply);
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public StreamObserver<HelloRequest> sayHelloBidiStream(StreamObserver<HelloReply> responseObserver) {
            return new StreamObserver<HelloRequest>() {
                @Override
                public void onNext(HelloRequest request) {
                    final String name = request.getName();
                    if ("error".equals(name)) {
                        final StatusException exception =
                                Status.INVALID_ARGUMENT.withDescription("invalid argument")
                                                       .asException();
                        responseObserver.onError(exception);
                        return;
                    }

                    final HelloReply reply = HelloReply.newBuilder()
                                                       .setMessage("Hello " + name)
                                                       .build();
                    responseObserver.onNext(reply);
                    responseObserver.onNext(reply);
                }

                @Override
                public void onError(Throwable t) {
                    final StatusException exception =
                            Status.CANCELLED.withDescription(t.getMessage())
                                            .asException();
                    responseObserver.onError(exception);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
