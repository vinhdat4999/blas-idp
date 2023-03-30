package com.blas.blasidp.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RegisterBody {

  @NotEmpty
  private String username;
  @NotEmpty
  private String firstName;
  @NotEmpty
  private String lastName;
  @NotEmpty
  private String phoneNumber;
  @NotEmpty
  @Email
  private String email;
  @NotEmpty
  private boolean gender;
  @NotEmpty
  private LocalDate birthday;
  @NotEmpty
  private String password;
  private String avatarBase64;
}
