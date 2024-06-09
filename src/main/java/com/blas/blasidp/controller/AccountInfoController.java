package com.blas.blasidp.controller;

import static com.blas.blascommon.constants.ResponseMessage.USERNAME_NOT_FOUND;

import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.core.model.UserDetail;
import com.blas.blascommon.core.service.AuthUserService;
import com.blas.blascommon.core.service.UserDetailService;
import com.blas.blascommon.exceptions.types.BadRequestException;
import com.blas.blascommon.exceptions.types.ForbiddenException;
import com.blas.blascommon.security.hash.Sha256Encoder;
import com.blas.blasidp.payload.UpdateAccountInfoRequest;
import com.blas.blasidp.payload.UpdatePasswordRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountInfoController {

  private static final String YOUR_ACCOUNT_IS_BLOCKED_OR_NOT_ACTIVE = "YOUR ACCOUNT IS BLOCKED OR NOT ACTIVE";
  private static final String PASSWORD_UPDATED = "PASSWORD UPDATED";
  private static final String WRONG_OLD_PASSWORD = "WRONG OLD PASSWORD";

  @Lazy
  private final UserDetailService userDetailService;

  @Lazy
  private final AuthUserService authUserService;

  @Lazy
  private final Sha256Encoder passwordEncoder;

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
