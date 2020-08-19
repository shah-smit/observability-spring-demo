package com.example.observability.controller;

import com.example.observability.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class HomeController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HomeController.class);
    @Autowired
    private GreetingService greetingService;

    @GetMapping("/greeting")
    public String getGreeting(){
        log.info("AUDIT_LOG >> customer id {}", UUID.randomUUID().toString().substring(0, 5));
        Customer customer = Customer.builder().customerId("S9472394C00").dob("23022010").build();
        log.info("GET Greeting { \"customerId\": \"S8672394C00\", \"dob\":\"12022010\"}");
        return greetingService.getDefaultGreeting();
    }

    @GetMapping("/greeting/{cid}/{dob}")
    public String getGreetingByparams(@PathVariable String cid, @PathVariable String dob){
        log.info("AUDIT_LOG >> customer id {}", UUID.randomUUID().toString().substring(0, 5));
        Customer customer = Customer.builder().customerId(cid).dob(dob).build();
        log.info("GET Greeting {}", customer);
        return greetingService.getDefaultGreeting();
    }

    @GetMapping("/greeting/{name}")
    public String getGreeting(@PathVariable  String name){
        log.info("AUDIT_LOG >> customer id {}", UUID.randomUUID().toString().substring(0, 5));
        return greetingService.getGreeting(name);
    }

    @GetMapping("/generate")
    public String generate(){
        log.info("AUDIT_LOG >> customer id {}", UUID.randomUUID().toString().substring(0, 5));
        Customer customer = Customer.builder().customerId(randomAlphaNumeric(11)).dob(randomAlphaNumeric(8)).build();
        log.info("GET Greeting {}", customer);
        return greetingService.getDefaultGreeting();
    }


    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
