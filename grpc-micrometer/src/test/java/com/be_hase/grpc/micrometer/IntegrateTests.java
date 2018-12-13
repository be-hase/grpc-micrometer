package com.be_hase.grpc.micrometer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class IntegrateTests {
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private HelloServiceGrpc.HelloServiceBlockingStub blockingStub;
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
    }

    @Test
    public void unary() {

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
                            Status.INVALID_ARGUMENT.withDescription("invalid argument")
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
                                      .setMessage("Hello " + requests.stream().collect(Collectors.joining()))
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
                            Status.INVALID_ARGUMENT.withDescription("invalid argument")
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
