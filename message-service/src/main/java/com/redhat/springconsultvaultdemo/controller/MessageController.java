package com.redhat.springconsultvaultdemo.controller;

import java.util.Objects;

import com.redhat.springconsultvaultdemo.properties.MessageProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller

public class MessageController {

    private MessageProperties properties;

    @Autowired
    public MessageController(MessageProperties properties) {
        this.properties = properties;
    }

    @ResponseBody
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/api/greeting")
    public String greeting() {
        Objects.requireNonNull(properties.getGreeting(), "Greeting message was not set in the properties");

        return String.format(properties.getGreeting());
    }
}