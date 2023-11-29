package com.blas.blasidp.utils;

import com.blas.blascommon.jwt.JwtTokenUtil;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthUtils {

  public static String generateToken(JwtTokenUtil jwtTokenUtil, String username)
      throws InvalidAlgorithmParameterException, UnrecoverableKeyException, IllegalBlockSizeException, NoSuchPaddingException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    return jwtTokenUtil.generateToken(username);
  }
}
