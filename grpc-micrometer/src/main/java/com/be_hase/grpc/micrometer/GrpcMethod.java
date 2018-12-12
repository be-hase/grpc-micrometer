package com.be_hase.grpc.micrometer;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;

class GrpcMethod {
    private final String serviceName;
    private final String methodName;
    private final MethodType methodType;

    static GrpcMethod of(MethodDescriptor<?, ?> method) {
        final String serviceName = MethodDescriptor.extractFullServiceName(method.getFullMethodName());
        String methodName = null;
        if (serviceName != null) {
            // Full method names are of the form: "full.serviceName/MethodName". We extract the last part.
            methodName = method.getFullMethodName().substring(serviceName.length() + 1);
        }

        return new GrpcMethod(serviceName, methodName, method.getType());
    }

    private GrpcMethod(String serviceName, String methodName, MethodType methodType) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.methodType = methodType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public boolean isStreamsRequests() {
        return !methodType.clientSendsOneMessage();
    }

    public boolean isStreamsResponses() {
        return !methodType.serverSendsOneMessage();
    }
}
