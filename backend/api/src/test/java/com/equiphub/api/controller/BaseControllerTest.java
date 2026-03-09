package com.equiphub.api.controller;

import com.equiphub.api.config.JwtConfig;
import com.equiphub.api.security.jwt.JwtUtils;
import com.equiphub.api.security.CustomUserDetailsService;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Base class for all @WebMvcTest controller tests.
 *
 * Declares the three beans that JwtAuthenticationFilter requires as
 * constructor parameters. Every child test class inherits these mocks
 * automatically — do NOT re-declare them in child classes.
 *
 *   JwtAuthenticationFilter(JwtUtils, CustomUserDetailsService, JwtConfig)
 *                              ↑               ↑                   ↑
 *                         param 0           param 1             param 2  ← was missing
 */
public abstract class BaseControllerTest {

    @MockBean
    protected JwtUtils jwtUtils;

    @MockBean
    protected CustomUserDetailsService customUserDetailsService;

    @MockBean
    protected JwtConfig jwtConfig;
}
