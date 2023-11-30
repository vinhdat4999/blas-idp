package com.blas.blasidp.controller;

import static com.blas.blascommon.constants.BlasConstant.BLAS;
import static com.blas.blascommon.enums.EmailTemplate.RESEND_KEY;
import static com.blas.blascommon.enums.FileType.JPG;
import static com.blas.blascommon.security.SecurityUtils.base64Decode;
import static com.blas.blascommon.utils.fileutils.FileUtils.writeByteArrayToFile;
import static com.blas.blasidp.constant.Authentication.ACCOUNT_ALREADY_ACTIVE;
import static com.blas.blasidp.constant.Authentication.AUTHEN_KEY;
import static com.blas.blasidp.constant.Authentication.REGISTER_SUCCESSFULLY;
import static com.blas.blasidp.constant.Authentication.SENT_AUTHEN_EMAIL;
import static com.blas.blasidp.constant.Authentication.SUBJECT_EMAIL_AUTHEN_CODE;
import static com.blas.blasidp.constant.Authentication.VERIFY_FAILED;
import static com.blas.blasidp.constant.Authentication.VERIFY_SUCCESSFULLY;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.core.model.Role;
import com.blas.blascommon.core.model.UserDetail;
import com.blas.blascommon.core.service.AuthUserService;
import com.blas.blascommon.core.service.AuthenKeyService;
import com.blas.blascommon.core.service.CentralizedLogService;
import com.blas.blascommon.exceptions.types.BadRequestException;
import com.blas.blascommon.exceptions.types.ForbiddenException;
import com.blas.blascommon.jwt.JwtTokenUtil;
import com.blas.blascommon.payload.HtmlEmailRequest;
import com.blas.blascommon.properties.BlasEmailConfiguration;
import com.blas.blascommon.security.hash.Sha256Encoder;
import com.blas.blascommon.utils.TelegramUtils;
import com.blas.blasidp.configuration.EmailQueueService;
import com.blas.blasidp.payload.RegisterBody;
import com.blas.blasidp.payload.VerifyAccountBody;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RegisterController {

  private static final String TELEGRAM_AUTHEN_KEY_MSG = "Do not disclose the verification code to anyone. Your authentication code will expire in 20 minutes. Your verification: ";
  @Lazy
  private final AuthenKeyService authenKeyService;
  @Lazy
  private final AuthUserService authUserService;
  @Lazy
  private final Sha256Encoder passwordEncoder;
  @Lazy
  private final BlasEmailConfiguration blasEmailConfiguration;
  @Lazy
  private final CentralizedLogService centralizedLogService;
  @Lazy
  private final JwtTokenUtil jwtTokenUtil;
  @Lazy
  private final TelegramUtils telegramUtils;
  @Lazy
  private final EmailQueueService emailQueueService;
  @Value("${blas.blas-idp.isSendEmailAlert}")
  private boolean isSendEmailAlert;
  @Value("${blas.service.serviceName}")
  private String serviceName;

  @PostMapping(value = "/auth/register")
  public ResponseEntity<String> registerAccount(@RequestBody RegisterBody registerBody)
      throws IOException {
    Role roleUser = new Role();
    roleUser.setRoleId(com.blas.blascommon.enums.Role.USER.name());

    AuthUser authUser = new AuthUser();
    authUser.setUsername(registerBody.getUsername());
    authUser.setPassword(passwordEncoder.encode(registerBody.getPassword()));
    authUser.setRole(roleUser);
    authUser.setCountLoginFailed(0);
    authUser.setBlock(false);
    authUser.setActive(false);
    authUser.setProvider(BLAS);

    UserDetail userDetail = new UserDetail();
    userDetail.setFirstName(registerBody.getFirstName());
    userDetail.setLastName(registerBody.getLastName());
    userDetail.setPhoneNumber(registerBody.getPhoneNumber());
    userDetail.setEmail(registerBody.getEmail());
    userDetail.setGender(registerBody.isGender());
    userDetail.setBirthday(registerBody.getBirthday());
    userDetail.setBCoin(0);
    if (!isBlank(registerBody.getAvatarBase64())) {
      String avatarPath = "avatar/" + authUser.getUsername() + "." + JPG.getPostfix();
      writeByteArrayToFile(base64Decode(registerBody.getAvatarBase64()), avatarPath);
      userDetail.setAvatarPath(avatarPath);
    }
    authUserService.createUser(authUser, userDetail);

    String host = blasEmailConfiguration.getEndpointHtmlEmail();

    HtmlEmailRequest htmlEmailRequest = new HtmlEmailRequest();
    htmlEmailRequest.setEmailTo(userDetail.getEmail());
    htmlEmailRequest.setTitle(SUBJECT_EMAIL_AUTHEN_CODE);
    htmlEmailRequest.setEmailTemplateName(RESEND_KEY.name());
    htmlEmailRequest.setData(Map.of(AUTHEN_KEY, authenKeyService.createAuthenKey(authUser)));

    emailQueueService.sendMessage(new JSONArray(List.of(htmlEmailRequest)).toString());
    log.info(REGISTER_SUCCESSFULLY + " - username: " + registerBody.getUsername());
    return ResponseEntity.ok(REGISTER_SUCCESSFULLY);
  }

  @PostMapping(value = "/auth/resend-key")
  public ResponseEntity<String> resendAuthenKey(@RequestBody String username) {
    AuthUser authUser = authUserService.getAuthUserByUsername(username);
    if (authUser.isActive()) {
      throw new ForbiddenException(ACCOUNT_ALREADY_ACTIVE);
    }

    final String authenKey = authenKeyService.createAuthenKey(authUser);
    try {
      telegramUtils.sendTelegramMessage(TELEGRAM_AUTHEN_KEY_MSG + authenKey,
          authUser.getUserDetail().getTelegramChatId());
    } catch (URISyntaxException | InvalidAlgorithmParameterException | UnrecoverableKeyException |
             IllegalBlockSizeException | NoSuchPaddingException | CertificateException |
             KeyStoreException | NoSuchAlgorithmException | BadPaddingException |
             InvalidKeyException | IOException exception) {
      log.error(exception.toString());
    }

    HtmlEmailRequest htmlEmailRequest = new HtmlEmailRequest();
    htmlEmailRequest.setEmailTo(authUser.getUserDetail().getEmail());
    htmlEmailRequest.setTitle(SUBJECT_EMAIL_AUTHEN_CODE);
    htmlEmailRequest.setEmailTemplateName(RESEND_KEY.name());
    htmlEmailRequest.setData(Map.of(AUTHEN_KEY, authenKey));
    emailQueueService.sendMessage(new JSONArray(List.of(htmlEmailRequest)).toString());
    return ResponseEntity.ok(SENT_AUTHEN_EMAIL);
  }

  @PostMapping(value = "/auth/verify-new-account")
  public ResponseEntity<String> verifyNewAccount(@RequestBody VerifyAccountBody verifyAccountBody) {
    if (authenKeyService.isValidAuthenKey(verifyAccountBody.getUsername(),
        verifyAccountBody.getAuthenKey(), now())) {
      AuthUser authUser = authUserService.getAuthUserByUsername(verifyAccountBody.getUsername());
      if (authUser.isActive()) {
        throw new ForbiddenException(ACCOUNT_ALREADY_ACTIVE);
      }
      authenKeyService.useAuthenKey(authUser);
      authUser.setActive(true);
      authUserService.updateAuthUser(authUser);
    } else {
      throw new BadRequestException(VERIFY_FAILED);
    }
    return ResponseEntity.ok(VERIFY_SUCCESSFULLY);
  }
}
