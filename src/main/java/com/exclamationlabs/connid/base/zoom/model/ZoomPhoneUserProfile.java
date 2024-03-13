package com.exclamationlabs.connid.base.zoom.model;

import com.google.gson.annotations.SerializedName;
import java.util.Set;

public class ZoomPhoneUserProfile {
  private transient Set<Integer> plans;

  @SerializedName("extension_number")
  private String extension;

  @SerializedName("phone_numbers")
  private Set<ZoomPhoneNumber> phoneNumbers;

  private transient Set<String> phones;

  @SerializedName("calling_plans")
  private Set<ZoomCallingPlan> callingPlans;

  @SerializedName("site_id")
  private String siteId;

  private String status;

  public Set<Integer> getPlans() {
    return plans;
  }

  public String getExtension() {
    return extension;
  }

  public Set<ZoomPhoneNumber> getPhoneNumbers() {
    return phoneNumbers;
  }

  public Set<String> getPhones() {
    return phones;
  }

  public Set<ZoomCallingPlan> getCallingPlans() {
    return callingPlans;
  }

  public String getSiteId() {
    return siteId;
  }

  public String getStatus() {
    return status;
  }

  public void setPlans(Set<Integer> plans) {
    this.plans = plans;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public void setPhoneNumbers(Set<ZoomPhoneNumber> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public void setPhones(Set<String> phones) {
    this.phones = phones;
  }

  public void setCallingPlans(Set<ZoomCallingPlan> callingPlans) {
    this.callingPlans = callingPlans;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
