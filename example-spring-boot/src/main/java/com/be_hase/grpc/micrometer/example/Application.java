package com.be_hase.grpc.micrometer.example;

import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.be_hase.grpc.micrometer.GrpcMetricsConfigure;
import com.be_hase.grpc.micrometer.MicrometerClientInterceptor;
import com.be_hase.grpc.micrometer.MicrometerServerInterceptor;
import com.be_hase.grpc.micrometer.example.hello.HelloServiceGrpc;
import com.be_hase.grpc.micrometer.example.hello.HelloServiceGrpc.HelloServiceBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public HelloServiceBlockingStub helloServiceBlockingStub() {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565)
                                                            .usePlaintext()
                                                            .build();

        final GrpcMetricsConfigure configure =
                GrpcMetricsConfigure.create()
                                    .withLatencyTimerConfigure(builder -> {
                                        builder.publishPercentiles(0.5, 0.75, 0.95, 0.99);
                                    });

        return HelloServiceGrpc
                .newBlockingStub(channel)
                .withInterceptors(new MicrometerClientInterceptor(Metrics.globalRegistry, configure));
    }

    @Bean
    @GRpcGlobalInterceptor
    public MicrometerServerInterceptor micrometerServerInterceptor(MeterRegistry meterRegistry) {
        final GrpcMetricsConfigure configure =
                GrpcMetricsConfigure.create()
                                    .withLatencyTimerConfigure(builder -> {
                                        builder.publishPercentiles(0.5, 0.75, 0.95, 0.99);
                                    });

        return new MicrometerServerInterceptor(meterRegistry, configure);
    }
}
