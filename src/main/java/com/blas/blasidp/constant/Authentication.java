package com.blas.blasidp.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Authentication {

  public static final String WRONG_CREDENTIAL = "WRONG CREDENTIAL";
  public static final String ACCOUNT_INACTIVE = "ACCOUNT INACTIVE";
  public static final String ACCOUNT_BLOCKED = "ACCOUNT BLOCKED";
  public static final String REGISTER_SUCCESSFULLY = "REGISTER SUCCESSFULLY. BLAS SENT AN EMAIL CONTAINING THE AUTHENTICATION CODE TO YOUR EMAIL";
  public static final String VERIFY_SUCCESSFULLY = "VERIFY SUCCESSFULLY";
  public static final String SENT_AUTHEN_EMAIL = "BLAS SENT AN EMAIL CONTAINING THE AUTHENTICATION CODE TO YOUR EMAIL";
  public static final String VERIFY_FAILED = "VERIFY FAILED";
  public static final String ACCOUNT_ALREADY_ACTIVE = "ACCOUNT ALREADY ACTIVE";
  public static final int THRESHOLD_BLOCK_ACCOUNT = 5;
  public static final String SUBJECT_EMAIL_AUTHEN_CODE = "BLAS VERIFICATION CODE";
  public static final String AUTHEN_KEY = "authenKey";
}
