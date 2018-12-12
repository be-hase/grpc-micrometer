package com.be_hase.grpc.micrometer;

import java.util.concurrent.TimeUnit;

import io.grpc.Status;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

class GrpcMetrics {

    private final MeterRegistry meterRegistry;
    private final String metricsName;
    private final GrpcMetricsConfigure metricsConfigure;
    private final GrpcMethod grpcMethod;

    GrpcMetrics(MeterRegistry meterRegistry,
                String metricsName,
                GrpcMetricsConfigure metricsConfigure,
                GrpcMethod grpcMethod) {
        this.meterRegistry = meterRegistry;
        this.metricsName = metricsName;
        this.metricsConfigure = metricsConfigure;
        this.grpcMethod = grpcMethod;
    }

    void recordLatency(Status status, long amount, TimeUnit unit) {
        latencyTimer(status).record(amount, unit);
    }

    void incrementStreamMessagesReceived() {
        streamMessagesReceivedCounter().increment();
    }

    void incrementStreamMessagesSent() {
        streamMessagesSentCounter().increment();
    }

    private Timer latencyTimer(Status status) {
        final Timer.Builder latencyTimerBuilder =
                Timer.builder(metricsName + ".requests")
                     .description("Response latency(seconds) of gRPC.")
                     .tags(GrpcMethodTagProvider.tags(grpcMethod))
                     .tag("statusCode", status.getCode().name());
        metricsConfigure.latencyTimerConfigure.accept(latencyTimerBuilder);
        return latencyTimerBuilder.register(meterRegistry);
    }

    private Counter streamMessagesReceivedCounter() {
        final Counter.Builder streamMessagesReceivedCounterBuilder =
                Counter.builder(metricsName + ".stream.messages.received")
                       .description("Total number of stream messages received.")
                       .tags(GrpcMethodTagProvider.tags(grpcMethod));
        metricsConfigure.streamMessagesReceivedCounterConfigure.accept(streamMessagesReceivedCounterBuilder);
        return streamMessagesReceivedCounterBuilder.register(meterRegistry);
    }

    private Counter streamMessagesSentCounter() {
        final Counter.Builder streamMessagesSentCounterBuilder =
                Counter.builder(metricsName + ".stream.messages.sent")
                       .description("Total number of stream messages sent.")
                       .tags(GrpcMethodTagProvider.tags(grpcMethod));
        metricsConfigure.streamMessagesSentCounterConfigure.accept(streamMessagesSentCounterBuilder);
        return streamMessagesSentCounterBuilder.register(meterRegistry);
    }
}
