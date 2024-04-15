package com.ss871104.oauth2security.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private String customServletPath;

    public CustomHttpServletRequestWrapper(HttpServletRequest request, String customServletPath) {
        super(request);
        this.customServletPath = customServletPath;
    }

    @Override
    public String getServletPath() {
        return this.customServletPath;
    }

}
