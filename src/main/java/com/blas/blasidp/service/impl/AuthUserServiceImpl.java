package com.blas.blasidp.service.impl;

import static com.blas.blascommon.constants.Response.USERNAME_NOT_FOUND;

import com.blas.blascommon.core.dao.AuthUserDao;
import com.blas.blascommon.core.model.AuthUser;
import com.blas.blascommon.exceptions.types.NotFoundException;
import com.blas.blasidp.service.AuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = {Exception.class, Throwable.class})
public class AuthUserServiceImpl implements AuthUserService {

    @Autowired
    private AuthUserDao authUserDao;

    @Override
    public AuthUser getAuthUserByUsername(String username) {
        AuthUser authUser = authUserDao.getAuthUserByUsername(username);
        if (authUser == null) {
            throw new NotFoundException(USERNAME_NOT_FOUND);
        }
        return authUser;
    }
}
