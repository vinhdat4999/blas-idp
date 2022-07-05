package com.blas.blasidp.controller;

import static com.blas.blasidp.constant.Authentication.REGISTER_SUCCESSFULLY;

import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.core.model.Role;
import com.blas.blascommon.core.model.UserDetail;
import com.blas.blascommon.core.service.AuthUserService;
import com.blas.blascommon.core.service.AuthenKeyService;
import com.blas.blascommon.security.SecurityUtils;
import com.blas.blascommon.security.hash.Sha256Encoder;
import com.blas.blascommon.utils.fileutils.FileUtils;
import com.blas.blasidp.payload.RegisterBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController {

    @Autowired
    private AuthenKeyService authenKeyService;

    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private Sha256Encoder passwordEncoder;

    @PostMapping(value = "/register")
    public ResponseEntity<?> registerAccount(@RequestBody RegisterBody registerBody) {
        Role roleUser = new Role();
        roleUser.setRoleId("1");
        AuthUser authUser = new AuthUser();
        authUser.setUsername(registerBody.getUsername());
        authUser.setPassword(passwordEncoder.encode(registerBody.getPassword()));
        authUser.setRole(roleUser);
        authUser.setCountLoginFailed(0);
        authUser.setBlock(false);
        authUser.setActive(false);

        UserDetail userDetail = new UserDetail();
        userDetail.setFirstName(registerBody.getFirstName());
        userDetail.setLastName(registerBody.getLastName());
        userDetail.setPhoneNumber(registerBody.getPhoneNumber());
        userDetail.setEmail(registerBody.getEmail());
        userDetail.setGender(registerBody.isGender());
        userDetail.setBirthday(registerBody.getBirthday());
        userDetail.setBCoin(0);
        String avatarPath = "avatar/" + authUser.getUsername() + ".jpg";
        FileUtils.writeByteArrayToFile(
                SecurityUtils.base64Decode(registerBody.getAvatarBase64()),
                avatarPath);
        userDetail.setAvatarPath(avatarPath);

        authUserService.createUser(authUser, userDetail);

        authenKeyService.createAuthenKey(authUser);

        return ResponseEntity.ok(REGISTER_SUCCESSFULLY);
    }
}