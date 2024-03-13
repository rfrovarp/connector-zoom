package com.exclamationlabs.connid.base.zoom.model.response;

import com.google.gson.annotations.SerializedName;

public class ZoomUserCreateResponse {
  private Integer code;
  private String email;

  @SerializedName("first_name")
  private String firstName;

  private String id;

  @SerializedName("last_name")
  private String lastName;

  private String message;
  private Integer type;

  public Integer getCode() {
    return code;
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getId() {
    return id;
  }

  public String getLastName() {
    return lastName;
  }

  public String getMessage() {
    return message;
  }

  public Integer getType() {
    return type;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setType(Integer type) {
    this.type = type;
  }
}
