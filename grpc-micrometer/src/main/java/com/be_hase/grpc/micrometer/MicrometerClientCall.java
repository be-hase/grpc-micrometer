package com.be_hase.grpc.micrometer;

import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;

class MicrometerClientCall<ReqT, RespT>
        extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {
    private final GrpcMetrics grpcMetrics;
    private final GrpcMethod grpcMethod;

    MicrometerClientCall(ClientCall<ReqT, RespT> delegate,
                         GrpcMetrics grpcMetrics,
                         GrpcMethod grpcMethod) {
        super(delegate);

        this.grpcMetrics = grpcMetrics;
        this.grpcMethod = grpcMethod;
    }

    @Override
    public void start(Listener<RespT> responseListener, Metadata headers) {
        super.start(new MicrometerClientCallListener<>(responseListener, grpcMetrics, grpcMethod), headers);
    }

    @Override
    public void sendMessage(ReqT message) {
        super.sendMessage(message);
        if (grpcMethod.isStreamsRequests()) {
            grpcMetrics.incrementStreamMessagesSent();
        }
    }
}
