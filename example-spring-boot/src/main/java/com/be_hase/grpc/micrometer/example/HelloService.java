package com.be_hase.grpc.micrometer.example;

import org.lognet.springboot.grpc.GRpcService;

import com.be_hase.grpc.micrometer.example.hello.HelloReply;
import com.be_hase.grpc.micrometer.example.hello.HelloRequest;
import com.be_hase.grpc.micrometer.example.hello.HelloServiceGrpc;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

@GRpcService
public class HelloService extends HelloServiceGrpc.HelloServiceImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        final String name = request.getName();
        if ("error".equals(name)) {
            final StatusException exception = Status.INVALID_ARGUMENT.withDescription("invalid argument")
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
}
