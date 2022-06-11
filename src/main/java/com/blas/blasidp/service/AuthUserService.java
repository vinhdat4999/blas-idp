package com.blas.blasidp.service;

import com.blas.blascommon.core.model.AuthUser;

public interface AuthUserService {

    public AuthUser getAuthUserByUsername(String username);

}
