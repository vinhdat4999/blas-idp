package com.blas.blasidp.controller;

import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.jwt.payload.JwtRequest;
import com.blas.blascommon.jwt.payload.JwtResponse;
import com.blas.blasidp.jwt.JwtTokenUtil;
import com.blas.blasidp.jwt.JwtUserDetailsService;
import com.blas.blasidp.service.AuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @PostMapping(value = "/auth")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(
                authenticationRequest.getUsername());
        AuthUser authUser = authUserService.getAuthUserByUsername("admin");
        return ResponseEntity.ok(new JwtResponse(jwtTokenUtil.generateToken(userDetails)));
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException exception) {
            throw new Exception("USER_DISABLED", exception);
        } catch (BadCredentialsException exception) {
            throw new Exception("INVALID_CREDENTIALS", exception);
        }
    }
}