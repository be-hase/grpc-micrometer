package com.be_hase.grpc.micrometer;

import java.util.Objects;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.MeterRegistry;

public class MicrometerServerInterceptor implements ServerInterceptor {
    private static final String DEFAULT_METRICS_NAME = "grpc.server";

    private final MeterRegistry meterRegistry;
    private final String metricsName;
    private final GrpcMetricsConfigure metricsConfigure;

    /**
     * @param meterRegistry
     */
    public MicrometerServerInterceptor(MeterRegistry meterRegistry) {
        this(meterRegistry, DEFAULT_METRICS_NAME);
    }

    /**
     * @param meterRegistry
     * @param metricsName
     */
    public MicrometerServerInterceptor(MeterRegistry meterRegistry, String metricsName) {
        this(meterRegistry, metricsName, GrpcMetricsConfigure.create());
    }

    /**
     * @param meterRegistry
     * @param metricsConfigure
     */
    public MicrometerServerInterceptor(MeterRegistry meterRegistry, GrpcMetricsConfigure metricsConfigure) {
        this(meterRegistry, DEFAULT_METRICS_NAME, metricsConfigure);
    }

    /**
     * @param meterRegistry
     * @param metricsName
     * @param metricsConfigure
     */
    public MicrometerServerInterceptor(MeterRegistry meterRegistry,
                                       String metricsName,
                                       GrpcMetricsConfigure metricsConfigure) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry");
        this.metricsName = Objects.requireNonNull(metricsName, "metricsName");
        this.metricsConfigure = Objects.requireNonNull(metricsConfigure, "metricsConfigure");
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                      ServerCallHandler<ReqT, RespT> next) {
        final GrpcMethod grpcMethod = GrpcMethod.of(call.getMethodDescriptor());
        final GrpcMetrics grpcMetrics =
                new GrpcMetrics(meterRegistry, metricsName, metricsConfigure, grpcMethod);
        final MicrometerServerCall<ReqT, RespT> micrometerCall =
                new MicrometerServerCall<>(call, grpcMetrics, grpcMethod);
        return new MicrometerServerCallListener<>(
                next.startCall(micrometerCall, headers), grpcMetrics, grpcMethod);
    }
}
