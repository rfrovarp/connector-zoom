package com.exclamationlabs.connid.base.zoom.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ZoomPhoneBatchUser {
  @SerializedName("calling_plans")
  private List<String> calling_plans;

  private String email;

  @SerializedName("extension_number")
  private String extensionNumber;

  private String firstName;
  private String lastName;

  @SerializedName("outbound_caller_id")
  private String outboundCallerId;

  @SerializedName("phone_numbers")
  private List<String> phone_numbers;

  @SerializedName("site_code")
  private String siteCode;

  @SerializedName("site_name")
  private String siteName;

  private Boolean sms;

  @SerializedName("template_name")
  private String templateName;

  public List<String> getCalling_plans() {
    return calling_plans;
  }

  public String getEmail() {
    return email;
  }

  public String getExtensionNumber() {
    return extensionNumber;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getOutboundCallerId() {
    return outboundCallerId;
  }

  public List<String> getPhone_numbers() {
    return phone_numbers;
  }

  public String getSiteCode() {
    return siteCode;
  }

  public String getSiteName() {
    return siteName;
  }

  public Boolean getSms() {
    return sms;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setCalling_plans(List<String> calling_plans) {
    this.calling_plans = calling_plans;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setExtensionNumber(String extensionNumber) {
    this.extensionNumber = extensionNumber;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setOutboundCallerId(String outboundCallerId) {
    this.outboundCallerId = outboundCallerId;
  }

  public void setPhone_numbers(List<String> phone_numbers) {
    this.phone_numbers = phone_numbers;
  }

  public void setSiteCode(String siteCode) {
    this.siteCode = siteCode;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  public void setSms(Boolean sms) {
    this.sms = sms;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }
}
