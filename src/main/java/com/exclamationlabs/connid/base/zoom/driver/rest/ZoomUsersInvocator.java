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

package com.exclamationlabs.connid.base.zoom.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.connector.driver.rest.RestRequest;
import com.exclamationlabs.connid.base.connector.driver.rest.RestResponseData;
import com.exclamationlabs.connid.base.connector.filter.FilterType;
import com.exclamationlabs.connid.base.connector.logging.Logger;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.zoom.model.GroupMember;
import com.exclamationlabs.connid.base.zoom.model.UserCreationType;
import com.exclamationlabs.connid.base.zoom.model.ZoomUser;
import com.exclamationlabs.connid.base.zoom.model.request.GroupMembersRequest;
import com.exclamationlabs.connid.base.zoom.model.request.UserCreationRequest;
import com.exclamationlabs.connid.base.zoom.model.request.UserStatusChangeRequest;
import com.exclamationlabs.connid.base.zoom.model.response.GroupMembersResponse;
import com.exclamationlabs.connid.base.zoom.model.response.ListUsersResponse;
import java.util.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class ZoomUsersInvocator implements DriverInvocator<ZoomDriver, ZoomUser> {

  @Override
  public String create(ZoomDriver zoomDriver, ZoomUser zoomUser) throws ConnectorException {

    UserCreationRequest requestData =
        new UserCreationRequest(UserCreationType.CREATE.getZoomName(), zoomUser);

    RestRequest request = new RestRequest.Builder<>(ZoomUser.class)
            .withPost()
            .withRequestUri("/users")
            .withRequestBody(requestData)
            .build();
    RestResponseData<ZoomUser> data = zoomDriver.executeRequest(request);
    ZoomUser newUser =    data.getResponseObject();
    if (newUser == null) {
      throw new ConnectorException("Response from user creation was invalid");
    }
    return newUser.getId();
  }

  @Override
  public void update(ZoomDriver zoomDriver, String userId, ZoomUser userModel)
      throws ConnectorException {
    boolean deactivateOnly = StringUtils.equalsIgnoreCase(userModel.getStatus(), "inactive");
    updateUserStatus(zoomDriver, userModel.getStatus(), userId);

    // If this Update request was an attempt to deactivate a user, do not invoke any other update
    // attempts
    if (!deactivateOnly) {
      zoomDriver.executeRequest(
          new RestRequest.Builder<>(Void.class)
              .withPatch()
              .withRequestBody(userModel)
              .withRequestUri("/users/" + userId)
              .build());

      if (userModel.getGroupsToAdd() != null || userModel.getGroupsToRemove() != null) {
        updateGroupAssignments(
            zoomDriver, userId, userModel.getGroupsToAdd(), userModel.getGroupsToRemove());
      }
    }
  }

  @Override
  public void delete(ZoomDriver zoomDriver, String userId) throws ConnectorException {
    if (BooleanUtils.isTrue(zoomDriver.getConfiguration().getUserDeactivationEnabled())) {
      updateUserStatus(zoomDriver, "inactive", userId);
    } else {
      zoomDriver.executeRequest(
          new RestRequest.Builder<>(Void.class)
              .withDelete()
              .withRequestUri("/users/" + userId)
              .build());
    }
  }

  @Override
  public Set<ZoomUser> getAll(ZoomDriver zoomDriver, ResultsFilter filter, ResultsPaginator paginator, Integer forceNumber)
      throws ConnectorException {

    String status = null;
    Set<ZoomUser> allUsers = null;
    Set<ZoomUser> inactiveUsers = null;
    Set<ZoomUser> activeUsers = null;
    if ( filter != null
            && filter.hasFilter()
            && filter.getFilterType() == FilterType.EqualsFilter
            && filter.getAttribute() != null
            && filter.getAttribute().equalsIgnoreCase("status")) {

        status = filter.getValue();
        allUsers = getUsersByStatus(zoomDriver, status, paginator);
    }
    else if (paginator.hasPagination()) {

      if ( paginator.getTokenAsString() == null || paginator.getTokenAsString().trim().length() == 0 )
      {
        paginator.setToken("active");
      }
      status = paginator.getTokenAsString();
      if ( status.trim().equalsIgnoreCase("active")) {
        activeUsers = getUsersByStatus(zoomDriver, status, paginator);
        if ( activeUsers != null
                && activeUsers.size() > 0
                && paginator.getCurrentPageNumber() <= paginator.getNumberOfTotalPages())
        {
          allUsers = activeUsers;
        }
        if ( paginator.getCurrentPageNumber() >= paginator.getNumberOfTotalPages() )
        {
          paginator.setToken("inactive");
          ResultsPaginator inactivePaginator = new ResultsPaginator(paginator.getPageSize(), 1);
          inactiveUsers = getUsersByStatus(zoomDriver, "inactive", inactivePaginator);
          if ( inactiveUsers != null && inactiveUsers.size() > 0 )
          {
            if (allUsers != null ){
              allUsers.addAll(inactiveUsers);
            }
            else {
              allUsers = inactiveUsers;
            }
            paginator.setNumberOfProcessedResults(paginator.getNumberOfProcessedResults()+inactiveUsers.size());
          }
          paginator.setCurrentPageNumber(inactivePaginator.getCurrentPageNumber());
          paginator.setNumberOfTotalPages(inactivePaginator.getNumberOfTotalPages());
          if ( paginator.getCurrentPageNumber() >= paginator.getNumberOfTotalPages())
          {
            paginator.setNoMoreResults(true);
          }
        }
      }
      else {
        allUsers = getUsersByStatus(zoomDriver, "inactive", paginator);
        if ( paginator.getCurrentPageNumber() >= paginator.getNumberOfTotalPages())
        {
          paginator.setNoMoreResults(true);
        }
      }
    }
    else{
      allUsers = getUsersByStatus(zoomDriver, "active", paginator);
      ResultsPaginator inactivePaginator = new ResultsPaginator();
      allUsers.addAll(getUsersByStatus(zoomDriver, "inactive", inactivePaginator));
      paginator.setNoMoreResults(true);
    }



    return allUsers;
  }

  /**
   * @param zoomDriver Driver belonging to this Invocator and providing interaction with the applicable
   *                   destination system.
   * @param userId     The expected can be the Zoom User id or the Zoom User email address
   * @param dataMap    Map of prefetch data applicable to the Identity Model and that may be
   *                   understood by the invocator.
   * @return
   * @throws ConnectorException
   */
  @Override
  public ZoomUser getOne(ZoomDriver zoomDriver, String userId, Map<String, Object> dataMap)
      throws ConnectorException {
    return zoomDriver
        .executeRequest(
            new RestRequest.Builder<>(ZoomUser.class)
                .withGet()
                .withRequestUri("/users/" + userId)
                .build())
        .getResponseObject();
  }

  private Set<ZoomUser> getUsersByStatus(ZoomDriver zoomDriver, String status, ResultsPaginator paginator) {

    Set<ZoomUser> users = null;
    boolean getAll = false;
    String additionalQueryString = "?status=" + status;
    if ( paginator != null && paginator.hasPagination() )
    {
      additionalQueryString = additionalQueryString + "&page_size=" + paginator.getPageSize();
      if ( paginator.getCurrentPageNumber() == null || paginator.getCurrentPageNumber() <= 0 ) {
        paginator.setCurrentPageNumber(1);
      }
      additionalQueryString = additionalQueryString + "&page_number=" + paginator.getCurrentPageNumber();
    }
    else
    {
      getAll= true;
    }
    RestRequest request = new RestRequest.Builder<>(ListUsersResponse.class)
            .withGet()
            .withRequestUri("/users" + additionalQueryString)
            .build();
    RestResponseData<ListUsersResponse> data = zoomDriver.executeRequest(request);
    ListUsersResponse response = data.getResponseObject();

    if ( response != null  )
    {
      users = response.getUsers();
      paginator.setTotalResults(response.getTotalRecords());
      paginator.setNumberOfProcessedPages(response.getPageNumber());
      paginator.setNumberOfTotalPages(response.getPageCount());
      paginator.setPageSize(response.getPageSize());
      if ( response.getUsers() != null && response.getUsers().size() > 0 )
      {
        if ( paginator.getNumberOfProcessedResults() == null )
        {
          paginator.setNumberOfProcessedResults(0);
        }
        paginator.setNumberOfProcessedResults(paginator.getNumberOfProcessedResults() + response.getUsers().size());
      }

      while (getAll && response.getPageNumber() < response.getPageCount() )
      {
        Integer pageNumber = response.getPageNumber() + 1;
        additionalQueryString = "?status=" + status + "&page_size=" + response.getPageSize() + "&page_number=" + pageNumber;
        request = new RestRequest.Builder<>(ListUsersResponse.class)
                .withGet()
                .withRequestUri("/users" + additionalQueryString)
                .build();
        data = zoomDriver.executeRequest(request);
        response = data.getResponseObject();
        if ( response != null )
        {
          paginator.setTotalResults(response.getTotalRecords());
          paginator.setNumberOfProcessedPages(response.getPageNumber());
          paginator.setNumberOfTotalPages(response.getPageCount());
          paginator.setPageSize(response.getPageSize());
          if ( response.getUsers() != null && response.getUsers().size() > 0 )
          {
            paginator.setNumberOfProcessedResults(paginator.getNumberOfProcessedResults() + response.getUsers().size());
            users.addAll(response.getUsers());
          }
        }
      }
    }

    return users;
  }

  private void updateUserStatus(ZoomDriver zoomDriver, String desiredStatus, String userId) {
    if (desiredStatus == null) {
      return;
    }

    ZoomUser currentUser = getOne(zoomDriver, userId, null);
    if (StringUtils.equalsIgnoreCase(currentUser.getStatus(), desiredStatus)) {
      return;
    }

    UserStatusChangeRequest statusChangeRequest = new UserStatusChangeRequest();
    String statusVerb =
        StringUtils.equalsIgnoreCase(desiredStatus, "active") ? "activate" : "deactivate";
    statusChangeRequest.setAction(statusVerb);
    Logger.info(
        this,
        String.format("Changing status of user id %s, request verb is %s", userId, statusVerb));
    zoomDriver.executeRequest(
        new RestRequest.Builder<>(Void.class)
            .withPut()
            .withRequestBody(statusChangeRequest)
            .withRequestUri("/users/" + userId + "/status")
            .build());

    if (StringUtils.equalsIgnoreCase(desiredStatus, "inactive")
        && BooleanUtils.isTrue(zoomDriver.getConfiguration().getImmediateLogoutOnDeactivate())) {
      Logger.info(
          this, String.format("Deactivation completed.  Deleting SSOToken for user id %s", userId));

      zoomDriver.executeRequest(
          new RestRequest.Builder<>(Void.class)
              .withDelete()
              .withRequestUri("/users/" + userId + "/token")
              .build());
    }
  }

  private void updateGroupAssignments(
      ZoomDriver driver, String userId, Set<String> toAdd, Set<String> toRemove) {

    ZoomUser temp = getOne(driver, userId, Collections.emptyMap());
    Set<String> currentGroupIds = temp.getGroupIds();

    if (toRemove != null) {
      for (String groupId : currentGroupIds) {
        if (toRemove.contains(groupId)) {
          driver.executeRequest(
              new RestRequest.Builder<>(Void.class)
                  .withDelete()
                  .withRequestUri("/groups/" + groupId + "/members/" + userId)
                  .build());
          Logger.info(
              this,
              String.format("Successfully removed group id %s from user id %s", groupId, userId));
        }
      }
    }

    if (toAdd != null) {
      for (String groupId : toAdd) {
        if (!currentGroupIds.contains(groupId)) {
          addGroupToUser(driver, groupId, userId);
          Logger.info(
              this, String.format("Successfully added group id %s to user id %s", groupId, userId));
        }
      }
    }
  }

  private void addGroupToUser(ZoomDriver driver, String groupId, String userId) {
    List<GroupMember> memberList = new ArrayList<>();
    memberList.add(new GroupMember(userId));
    GroupMembersRequest membersRequest = new GroupMembersRequest(memberList);

    GroupMembersResponse response =
        driver
            .executeRequest(
                new RestRequest.Builder<>(GroupMembersResponse.class)
                    .withPost()
                    .withRequestUri("/groups/" + groupId + "/members")
                    .withRequestBody(membersRequest)
                    .build())
            .getResponseObject();

    if (response == null || response.getAddedAt() == null) {
      throw new ConnectorException(
          String.format(
              "Unexpected response received while adding user id %s to group id %s",
              userId, groupId));
    }
  }
}
