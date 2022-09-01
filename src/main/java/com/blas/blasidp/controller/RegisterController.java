//package com.blas.blasidp.controller;
//
//import static com.blas.blascommon.utils.TimeUtils.getTimeNow;
//import static com.blas.blasidp.constant.Authentication.REGISTER_SUCCESSFULLY;
//import static com.blas.blasidp.constant.Authentication.SENT;
//import static com.blas.blasidp.constant.Authentication.SUBJECT_EMAIL_AUTHEN_CODE;
//import static com.blas.blasidp.constant.Authentication.VERIFY_FAILED;
//import static com.blas.blasidp.constant.Authentication.VERIFY_SUCCESSFULLY;
//
//import com.blas.blascommon.core.model.AuthUser;
//import com.blas.blascommon.core.model.Role;
//import com.blas.blascommon.core.model.UserDetail;
//import com.blas.blascommon.core.service.AuthUserService;
//import com.blas.blascommon.core.service.AuthenKeyService;
//import com.blas.blascommon.exceptions.types.BadRequestException;
//import com.blas.blascommon.security.SecurityUtils;
//import com.blas.blascommon.security.hash.Sha256Encoder;
//import com.blas.blascommon.utils.email.HtmlEmail;
//import com.blas.blascommon.utils.fileutils.FileUtils;
//import com.blas.blasidp.payload.RegisterBody;
//import com.blas.blasidp.payload.VerifyAccountBody;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class RegisterController {
//
//  @Autowired
//  private AuthenKeyService authenKeyService;
//
//  @Autowired
//  private AuthUserService authUserService;
//
//  @Autowired
//  private Sha256Encoder passwordEncoder;
//
//  @PostMapping(value = "/auth/register")
//  public ResponseEntity<?> registerAccount(@RequestBody RegisterBody registerBody) {
//    Role roleUser = new Role();
//    roleUser.setRoleId("1");
//    AuthUser authUser = new AuthUser();
//    authUser.setUsername(registerBody.getUsername());
//    authUser.setPassword(passwordEncoder.encode(registerBody.getPassword()));
//    authUser.setRole(roleUser);
//    authUser.setCountLoginFailed(0);
//    authUser.setBlock(false);
//    authUser.setActive(false);
//
//    UserDetail userDetail = new UserDetail();
//    userDetail.setFirstName(registerBody.getFirstName());
//    userDetail.setLastName(registerBody.getLastName());
//    userDetail.setPhoneNumber(registerBody.getPhoneNumber());
//    userDetail.setEmail(registerBody.getEmail());
//    userDetail.setGender(registerBody.isGender());
//    userDetail.setBirthday(registerBody.getBirthday());
//    userDetail.setBCoin(0);
//    String avatarPath = "avatar/" + authUser.getUsername() + ".jpg";
//    FileUtils.writeByteArrayToFile(SecurityUtils.base64Decode(registerBody.getAvatarBase64()),
//        avatarPath);
//    userDetail.setAvatarPath(avatarPath);
//
//    authUserService.createUser(authUser, userDetail);
//
//    String authenKey = authenKeyService.createAuthenKey(authUser);
//    HtmlEmail htmlEmail = new HtmlEmail(userDetail.getEmail(), SUBJECT_EMAIL_AUTHEN_CODE,
//        getHtmlContentEmailSendAuthenKey(authenKey));
//    Thread thread = new Thread(htmlEmail);
//    thread.start();
//
//    return ResponseEntity.ok(REGISTER_SUCCESSFULLY);
//  }
//
//  @PostMapping(value = "/auth/resend-key")
//  public ResponseEntity<?> resendAuthenKey(@RequestBody String userId) {
//    AuthUser authUser = authUserService.getAuthUserByUserId(userId);
//    String authenKey = authenKeyService.createAuthenKey(authUser);
//    HtmlEmail htmlEmail = new HtmlEmail(authUser.getUserDetail().getEmail(),
//        SUBJECT_EMAIL_AUTHEN_CODE, getHtmlContentEmailSendAuthenKey(authenKey));
//    Thread thread = new Thread(htmlEmail);
//    thread.start();
//    return ResponseEntity.ok(SENT);
//  }
//
//  @PostMapping(value = "/auth/verify-new-account")
//  public ResponseEntity<?> verifyNewAccount(@RequestBody VerifyAccountBody verifyAccountBody) {
//    if (authenKeyService.isValidAuthenKey(verifyAccountBody.getUserId(),
//        verifyAccountBody.getAuthenKey(), getTimeNow())) {
//      AuthUser authUser = authUserService.getAuthUserByUserId(verifyAccountBody.getUserId());
//      authenKeyService.useAuthenKey(authUser);
//      authUser.setActive(true);
//      authUserService.updateAuthUser(authUser);
//    } else {
//      throw new BadRequestException(VERIFY_FAILED);
//    }
//    return ResponseEntity.ok(VERIFY_SUCCESSFULLY);
//  }
//
//  private String getHtmlContentEmailSendAuthenKey(String authenKey) {
//    String htmlContent = "<!DOCTYPE html>\n"
//        + "<html>\n"
//        + "<head>\n"
//        + "  <meta charset=\"utf-8\">\n"
//        + "  <meta http-equiv=\"x-ua-compatible\" content=\"ie=edge\">\n"
//        + "  <title>Email Confirmation</title>\n"
//        + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
//        + "  <style type=\"text/css\">\n"
//        + "  @media screen {\n"
//        + "    @font-face {\n"
//        + "      font-family: 'Source Sans Pro';\n"
//        + "      font-style: normal;\n"
//        + "      font-weight: 400;\n"
//        + "      src: local('Source Sans Pro Regular'), local('SourceSansPro-Regular'), url(https://fonts.gstatic.com/s/sourcesanspro/v10/ODelI1aHBYDBqgeIAH2zlBM0YzuT7MdOe03otPbuUS0.woff) format('woff');\n"
//        + "    }\n"
//        + "    @font-face {\n"
//        + "      font-family: 'Source Sans Pro';\n"
//        + "      font-style: normal;\n"
//        + "      font-weight: 700;\n"
//        + "      src: local('Source Sans Pro Bold'), local('SourceSansPro-Bold'), url(https://fonts.gstatic.com/s/sourcesanspro/v10/toadOcfmlt9b38dHJxOBGFkQc6VGVFSmCnC_l7QZG60.woff) format('woff');\n"
//        + "    }\n"
//        + "  }\n"
//        + "  body,\n"
//        + "  table,\n"
//        + "  td,\n"
//        + "  a {\n"
//        + "    -ms-text-size-adjust: 100%; /* 1 */\n"
//        + "    -webkit-text-size-adjust: 100%; /* 2 */\n"
//        + "  }\n"
//        + "  table,\n"
//        + "  td {\n"
//        + "    mso-table-rspace: 0pt;\n"
//        + "    mso-table-lspace: 0pt;\n"
//        + "  }\n"
//        + "  img {\n"
//        + "    -ms-interpolation-mode: bicubic;\n"
//        + "  }\n"
//        + "  a[x-apple-data-detectors] {\n"
//        + "    font-family: inherit !important;\n"
//        + "    font-size: inherit !important;\n"
//        + "    font-weight: inherit !important;\n"
//        + "    line-height: inherit !important;\n"
//        + "    color: inherit !important;\n"
//        + "    text-decoration: none !important;\n"
//        + "  }\n"
//        + "  div[style*=\"margin: 16px 0;\"] {\n"
//        + "    margin: 0 !important;\n"
//        + "  }\n"
//        + "\n"
//        + "  body {\n"
//        + "    width: 100% !important;\n"
//        + "    height: 100% !important;\n"
//        + "    padding: 0 !important;\n"
//        + "    margin: 0 !important;\n"
//        + "  }\n"
//        + "  table {\n"
//        + "    border-collapse: collapse !important;\n"
//        + "  }\n"
//        + "  a {\n"
//        + "    color: #1a82e2;\n"
//        + "  }\n"
//        + "  img {\n"
//        + "    height: auto;\n"
//        + "    line-height: 100%;\n"
//        + "    text-decoration: none;\n"
//        + "    border: 0;\n"
//        + "    outline: none;\n"
//        + "  }\n"
//        + "  </style>\n"
//        + "</head>\n"
//        + "<body style=\"background-color: #e9ecef;\">\n"
//        + "  <div class=\"preheader\" style=\"display: none; max-width: 0; max-height: 0; overflow: hidden; font-size: 1px; line-height: 1px; color: #fff; opacity: 0;\">\n"
//        + "    A preheader is the short summary text that follows the subject line when an email is viewed in the inbox.\n"
//        + "  </div>\n"
//        + "  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
//        + "    <tr>\n"
//        + "      <td align=\"center\" bgcolor=\"#e9ecef\">\n"
//        + "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n"
//        + "          <tr>\n"
//        + "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 36px 24px 0; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; border-top: 3px solid #d4dadf;\">\n"
//        + "              <h1 style=\"margin: 0; font-size: 32px; font-weight: 700; letter-spacing: -1px; line-height: 48px;\">Your BLAS verification code</h1>\n"
//        + "            </td>\n"
//        + "          </tr>\n"
//        + "        </table>\n"
//        + "      </td>\n"
//        + "    </tr>\n"
//        + "    <tr>\n"
//        + "      <td align=\"center\" bgcolor=\"#e9ecef\">\n"
//        + "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n"
//        + "          <tr>\n"
//        + "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px;\">\n"
//        + "              <p style=\"margin: 0;\">Do not disclose the verification code to anyone. your authentication code will expire in 20 minutes. Your verification: </p>\n"
//        + "            </td>\n"
//        + "          </tr>\n"
//        + "          <tr>\n"
//        + "            <td align=\"left\" bgcolor=\"#ffffff\">\n"
//        + "              <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
//        + "                <tr>\n"
//        + "                  <td align=\"center\" bgcolor=\"#ffffff\" style=\"padding: 12px;\">\n"
//        + "                    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n"
//        + "                      <tr>\n"
//        + "                        <td align=\"center\" bgcolor=\"#1a82e2\" style=\"border-radius: 6px;\">\n"
//        + "                          <p style=\"display: inline-block; padding: 0px 36px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 28px; color: #ffffff; text-decoration: none; border-radius: 6px;\">"
//        + authenKey + "</p>\n"
//        + "                        </td>\n"
//        + "                      </tr>\n"
//        + "                    </table>\n"
//        + "                  </td>\n"
//        + "                </tr>\n"
//        + "              </table>\n"
//        + "            </td>\n"
//        + "          </tr>\n"
//        + "          <tr>\n"
//        + "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px; border-bottom: 3px solid #d4dadf\">\n"
//        + "              <p style=\"margin: 0;\">Sincerely,<br> BLAS</p>\n"
//        + "            </td>\n"
//        + "          </tr>\n"
//        + "        </table>\n"
//        + "      </td>\n"
//        + "    </tr>\n"
//        + "  </table>\n"
//        + "</body>\n"
//        + "</html>";
//    return htmlContent;
//  }
//}