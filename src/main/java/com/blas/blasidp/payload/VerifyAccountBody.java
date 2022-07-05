package com.blas.blasidp.payload;

import lombok.Data;

@Data
public class VerifyAccountBody {

    private String userId;
    private String authenKey;

}
