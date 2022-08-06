package com.blas.blasidp.payload;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JwtRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
