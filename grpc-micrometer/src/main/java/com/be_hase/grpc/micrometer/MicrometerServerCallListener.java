package com.be_hase.grpc.micrometer;

import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall.Listener;

class MicrometerServerCallListener<ReqT>
        extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {
    private final GrpcMethod grpcMethod;
    private final GrpcMetrics grpcMetrics;

    MicrometerServerCallListener(Listener<ReqT> delegate,
                                 GrpcMetrics grpcMetrics,
                                 GrpcMethod grpcMethod) {
        super(delegate);

        this.grpcMetrics = grpcMetrics;
        this.grpcMethod = grpcMethod;
    }

    @Override
    public void onMessage(ReqT message) {
        super.onMessage(message);
        if (grpcMethod.isStreamsRequests()) {
            grpcMetrics.incrementStreamMessagesReceived();
        }
    }
}
