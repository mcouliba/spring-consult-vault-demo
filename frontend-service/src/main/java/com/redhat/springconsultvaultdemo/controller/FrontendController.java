package com.redhat.springconsultvaultdemo.controller;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import javax.naming.ServiceUnavailableException;

import com.redhat.springconsultvaultdemo.properties.FrontendProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
public class FrontendController {
    @Autowired
    private DiscoveryClient discoveryClient;
    private final FrontendProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
    public FrontendController(final FrontendProperties properties) {
        this.properties = properties;
    }

    private Optional<URI> serviceUrl(final String servicename) {
        Objects.requireNonNull(servicename, "Endpoint for '" + servicename + "' was not set in the properties");
        

        return discoveryClient.getInstances(servicename)
          .stream()
          .map(si -> {
              return si.getUri();
          })
          .findFirst();
    }

    @ResponseBody
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/api/greeting")
    public String greeting(@RequestParam final String servicename) throws RestClientException, ServiceUnavailableException{
        URI service = serviceUrl(servicename)
            .map(s -> s.resolve("/api/greeting"))
            .orElseThrow(ServiceUnavailableException::new);

        return restTemplate.getForEntity(service, String.class)
            .getBody();
    }
}