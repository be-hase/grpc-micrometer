package com.be_hase.grpc.micrometer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import io.grpc.MethodDescriptor.MethodType;
import io.grpc.Status;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class GrpcMetricsTest {
    private GrpcMetrics target;

    private MeterRegistry meterRegistry;
    private GrpcMethod grpcMethod;

    @Before
    public void before() {
        meterRegistry = new SimpleMeterRegistry();
        grpcMethod = mock(GrpcMethod.class);
        doReturn("serviceName").when(grpcMethod).getServiceName();
        doReturn("methodName").when(grpcMethod).getMethodName();
        doReturn(MethodType.UNARY).when(grpcMethod).getMethodType();

        target = new GrpcMetrics(meterRegistry, "test", GrpcMetricsConfigure.create(), grpcMethod);
    }

    @Test
    public void recordLatency() {
        // when
        target.recordLatency(Status.OK, 100, TimeUnit.MILLISECONDS);
        target.recordLatency(Status.OK, 200, TimeUnit.MILLISECONDS);

        // then
        final Timer timer = meterRegistry.timer("test.requests", Arrays.asList(
                Tag.of("service", "serviceName"),
                Tag.of("method", "methodName"),
                Tag.of("methodType", "UNARY"),
                Tag.of("statusCode", "OK")
        ));
        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.max(TimeUnit.SECONDS)).isEqualTo(0.2);
        assertThat(timer.mean(TimeUnit.SECONDS)).isEqualTo(0.15);
    }

    @Test
    public void incrementStreamMessagesReceived() {
        // when
        target.incrementStreamMessagesReceived();
        target.incrementStreamMessagesReceived();

        // then
        final Counter counter = meterRegistry.counter("test.stream.messages.received", Arrays.asList(
                Tag.of("service", "serviceName"),
                Tag.of("method", "methodName"),
                Tag.of("methodType", "UNARY")
        ));
        assertThat(counter.count()).isEqualTo(2);
    }

    @Test
    public void incrementStreamMessagesSent() {
        // when
        target.incrementStreamMessagesSent();
        target.incrementStreamMessagesSent();

        // then
        final Counter counter = meterRegistry.counter("test.stream.messages.sent", Arrays.asList(
                Tag.of("service", "serviceName"),
                Tag.of("method", "methodName"),
                Tag.of("methodType", "UNARY")
        ));
        assertThat(counter.count()).isEqualTo(2);
    }
}
