package com.exclamationlabs.connid.base.zoom.model;

import com.google.gson.annotations.SerializedName;

public class ZoomFeature {
  @SerializedName("zoom_one_type")
  private Integer zoomOneType;

  @SerializedName("zoom_phone")
  private Boolean zoomPhone;

  public Integer getZoomOneType() {
    return zoomOneType;
  }

  public Boolean getZoomPhone() {
    return zoomPhone;
  }

  public void setZoomOneType(Integer zoomOneType) {
    this.zoomOneType = zoomOneType;
  }

  public void setZoomPhone(Boolean zoomPhone) {
    this.zoomPhone = zoomPhone;
  }
}
