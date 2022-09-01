package com.blas.blasidp.payload;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class JwtResponse {

  private final String jwtToken;
  private final long expireIn;
  private final LocalDateTime timeExpired;
  private final String refreshToken;
  private final String identityToken;
  private final String tokenType;
}