package com.be_hase.grpc.micrometer;

import java.util.function.Consumer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

public final class GrpcMetricsConfigure {
    private static final Consumer<Timer.Builder> NOOP_TIMER_CONFIGURE = it -> {
    };
    private static final Consumer<Counter.Builder> NOOP_COUNTER_CONFIGURE = it -> {
    };

    final Consumer<Timer.Builder> latencyTimerConfigure;
    final Consumer<Counter.Builder> streamMessagesReceivedCounterConfigure;
    final Consumer<Counter.Builder> streamMessagesSentCounterConfigure;

    private GrpcMetricsConfigure(Consumer<Timer.Builder> latencyTimerConfigure,
                                 Consumer<Counter.Builder> streamMessagesReceivedCounterConfigure,
                                 Consumer<Counter.Builder> streamMessagesSentCounterConfigure) {
        this.latencyTimerConfigure =
                latencyTimerConfigure == null
                ? NOOP_TIMER_CONFIGURE
                : latencyTimerConfigure;
        this.streamMessagesReceivedCounterConfigure =
                streamMessagesReceivedCounterConfigure == null
                ? NOOP_COUNTER_CONFIGURE
                : streamMessagesReceivedCounterConfigure;
        this.streamMessagesSentCounterConfigure =
                streamMessagesSentCounterConfigure == null
                ? NOOP_COUNTER_CONFIGURE
                : streamMessagesSentCounterConfigure;
    }

    public static GrpcMetricsConfigure create() {
        return new GrpcMetricsConfigure(null, null, null);
    }

    /**
     * Set latencyTimerConfigure
     * @param configure
     * @return
     */
    public GrpcMetricsConfigure withLatencyTimerConfigure(Consumer<Timer.Builder> configure) {
        return new GrpcMetricsConfigure(configure,
                                        streamMessagesReceivedCounterConfigure,
                                        streamMessagesSentCounterConfigure);
    }

    /**
     * Set streamMessagesReceivedCounterConfigure
     * @param configure
     * @return
     */
    public GrpcMetricsConfigure withStreamMessagesReceivedCounterConfigure(
            Consumer<Counter.Builder> configure) {
        return new GrpcMetricsConfigure(latencyTimerConfigure,
                                        configure,
                                        streamMessagesSentCounterConfigure);
    }

    /**
     * Set streamMessagesSentCounterConfigure
     * @param configure
     * @return
     */
    public GrpcMetricsConfigure withStreamMessagesSentCounterConfigure(
            Consumer<Counter.Builder> configure) {
        return new GrpcMetricsConfigure(latencyTimerConfigure,
                                        streamMessagesReceivedCounterConfigure,
                                        configure);
    }
}
