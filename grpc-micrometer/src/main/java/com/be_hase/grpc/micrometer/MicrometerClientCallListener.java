package com.be_hase.grpc.micrometer;

import java.util.concurrent.TimeUnit;

import io.grpc.ClientCall.Listener;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.Status;

class MicrometerClientCallListener<RespT>
        extends ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT> {
    private final GrpcMetrics grpcMetrics;
    private final GrpcMethod grpcMethod;
    private final long start;

    MicrometerClientCallListener(Listener<RespT> delegate,
                                 GrpcMetrics grpcMetrics,
                                 GrpcMethod grpcMethod) {
        super(delegate);

        this.grpcMetrics = grpcMetrics;
        this.grpcMethod = grpcMethod;
        start = System.nanoTime();
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        super.onClose(status, trailers);
        grpcMetrics.recordLatency(status, System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }

    @Override
    public void onMessage(RespT message) {
        super.onMessage(message);
        if (grpcMethod.isStreamsResponses()) {
            grpcMetrics.incrementStreamMessagesReceived();
        }
    }
}
