package com.blas.blasidp.controller;

import static com.blas.blasidp.constant.Authentication.ACCOUNT_BLOCKED;
import static com.blas.blasidp.constant.Authentication.ACCOUNT_INACTIVE;
import static com.blas.blasidp.constant.Authentication.THRESHOLD_BLOCK_ACCOUNT;
import static com.blas.blasidp.constant.Authentication.WRONG_CREDENTIAL;

import com.blas.blascommon.core.dao.AuthUserDao;
import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.exceptions.types.ForbiddenException;
import com.blas.blascommon.exceptions.types.UnauthorizedException;
import com.blas.blascommon.jwt.JwtTokenUtil;
import com.blas.blascommon.jwt.JwtUserDetailsService;
import com.blas.blascommon.jwt.payload.JwtRequest;
import com.blas.blascommon.jwt.payload.JwtResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthUserDao authUserDao;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @PostMapping(value = "/auth/login")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(
                authenticationRequest.getUsername());
        return ResponseEntity.ok(new JwtResponse(jwtTokenUtil.generateToken(userDetails)));
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            AuthUser authUser = authUserDao.getAuthUserByUsername(username);
            if (authUser.getCountLoginFailed() > 0) {
                authUser.setCountLoginFailed(0);
                authUserDao.save(authUser);
            }
        } catch (DisabledException exception) {
            throw new UnauthorizedException(ACCOUNT_INACTIVE);
        } catch (LockedException exception) {
            throw new ForbiddenException(ACCOUNT_BLOCKED);
        } catch (BadCredentialsException exception) {
            AuthUser authUser = authUserDao.getAuthUserByUsername(username);
            if (authUser != null && authUser.getCountLoginFailed() < THRESHOLD_BLOCK_ACCOUNT) {
                authUser.setCountLoginFailed(authUser.getCountLoginFailed() + 1);
                if (authUser.getCountLoginFailed() == THRESHOLD_BLOCK_ACCOUNT) {
                    authUser.setBlock(true);
                }
                authUserDao.save(authUser);
            }
            throw new UnauthorizedException(WRONG_CREDENTIAL);
        }
    }
}