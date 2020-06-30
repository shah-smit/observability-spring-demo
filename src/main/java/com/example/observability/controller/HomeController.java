package com.example.observability.controller;

import com.example.observability.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
public class HomeController {

    @Autowired
    private GreetingService greetingService;

    @GetMapping("/greeting")
    public String getGreeting(){
        log.info("AUDIT_LOG >> customer id {}", UUID.randomUUID().toString().substring(0, 5));
        return greetingService.getDefaultGreeting();
    }

    @GetMapping("/greeting/{name}")
    public String getGreeting(@PathVariable  String name){
        log.info("AUDIT_LOG >> customer id {}", UUID.randomUUID().toString().substring(0, 5));
        return greetingService.getGreeting(name);
    }
}
