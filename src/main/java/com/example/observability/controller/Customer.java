package com.example.observability.controller;

import lombok.Builder;
import lombok.ToString;

@Builder
public class Customer {
    private String customerId;
    private String dob;

    @Override
    public String toString() {
        return "{ \"customerId\": \""+customerId+"\", \"dob\":\""+dob+"\"}";
    }
}
