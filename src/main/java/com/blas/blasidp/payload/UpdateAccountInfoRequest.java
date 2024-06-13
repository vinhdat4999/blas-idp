package com.blas.blasidp.payload;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateAccountInfoRequest {

  @NotEmpty
  private String username;

  @NotEmpty
  private String firstName;

  @NotEmpty
  private String lastName;

  @NotEmpty
  private String phoneNumber;

  @NotEmpty
  private boolean gender;

  @NotEmpty
  private LocalDate birthday;
}
