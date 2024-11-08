package com.blas.blasidp.controller;

import static com.blas.blascommon.constants.ResponseMessage.USERNAME_NOT_FOUND;
import static com.blas.blascommon.security.SecurityUtils.getUsernameLoggedIn;
import static com.blas.blascommon.utils.StringUtils.EMPTY;

import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.core.model.UserDetail;
import com.blas.blascommon.core.service.AuthUserService;
import com.blas.blascommon.core.service.CentralizedLogService;
import com.blas.blascommon.core.service.ImgbbService;
import com.blas.blascommon.core.service.UserDetailService;
import com.blas.blascommon.exceptions.BlasErrorCodeEnum;
import com.blas.blascommon.exceptions.types.BadRequestException;
import com.blas.blascommon.exceptions.types.BlasException;
import com.blas.blascommon.exceptions.types.ForbiddenException;
import com.blas.blascommon.security.SecurityUtils;
import com.blas.blascommon.security.hash.Sha256Encoder;
import com.blas.blasidp.payload.UpdateAccountInfoRequest;
import com.blas.blasidp.payload.UpdatePasswordRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AccountInfoController {

  private static final String YOUR_ACCOUNT_IS_BLOCKED_OR_NOT_ACTIVE = "YOUR ACCOUNT IS BLOCKED OR NOT ACTIVE";
  private static final String PASSWORD_UPDATED = "PASSWORD UPDATED";
  private static final String WRONG_OLD_PASSWORD = "WRONG OLD PASSWORD";
  public static final String PLEASE_SELECT_AN_IMAGE_TO_UPLOAD = "Please select an image to upload.";

  @Lazy
  private final UserDetailService userDetailService;

  @Lazy
  private final AuthUserService authUserService;

  @Lazy
  private final Sha256Encoder passwordEncoder;

  @Lazy
  private final ImgbbService imgbbService;

  @Lazy
  private final CentralizedLogService centralizedLogService;

  @PostMapping(value = "/auth/get-user")
  public ResponseEntity<AuthUser> getAuthUser(@RequestBody String username) {
    username = username.replace("\"", EMPTY);
    AuthUser userDetail = authUserService.getAuthUserByUsername(username);
    if (userDetail == null) {
      throw new BadRequestException(USERNAME_NOT_FOUND);
    }
    return ResponseEntity.ok(userDetail);
  }

  @PostMapping(value = "/auth/update-avatar")
  public ResponseEntity<String> updateAvatar(@RequestParam("image") MultipartFile file) {
    if (file.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(PLEASE_SELECT_AN_IMAGE_TO_UPLOAD);
    }

    try {
      byte[] imageBytes = file.getBytes();
      String base64Image = SecurityUtils.base64Encode(imageBytes);
      String displayUrl = imgbbService.publishImage(base64Image);
      UserDetail userDetail = userDetailService.getUserDetailByUsername(getUsernameLoggedIn());
      userDetail.setAvatarPath(displayUrl);
      userDetailService.updateUserDetail(userDetail);
      return ResponseEntity.ok(displayUrl);
    } catch (IOException exception) {
      centralizedLogService.saveLog(exception);
      throw new BlasException(BlasErrorCodeEnum.MSG_FAILURE);
    }
  }

  @PostMapping(value = "/auth/update-account-info")
  public ResponseEntity<UserDetail> updateAccountInfo(
      @RequestBody UpdateAccountInfoRequest updateAccountInfoRequest) {
    UserDetail userDetail = userDetailService.getUserDetailByUsername(
        updateAccountInfoRequest.getUsername());
    if (userDetail == null) {
      throw new BadRequestException(USERNAME_NOT_FOUND);
    }
    userDetail.setFirstName(updateAccountInfoRequest.getFirstName());
    userDetail.setLastName(updateAccountInfoRequest.getLastName());
    userDetail.setPhoneNumber(updateAccountInfoRequest.getPhoneNumber());
    userDetail.setGender(updateAccountInfoRequest.isGender());
    userDetail.setBirthday(updateAccountInfoRequest.getBirthday());
    userDetailService.updateUserDetail(userDetail);
    return ResponseEntity.ok(userDetail);
  }

  @PostMapping(value = "/auth/update-password")
  public ResponseEntity<String> updatePassword(
      @RequestBody UpdatePasswordRequest updatePasswordRequest) {
    AuthUser authUser = authUserService.getAuthUserByUsername(
        updatePasswordRequest.getUsername());
    if (authUser == null) {
      throw new BadRequestException(USERNAME_NOT_FOUND);
    }
    if (authUser.isBlock() || !authUser.isActive()) {
      throw new ForbiddenException(YOUR_ACCOUNT_IS_BLOCKED_OR_NOT_ACTIVE);
    }

    final String plainTextOldPassword = passwordEncoder.encode(
        updatePasswordRequest.getOldPassword());
    if (!StringUtils.equals(plainTextOldPassword, authUser.getPassword())) {
      throw new ForbiddenException(WRONG_OLD_PASSWORD);
    }

    authUser.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
    authUser.setCountLoginFailed(0);
    authUserService.updateAuthUser(authUser);
    return ResponseEntity.ok(PASSWORD_UPDATED);
  }
}
