package com.equiphub.api.controller;

import com.equiphub.api.config.JwtConfig;
import com.equiphub.api.security.jwt.JwtUtils;
import com.equiphub.api.security.CustomUserDetailsService;
import org.springframework.boot.test.mock.mockito.MockBean;

public abstract class BaseControllerTest {

    @MockBean
    protected JwtUtils jwtUtils;

    @MockBean
    protected CustomUserDetailsService customUserDetailsService;

    @MockBean
    protected JwtConfig jwtConfig;
}
