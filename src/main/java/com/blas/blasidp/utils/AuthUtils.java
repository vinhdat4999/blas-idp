package com.blas.blasidp.utils;

import com.blas.blascommon.jwt.JwtTokenUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthUtils {

  public static String generateToken(JwtTokenUtil jwtTokenUtil, String username) {
    return jwtTokenUtil.generateToken(username);
  }
}
