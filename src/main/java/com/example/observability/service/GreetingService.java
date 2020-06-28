package com.example.observability.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public String getDefaultGreeting(){
        return "Hello Default";
    }

    public String getGreeting(String name){
        return "Hello "+name;
    }
}
