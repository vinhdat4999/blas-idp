package com.blas.blasidp.controller;

import static com.blas.blascommon.constants.BlasConstant.BLAS;
import static com.blas.blascommon.constants.Response.CANNOT_CONNECT_TO_HOST;
import static com.blas.blascommon.enums.BlasService.BLAS_IDP;
import static com.blas.blascommon.enums.FileType.JPG;
import static com.blas.blascommon.enums.LogType.ERROR;
import static com.blas.blascommon.security.SecurityUtils.base64Decode;
import static com.blas.blascommon.utils.fileutils.FileUtils.writeByteArrayToFile;
import static com.blas.blascommon.utils.httprequest.PostRequest.sendPostRequestWithJsonArrayPayloadGetJsonObjectResponse;
import static com.blas.blascommon.utils.timeutils.TimeUtils.getTimeNow;
import static com.blas.blasidp.constant.Authentication.AUTHEN_KEY;
import static com.blas.blasidp.constant.Authentication.REGISTER_SUCCESSFULLY;
import static com.blas.blasidp.constant.Authentication.SENT;
import static com.blas.blasidp.constant.Authentication.SUBJECT_EMAIL_AUTHEN_CODE;
import static com.blas.blasidp.constant.Authentication.TEMPLATE_RESEND_KEY;
import static com.blas.blasidp.constant.Authentication.VERIFY_FAILED;
import static com.blas.blasidp.constant.Authentication.VERIFY_SUCCESSFULLY;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.core.model.Role;
import com.blas.blascommon.core.model.UserDetail;
import com.blas.blascommon.core.service.AuthUserService;
import com.blas.blascommon.core.service.AuthenKeyService;
import com.blas.blascommon.core.service.CentralizedLogService;
import com.blas.blascommon.exceptions.types.BadRequestException;
import com.blas.blascommon.exceptions.types.ServiceUnavailableException;
import com.blas.blascommon.jwt.JwtTokenUtil;
import com.blas.blascommon.jwt.JwtUserDetailsService;
import com.blas.blascommon.payload.HtmlEmailRequest;
import com.blas.blascommon.properties.BlasEmailConfiguration;
import com.blas.blascommon.security.hash.Sha256Encoder;
import com.blas.blasidp.payload.RegisterBody;
import com.blas.blasidp.payload.VerifyAccountBody;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController {

  @Value("${blas.blas-idp.isSendEmailAlert}")
  private boolean isSendEmailAlert;

  @Autowired
  private AuthenKeyService authenKeyService;

  @Autowired
  private AuthUserService authUserService;

  @Autowired
  private Sha256Encoder passwordEncoder;

  @Autowired
  private BlasEmailConfiguration blasEmailConfiguration;

  @Autowired
  private CentralizedLogService centralizedLogService;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Autowired
  private JwtUserDetailsService userDetailsService;

  @PostMapping(value = "/auth/register")
  public ResponseEntity<String> registerAccount(@RequestBody RegisterBody registerBody) {
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
    htmlEmailRequest.setEmailTemplateName(TEMPLATE_RESEND_KEY);
    htmlEmailRequest.setData(Map.of(AUTHEN_KEY, authenKeyService.createAuthenKey(authUser)));

    try {
      sendPostRequestWithJsonArrayPayloadGetJsonObjectResponse(host, null, null,
          new JSONArray(List.of(htmlEmailRequest)));
    } catch (IOException e) {
      centralizedLogService.saveLog(BLAS_IDP.getServiceName(), ERROR, e.toString(),
          e.getCause() == null ? EMPTY : e.getCause().toString(),
          new JSONArray(List.of(htmlEmailRequest)).toString(), null, null,
          String.valueOf(new JSONArray(e.getStackTrace())), isSendEmailAlert);
      throw new ServiceUnavailableException(CANNOT_CONNECT_TO_HOST);
    }
    return ResponseEntity.ok(REGISTER_SUCCESSFULLY);
  }

  @PostMapping(value = "/auth/resend-key")
  public ResponseEntity<String> resendAuthenKey(@RequestBody String userId) {
    AuthUser authUser = authUserService.getAuthUserByUserId(userId);
    HtmlEmailRequest htmlEmailRequest = new HtmlEmailRequest();
    htmlEmailRequest.setEmailTo(authUser.getUserDetail().getEmail());
    htmlEmailRequest.setTitle(SUBJECT_EMAIL_AUTHEN_CODE);
    htmlEmailRequest.setEmailTemplateName(TEMPLATE_RESEND_KEY);
    htmlEmailRequest.setData(Map.of(AUTHEN_KEY, authenKeyService.createAuthenKey(authUser)));
    try {
      sendPostRequestWithJsonArrayPayloadGetJsonObjectResponse(
          blasEmailConfiguration.getEndpointHtmlEmail(), null,
          jwtTokenUtil.generateInternalSystemToken(), new JSONArray(List.of(htmlEmailRequest)));
    } catch (IOException | JSONException e) {
      centralizedLogService.saveLog(BLAS_IDP.getServiceName(), ERROR, e.toString(),
          e.getCause() == null ? EMPTY : e.getCause().toString(),
          new JSONArray(List.of(htmlEmailRequest)).toString(), null, null,
          String.valueOf(new JSONArray(e.getStackTrace())), isSendEmailAlert);
      throw new ServiceUnavailableException(CANNOT_CONNECT_TO_HOST);
    }
    return ResponseEntity.ok(SENT);
  }

  @PostMapping(value = "/auth/verify-new-account")
  public ResponseEntity<String> verifyNewAccount(@RequestBody VerifyAccountBody verifyAccountBody) {
    if (authenKeyService.isValidAuthenKey(verifyAccountBody.getUserId(),
        verifyAccountBody.getAuthenKey(), getTimeNow())) {
      AuthUser authUser = authUserService.getAuthUserByUserId(verifyAccountBody.getUserId());
      authenKeyService.useAuthenKey(authUser);
      authUser.setActive(true);
      authUserService.updateAuthUser(authUser);
    } else {
      throw new BadRequestException(VERIFY_FAILED);
    }
    return ResponseEntity.ok(VERIFY_SUCCESSFULLY);
  }
}
