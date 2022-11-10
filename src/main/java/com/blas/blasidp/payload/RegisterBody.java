package com.blas.blasidp.payload;

import java.time.LocalDate;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
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
