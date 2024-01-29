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

package com.exclamationlabs.connid.base.zoom.model.response;

import com.exclamationlabs.connid.base.zoom.model.ZoomUser;
import com.google.gson.annotations.SerializedName;
import java.util.Set;

public class ListUsersResponse {
  @SerializedName("next_page_token")
  private String nextPageToken;

  @SerializedName("page_count")
  private Integer pageCount;

  @SerializedName("page_number")
  private Integer pageNumber;

  @SerializedName("page_size")
  private Integer pageSize;

  @SerializedName("total_records")
  private Integer totalRecords;

  private Set<ZoomUser> users;

  public String getNextPageToken() {
    return nextPageToken;
  }

  public Integer getPageCount() {
    return pageCount;
  }

  public Integer getPageNumber() {
    return pageNumber;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public Integer getTotalRecords() {
    return totalRecords;
  }

  public Set<ZoomUser> getUsers() {
    return users;
  }

  public void setNextPageToken(String nextPageToken) {
    this.nextPageToken = nextPageToken;
  }

  public void setPageCount(Integer pageCount) {
    this.pageCount = pageCount;
  }

  public void setPageNumber(Integer pageNumber) {
    this.pageNumber = pageNumber;
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public void setTotalRecords(Integer totalRecords) {
    this.totalRecords = totalRecords;
  }

  public void setUsers(Set<ZoomUser> users) {
    this.users = users;
  }
}
