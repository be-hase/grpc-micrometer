package com.be_hase.grpc.micrometer;

import java.util.Arrays;
import java.util.Objects;

import io.micrometer.core.instrument.Tag;

final class GrpcMethodTagProvider {
    private GrpcMethodTagProvider() {
    }

    static Iterable<Tag> tags(GrpcMethod method) {
        Objects.requireNonNull(method, "method");

        return Arrays.asList(
                Tag.of("service", method.getServiceName()),
                Tag.of("method", method.getMethodName()),
                Tag.of("methodType", method.getMethodType().name())
        );
    }
}
