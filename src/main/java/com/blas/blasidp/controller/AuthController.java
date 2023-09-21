package com.blas.blasidp.controller;

import static com.blas.blasidp.constant.Authentication.ACCOUNT_BLOCKED;
import static com.blas.blasidp.constant.Authentication.ACCOUNT_INACTIVE;
import static com.blas.blasidp.constant.Authentication.THRESHOLD_BLOCK_ACCOUNT;
import static com.blas.blasidp.constant.Authentication.WRONG_CREDENTIAL;

import com.blas.blascommon.core.dao.jpa.AuthUserDao;
import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.exceptions.types.ForbiddenException;
import com.blas.blascommon.exceptions.types.UnauthorizedException;
import com.blas.blascommon.jwt.JwtTokenUtil;
import com.blas.blascommon.jwt.JwtUserDetailsService;
import com.blas.blascommon.properties.JwtConfigurationProperties;
import com.blas.blasidp.payload.JwtRequest;
import com.blas.blasidp.payload.JwtResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

  @Lazy
  private final AuthUserDao authUserDao;

  @Lazy
  private final AuthenticationManager authenticationManager;

  @Lazy
  private final JwtTokenUtil jwtTokenUtil;

  private final JwtUserDetailsService userDetailsService;

  @Lazy
  private final JwtConfigurationProperties jwtConfigurationProperties;

  @PostMapping(value = "/auth/login")
  public ResponseEntity<JwtResponse> createAuthenticationToken(
      @RequestBody JwtRequest authenticationRequest) {
    authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
    final UserDetails userDetails = userDetailsService.loadUserByUsername(
        authenticationRequest.getUsername());
    long timeToExpired = jwtConfigurationProperties.getTimeToExpired();
    log.info(
        "Generated JWT - username: " + authenticationRequest.getUsername() + " - time to expired: "
            + timeToExpired);
    return ResponseEntity.ok(new JwtResponse(jwtTokenUtil.generateToken(userDetails), timeToExpired,
        LocalDateTime.now().minusSeconds(-timeToExpired), null,
        null, "Bearer"));
  }

  private void authenticate(String username, String password) {
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
      if (authUser != null && (authUser.getCountLoginFailed() < THRESHOLD_BLOCK_ACCOUNT
          || !authUser.isBlock())) {
        authUser.setCountLoginFailed(authUser.getCountLoginFailed() + 1);
        if (authUser.getCountLoginFailed() >= THRESHOLD_BLOCK_ACCOUNT) {
          authUser.setBlock(true);
        }
        authUserDao.save(authUser);
      }
      throw new UnauthorizedException(WRONG_CREDENTIAL);
    }
  }
}
