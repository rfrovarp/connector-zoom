/*
    Copyright 2020 Exclamation Labs

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.exclamationlabs.connid.base.zoom.model;

import com.exclamationlabs.connid.base.connector.model.IdentityModel;
import com.google.gson.annotations.SerializedName;
import java.util.Set;

public class ZoomUser implements IdentityModel {

  @SerializedName("created_at")
  private String createdAt;

  private String email;
  private ZoomFeature feature;

  @SerializedName("first_name")
  private String firstName;

  @SerializedName("group_ids")
  private Set<String> groupIds;

  private transient Set<String> groupsToAdd;
  private transient Set<String> groupsToRemove;
  private String id;
  private transient ZoomPhoneUserProfile outboundAdd;
  private transient ZoomPhoneUserProfile outboundRemove;
  private String language;

  @SerializedName("last_login_time")
  private String lastLoginTime;

  @SerializedName("last_name")
  private String lastName;

  private String password;

  @SerializedName("pmi")
  private Long personalMeetingId;

  @SerializedName("phone_country")
  private String phoneCountry;

  @SerializedName("phone_number")
  private String phoneNumber;

  private transient ZoomPhoneUserProfile phoneProfile;
  private transient ZoomPhoneSite site;
  private String status;
  private String timezone;
  private Integer type;
  private String verified;

  public String getCreatedAt() {
    return createdAt;
  }

  public String getEmail() {
    return email;
  }

  public ZoomFeature getFeature() {
    return feature;
  }

  public String getFirstName() {
    return firstName;
  }

  public Set<String> getGroupIds() {
    return groupIds;
  }

  public Set<String> getGroupsToAdd() {
    return groupsToAdd;
  }

  public Set<String> getGroupsToRemove() {
    return groupsToRemove;
  }

  public String getId() {
    return id;
  }

  @Override
  public String getIdentityIdValue() {
    return getId();
  }

  @Override
  public String getIdentityNameValue() {
    return getEmail();
  }

  public ZoomPhoneUserProfile getOutboundAdd() {
    return outboundAdd;
  }

  public ZoomPhoneUserProfile getOutboundRemove() {
    return outboundRemove;
  }

  public String getLanguage() {
    return language;
  }

  public String getLastLoginTime() {
    return lastLoginTime;
  }

  public String getLastName() {
    return lastName;
  }

  public String getPassword() {
    return password;
  }

  public Long getPersonalMeetingId() {
    return personalMeetingId;
  }

  public String getPhoneCountry() {
    return phoneCountry;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public ZoomPhoneUserProfile getPhoneProfile() {
    return phoneProfile;
  }

  public ZoomPhoneSite getSite() {
    return site;
  }

  public String getStatus() {
    return status;
  }

  public String getTimezone() {
    return timezone;
  }

  public Integer getType() {
    return type;
  }

  public String getVerified() {
    return verified;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setFeature(ZoomFeature feature) {
    this.feature = feature;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setGroupIds(Set<String> groupIds) {
    this.groupIds = groupIds;
  }

  public void setGroupsToAdd(Set<String> groupsToAdd) {
    this.groupsToAdd = groupsToAdd;
  }

  public void setGroupsToRemove(Set<String> groupsToRemove) {
    this.groupsToRemove = groupsToRemove;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setOutboundAdd(ZoomPhoneUserProfile outboundAdd) {
    this.outboundAdd = outboundAdd;
  }

  public void setOutboundRemove(ZoomPhoneUserProfile outboundRemove) {
    this.outboundRemove = outboundRemove;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setLastLoginTime(String lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPersonalMeetingId(Long personalMeetingId) {
    this.personalMeetingId = personalMeetingId;
  }

  public void setPhoneCountry(String phoneCountry) {
    this.phoneCountry = phoneCountry;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setPhoneProfile(ZoomPhoneUserProfile phoneProfile) {
    this.phoneProfile = phoneProfile;
  }

  public void setSite(ZoomPhoneSite site) {
    this.site = site;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public void setVerified(String verified) {
    this.verified = verified;
  }
}
