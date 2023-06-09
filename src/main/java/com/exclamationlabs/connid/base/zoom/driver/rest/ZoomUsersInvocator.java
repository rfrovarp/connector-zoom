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

    ZoomUser newUser =
        zoomDriver
            .executeRequest(
                new RestRequest.Builder<>(ZoomUser.class)
                    .withPost()
                    .withRequestUri("/users")
                    .withRequestBody(requestData)
                    .build())
            .getResponseObject();

    if (newUser == null) {
      throw new ConnectorException("Response from user creation was invalid");
    }

    if (zoomUser.getGroupIds() != null && !zoomUser.getGroupIds().isEmpty()) {
      updateGroupAssignments(zoomDriver, newUser.getId(), zoomUser.getGroupIds(), false);
    }
    return newUser.getId();
  }

  @Override
  public void update(ZoomDriver zoomDriver, String userId, ZoomUser userModel)
      throws ConnectorException {

    boolean deactivated = updateUserStatus(zoomDriver, userModel.getStatus(), userId);

    if (!deactivated) {
      zoomDriver.executeRequest(
          new RestRequest.Builder<>(Void.class)
              .withPatch()
              .withRequestBody(userModel)
              .withRequestUri("/users/" + userId)
              .build());

      if (userModel.getGroupIds() != null) {
        updateGroupAssignments(zoomDriver, userModel.getId(), userModel.getGroupIds(), true);
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
  public Set<ZoomUser> getAll(
      ZoomDriver zoomDriver, ResultsFilter filter, ResultsPaginator paginator, Integer forceNumber)
      throws ConnectorException {

    Set<ZoomUser> allUsers = getUsersByStatus(zoomDriver, "active");
    allUsers.addAll(getUsersByStatus(zoomDriver, "inactive"));
    allUsers.addAll(getUsersByStatus(zoomDriver, "pending"));

    return allUsers;
  }

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

  private Set<ZoomUser> getUsersByStatus(ZoomDriver zoomDriver, String status) {
    String additionalQueryString = "?status=" + status;
    return zoomDriver
        .executeRequest(
            new RestRequest.Builder<>(ListUsersResponse.class)
                .withGet()
                .withRequestUri("/users" + additionalQueryString)
                .build())
        .getResponseObject()
        .getUsers();
  }

  private boolean updateUserStatus(ZoomDriver zoomDriver, String desiredStatus, String userId) {
    if (desiredStatus == null) {
      return false;
    }

    ZoomUser currentUser = getOne(zoomDriver, userId, null);
    if (StringUtils.equalsIgnoreCase(currentUser.getStatus(), desiredStatus)) {
      return false;
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

    return StringUtils.equalsIgnoreCase(desiredStatus, "inactive");
  }

  private void updateGroupAssignments(
      ZoomDriver driver, String userId, Set<String> updatedGroupIds, boolean userUpdate) {
    Set<String> currentGroupIds = new HashSet<>();
    if (userUpdate) {
      ZoomUser temp = getOne(driver, userId, Collections.emptyMap());
      currentGroupIds = temp.getGroupIds();
    }

    for (String groupId : currentGroupIds) {
      if (!updatedGroupIds.contains(groupId)) {
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

    for (String groupId : updatedGroupIds) {
      if (!currentGroupIds.contains(groupId)) {
        // new group was added
        addGroupToUser(driver, groupId, userId);
        Logger.info(
            this, String.format("Successfully added group id %s to user id %s", groupId, userId));
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
