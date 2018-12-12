package com.be_hase.grpc.micrometer.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.be_hase.grpc.micrometer.example.hello.HelloRequest;
import com.be_hase.grpc.micrometer.example.hello.HelloServiceGrpc.HelloServiceBlockingStub;

@RestController
public class TestController {
    private final HelloServiceBlockingStub helloServiceBlockingStub;

    public TestController(HelloServiceBlockingStub helloServiceBlockingStub) {
        this.helloServiceBlockingStub = helloServiceBlockingStub;
    }

    @GetMapping("/test/sayHello")
    public String sayHelloByHttp(@RequestParam String name) {
        return helloServiceBlockingStub.sayHello(HelloRequest.newBuilder()
                                                             .setName(name)
                                                             .build())
                                       .getMessage();
    }
}
