package com.blas.blasidp.payload;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdatePasswordRequest {

  @NotEmpty
  private String username;

  @NotEmpty
  private String oldPassword;

  @NotEmpty
  private String newPassword;
}
