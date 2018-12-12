package com.be_hase.grpc.micrometer;

import java.util.concurrent.TimeUnit;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;

class MicrometerServerCall<ReqT, RespT>
        extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {
    private final GrpcMetrics grpcMetrics;
    private final GrpcMethod grpcMethod;
    private final long start;

    MicrometerServerCall(ServerCall<ReqT, RespT> delegate,
                         GrpcMetrics grpcMetrics,
                         GrpcMethod grpcMethod) {
        super(delegate);

        this.grpcMetrics = grpcMetrics;
        this.grpcMethod = grpcMethod;
        start = System.nanoTime();
    }

    @Override
    public void close(Status status, Metadata trailers) {
        super.close(status, trailers);
        grpcMetrics.recordLatency(status, System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }

    @Override
    public void sendMessage(RespT message) {
        super.sendMessage(message);
        if (grpcMethod.isStreamsResponses()) {
            grpcMetrics.incrementStreamMessagesSent();
        }
    }
}
