package com.blas.blasidp.configuration;

import static com.blas.blascommon.constants.BlasConstant.FACEBOOK;
import static com.blas.blascommon.constants.BlasConstant.GOOGLE;
import static com.blas.blascommon.constants.SecurityConstant.SLASH_REPLACE;
import static com.blas.blascommon.security.SecurityUtils.aesEncrypt;
import static com.blas.blascommon.utils.IdUtils.genMixID;
import static com.blas.blascommon.utils.StringUtils.SLASH;
import static com.blas.blasidp.constant.Authentication.REGISTER_SUCCESSFULLY;
import static com.blas.blasidp.utils.AuthUtils.generateToken;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.upperCase;

import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.core.model.Role;
import com.blas.blascommon.core.model.UserDetail;
import com.blas.blascommon.core.service.AuthUserService;
import com.blas.blascommon.jwt.JwtTokenUtil;
import com.blas.blascommon.properties.JwtConfigurationProperties;
import com.blas.blascommon.security.KeyService;
import com.blas.blascommon.security.hash.Sha256Encoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BlasOAuth2AuthenSuccess implements AuthenticationSuccessHandler {

  private static final int LENGTH_OF_RANDOMLY_DEFAULT_PASSWORD = 30;
  private static final String NAME_FIELD = "name";
  private static final String GIVEN_NAME_FIELD = "given_name";
  private static final String FAMILY_NAME_FIELD = "family_name";
  private static final String PICTURE_FIELD = "picture";

  private final AuthUserService authUserService;

  private final JwtTokenUtil jwtTokenUtil;

  @Lazy
  private final Sha256Encoder passwordEncoder;

  @Lazy
  private final JwtConfigurationProperties jwtConfigurationProperties;

  @Lazy
  private final KeyService keyService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    log.debug("BlasOAuth2AuthenSuccess invoked");
    BlasOAuth2User oauthUser = (BlasOAuth2User) authentication.getPrincipal();
    final String email = oauthUser.getEmail();
    AuthUser authUser = authUserService.getAuthUserByEmail(email);
    if (authUser == null) {
      authUser = signUpBlasAccountViaOAuth2(oauthUser, authentication);
      log.debug("Register new Blas account via OAuth2");
    }
    String username = authUser.getUsername();
    log.info(
        "Generated JWT - username: " + username + " - time to expired: "
            + jwtConfigurationProperties.getTimeToExpired());
    log.debug("Login via OAuth2 successfully");
    String aedEncryptedToken = null;
    try {
      aedEncryptedToken = aesEncrypt(keyService.getBlasPrivateKey(),
          generateToken(jwtTokenUtil, username));
    } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
             InvalidAlgorithmParameterException | InvalidKeyException |
             NoSuchAlgorithmException exception) {
      log.error(exception.toString());
    }
    response.sendRedirect(
        "/auth/token-via-oath2/" + replace(aedEncryptedToken, SLASH, SLASH_REPLACE));
  }

  private AuthUser signUpBlasAccountViaOAuth2(BlasOAuth2User oAuth2User,
      Authentication authentication) {
    Role roleUser = new Role();
    roleUser.setRoleId(com.blas.blascommon.enums.Role.USER.name());

    AuthUser authUser = new AuthUser();
    String email = oAuth2User.getEmail();
    authUser.setUsername(email);
    authUser.setPassword(passwordEncoder.encode(genMixID(LENGTH_OF_RANDOMLY_DEFAULT_PASSWORD)));
    authUser.setRole(roleUser);
    authUser.setCountLoginFailed(0);
    authUser.setBlock(false);
    authUser.setActive(true);
    authUser.setProvider(upperCase(
        ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()));

    UserDetail userDetail = new UserDetail();
    switch (authUser.getProvider()) {
      case GOOGLE -> {
        userDetail.setFirstName(
            (String) oAuth2User.getAttributes().getOrDefault(GIVEN_NAME_FIELD, EMPTY));
        userDetail.setLastName(
            (String) oAuth2User.getAttributes().getOrDefault(FAMILY_NAME_FIELD, EMPTY));
      }
      case FACEBOOK -> userDetail.setFirstName(
          (String) oAuth2User.getAttributes().getOrDefault(NAME_FIELD, EMPTY));
      default -> {
        // no operation
      }
    }
    userDetail.setEmail(email);
    userDetail.setBCoin(0);
    userDetail.setAvatarPath(
        (String) oAuth2User.getAttributes().getOrDefault(PICTURE_FIELD, EMPTY));
    authUserService.createUser(authUser, userDetail);
    log.info(REGISTER_SUCCESSFULLY + " - username: " + email + " via OAuth2");
    return authUser;
  }
}
