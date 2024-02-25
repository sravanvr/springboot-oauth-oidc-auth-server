package com.example.resourceserver.greeting;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/hello")
@RequiredArgsConstructor
@Validated
public class HelloBoundary {

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(OK)
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello World");
    }


    @PreAuthorize("hasAuthority('SCOPE_READ_PRIVILEGE')")
    @GetMapping("/authtest")
    public String protectedResource() {
        return "Access to protected resource granted";
    }
}
