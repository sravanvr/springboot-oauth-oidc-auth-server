package com.example.resourceserver.greeting;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;

public class CustomBearerTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver defaultBearerTokenResolver = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest httpServletRequest) {
        String resolveToken = defaultBearerTokenResolver.resolve(httpServletRequest);
        System.out.println("Resolved token: " + resolveToken);
        if(resolveToken == null){
            throw new RuntimeException("missing jwt token");
        }
        return resolveToken;
    }
}
