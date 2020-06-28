package com.example.observability.controller;

import com.example.observability.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HomeController {

    @Autowired
    private GreetingService greetingService;

    @GetMapping("/greeting")
    public String getGreeting(){
        return greetingService.getDefaultGreeting();
    }

    @GetMapping("/greeting/{name}")
    public String getGreeting(@PathVariable  String name){
        return greetingService.getGreeting(name);
    }
}
