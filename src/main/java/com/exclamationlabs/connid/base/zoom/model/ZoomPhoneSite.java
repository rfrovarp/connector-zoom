package com.exclamationlabs.connid.base.zoom.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ZoomPhoneSite {
  @SerializedName("site_code")
  private Integer code;

  private Map<String, String> country;
  private String id;
  private String level;
  private String name;

  @SerializedName("main_auto_receptionist")
  private Map<String, String> receptionist;

  public Integer getCode() {
    return code;
  }

  public Map<String, String> getCountry() {
    return country;
  }

  public String getId() {
    return id;
  }

  public String getLevel() {
    return level;
  }

  public String getName() {
    return name;
  }

  public Map<String, String> getReceptionist() {
    return receptionist;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public void setCountry(Map<String, String> country) {
    this.country = country;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setReceptionist(Map<String, String> receptionist) {
    this.receptionist = receptionist;
  }
}
