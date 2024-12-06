package com.survey.application.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope
public class SessionContextImpl implements SessionContext{
    private final HttpServletRequest request;

    public SessionContextImpl(HttpServletRequest request) {
        this.request = request;
    }


    @Override
    public String getClientLang() {
        return request.getHeader("Accept-Lang");
    }
}
