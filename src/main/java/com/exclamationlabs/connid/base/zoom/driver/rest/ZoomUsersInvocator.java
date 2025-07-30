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
import com.exclamationlabs.connid.base.zoom.model.*;
import com.exclamationlabs.connid.base.zoom.model.request.GroupMembersRequest;
import com.exclamationlabs.connid.base.zoom.model.request.UserCreationRequest;
import com.exclamationlabs.connid.base.zoom.model.request.UserStatusChangeRequest;
import com.exclamationlabs.connid.base.zoom.model.response.GroupMembersResponse;
import com.exclamationlabs.connid.base.zoom.model.response.ListSitesResponse;
import com.exclamationlabs.connid.base.zoom.model.response.ListUsersResponse;
import com.exclamationlabs.connid.base.zoom.model.response.ZoomUserCreateResponse;
import java.util.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class ZoomUsersInvocator implements DriverInvocator<ZoomDriver, ZoomUser> {
  private static final Log LOG = Log.getLog(ZoomUsersInvocator.class);

  @Override
  public String create(ZoomDriver driver, ZoomUser zoomUser) throws ConnectorException {

    ZoomUser user = null;
    String id = null;
    // Attempt to Create the User
    UserCreationRequest requestData =
        new UserCreationRequest(UserCreationType.CREATE.getZoomName(), zoomUser);

    RestRequest request =
        new RestRequest.Builder<>(ZoomUserCreateResponse.class)
            .withPost()
            .withRequestUri("/users")
            .withRequestBody(requestData)
            .build();
    RestResponseData<ZoomUserCreateResponse> data = driver.executeRequest(request);
    ZoomUserCreateResponse response = data.getResponseObject();
    if (data.getResponseStatusCode() == 201) {
      response = data.getResponseObject();
      if (response != null && response.getId() != null && response.getId().trim().length() == 0) {
        Logger.warn(this, "User with this email has unlinked account");
      }
      id = response.getId();
      // User might still be pending when action
    } else if (data.getResponseStatusCode() == 409) {
      user = getOneByName(driver, zoomUser.getEmail());
      if (user != null) {
        id = user.getId();
      }
      // Continue with Update if not pending
    } else if (data.getResponseStatusCode() == 429) {
      Logger.warn(this, String.format("Error %d:  %s", response.getCode(), response.getMessage()));
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        ;
      }
    } else {
      Logger.warn(this, String.format("Error %d:  %s", response.getCode(), response.getMessage()));
    }
    // Post Create Activity

    if (id == null) {
      throw new ConnectorException("Response from user creation was invalid");
    }
    return id;
  }

  /**
   * The Method must Support Zoom Phone correctly 1) Update Calling Plan 2) Specify Extension
   * Number, Site, Multiple e164 Phone Numbers 3)
   *
   * @param driver Driver belonging to this Invocator and providing interaction with the applicable
   *     destination system.
   * @param userId String holding the id to match the item being updated on the destination system.
   * @param user Model holding the data for the object to be updated. Null fields present are to be
   *     treated as elements that should remain unchanged on the destination system.
   * @throws ConnectorException
   */
  @Override
  public void update(ZoomDriver driver, String userId, ZoomUser user) throws ConnectorException {

    boolean deactivateOnly = StringUtils.equalsIgnoreCase(user.getStatus(), "inactive");
    updateUserStatus(driver, user.getStatus(), userId);

    // If this Update request was an attempt to deactivate a user, do not invoke any other update
    // attempts
    if (!deactivateOnly) {

      RestRequest req =
          new RestRequest.Builder<>(Void.class)
              .withPatch()
              .withRequestBody(user)
              .withRequestUri("/users/" + userId)
              .build();
      RestResponseData<Void> response;
      try {
        response = driver.executeRequest(req);
      } catch(UserDisabledException e) {
        if (driver.getConfiguration().getIgnoreUpdateErrorOnDisabledUser() 
            || driver.getConfiguration().getUpdateDisabledUsers() 
            || driver.getConfiguration().getRemovePhoneFeatureOnDisabledUser()) {
          handleDisabledUser(driver, userId, user);
          return;
        } else {
          // proceed as normal and throw exception
          throw new ConnectorException(e);
        }
      }

      if (response.getResponseStatusCode() == 204) {
        ZoomUser current = getOne(driver, userId, null);
        // Update email if required
        if (current != null
            && current.getEmail() != null
            && current.getEmail().trim().length() > 0
            && user.getEmail() != null
            && user.getEmail().trim().length() > 0
            && !current.getEmail().trim().equalsIgnoreCase(user.getEmail().trim())) {
          if (updateUserEmail(driver, userId, user.getEmail().trim())) {
            current.setEmail(user.getEmail().trim());
          }
        }
        // Update Groups
        if (user.getGroupsToAdd() != null || user.getGroupsToRemove() != null) {
          updateGroupAssignments(driver, userId, user.getGroupsToAdd(), user.getGroupsToRemove());
        }
        current = getOne(driver, userId, null);
        if (current.getFeature() != null
            && current.getFeature().getZoomPhone() != null
            && current.getFeature().getZoomPhone()) {
          // Update User Profile if needed
          String siteId = null;
          if (user.getSite() != null
              && user.getSite().getId() != null
              && user.getSite().getId().trim().length() > 0) {
            siteId = user.getSite().getId();
          } else if (user.getSite() != null
              && user.getSite().getName() != null
              && user.getSite().getName().trim().length() > 0) {
            ZoomPhoneSite aSite = getZoomPhoneSiteFromName(driver, user.getSite().getName());
            if (aSite != null) {
              siteId = aSite.getId();
            }
          } else if (current.getSite() != null
              && current.getSite().getId() != null
              && current.getSite().getId().trim().length() > 0) {
            siteId = current.getSite().getId();
          } else if (user.getSite() != null && user.getSite().getName() != null) {
            ZoomPhoneSite site = getZoomPhoneSiteFromName(driver, user.getSite().getName());
            if (site != null) {
              siteId = site.getId();
            }
          }
          String extension = user.getPhoneProfile().getExtension();
          if (siteId != null && extension != null) {
            updatePhoneUserProfile(driver, userId, current.getPhoneProfile(), extension, siteId);
          }
          // Remove Calling plans
          unAssignPhoneCallingPlans(driver, userId, user.getOutboundRemove().getPlans());
          // Update Calling Plans if needed
          assignPhoneCallingPlans(driver, userId, user.getOutboundAdd().getPlans());
          // Un-assign Phone Numbers
          unAssignPhoneNumbers(driver, userId, user.getOutboundRemove().getPhones());
          // Assign Phone Numbers
          assignPhoneNumbers(
              driver,
              userId,
              user.getOutboundAdd().getPhones(),
              current.getPhoneProfile().getPhoneNumbers());
        }
      } else {
        Logger.warn(
            this,
            String.format(
                "Status %d: Cannot update user %s", response.getResponseStatusCode(), userId));
      }
    }
  }

  @Override
  public void delete(ZoomDriver driver, String userId) throws ConnectorException {
    RestRequest req = null;
    ZoomUser user = getOne(driver, userId, null);
    if (driver.getConfiguration().getDeactivateOnDelete()) {
      if (user.getStatus().equalsIgnoreCase("pending")) {
        LOG.warn("Cannot DeActivate Pending user id=" + userId);
        updateUserStatus(driver, "inactive", userId);
      } else if (user.getStatus().equalsIgnoreCase("inactive")) {
        LOG.warn("User Already inactive: id=" + userId);
      } else {
        updateUserStatus(driver, "inactive", userId);
      }

    } else if (driver.getConfiguration().getDisassociateOnDelete()) {

      req =
          new RestRequest.Builder<>(Void.class)
              .withDelete()
              .withRequestUri("/users/" + userId)
              .build();
      driver.executeRequest(req);
    } else {
      String uri = "/users/" + userId + "?action=delete";
      req = new RestRequest.Builder<>(Void.class).withDelete().withRequestUri(uri).build();
      driver.executeRequest(req);
    }
  }

  @Override
  public Set<ZoomUser> getAll(
      ZoomDriver zoomDriver, ResultsFilter filter, ResultsPaginator paginator, Integer forceNumber)
      throws ConnectorException {

    String status = null;
    Set<ZoomUser> allUsers = null;
    Set<ZoomUser> inactiveUsers = null;
    Set<ZoomUser> activeUsers = null;
    if (filter != null
        && filter.hasFilter()
        && filter.getFilterType() == FilterType.EqualsFilter
        && filter.getAttribute() != null
        && filter.getAttribute().equalsIgnoreCase("status")) {

      status = filter.getValue();
      allUsers = getUsersByStatus(zoomDriver, status, paginator);
    } else if (paginator.hasPagination()) {

      if (paginator.getTokenAsString() == null
          || paginator.getTokenAsString().trim().length() == 0) {
        paginator.setToken("active");
      }
      status = paginator.getTokenAsString();
      if (status.trim().equalsIgnoreCase("active")) {
        activeUsers = getUsersByStatus(zoomDriver, status, paginator);
        if (activeUsers != null
            && activeUsers.size() > 0
            && paginator.getCurrentPageNumber() <= paginator.getNumberOfTotalPages()) {
          allUsers = activeUsers;
        }
        if (paginator.getCurrentPageNumber() >= paginator.getNumberOfTotalPages()) {
          paginator.setToken("inactive");
          ResultsPaginator inactivePaginator = new ResultsPaginator(paginator.getPageSize(), 1);
          inactiveUsers = getUsersByStatus(zoomDriver, "inactive", inactivePaginator);
          if (inactiveUsers != null && inactiveUsers.size() > 0) {
            if (allUsers != null) {
              allUsers.addAll(inactiveUsers);
            } else {
              allUsers = inactiveUsers;
            }
            paginator.setNumberOfProcessedResults(
                paginator.getNumberOfProcessedResults() + inactiveUsers.size());
          }
          paginator.setCurrentPageNumber(inactivePaginator.getCurrentPageNumber());
          paginator.setNumberOfTotalPages(inactivePaginator.getNumberOfTotalPages());
          if (paginator.getCurrentPageNumber() >= paginator.getNumberOfTotalPages()) {
            paginator.setNoMoreResults(true);
          }
        }
      } else {
        allUsers = getUsersByStatus(zoomDriver, "inactive", paginator);
        if (paginator.getCurrentPageNumber() >= paginator.getNumberOfTotalPages()) {
          paginator.setNoMoreResults(true);
        }
      }
    } else {
      allUsers = getUsersByStatus(zoomDriver, "active", paginator);
      ResultsPaginator inactivePaginator = new ResultsPaginator();
      allUsers.addAll(getUsersByStatus(zoomDriver, "inactive", inactivePaginator));
      paginator.setNoMoreResults(true);
    }

    return allUsers;
  }

  /**
   * @param driver Driver belonging to this Invocator and providing interaction with the applicable
   *     destination system.
   * @param userId The expected can be the Zoom User id or the Zoom User email address
   * @param dataMap Map of prefetch data applicable to the Identity Model and that may be understood
   *     by the invocator.
   * @return
   * @throws ConnectorException
   */
  @Override
  public ZoomUser getOne(ZoomDriver driver, String userId, Map<String, Object> dataMap)
      throws ConnectorException {
    ZoomUser user = null;
    RestRequest req =
        new RestRequest.Builder<>(ZoomUser.class)
            .withGet()
            .withRequestUri("/users/" + userId)
            .build();
    RestResponseData<ZoomUser> response = driver.executeRequest(req);
    if (response.getResponseStatusCode() == 200) {
      user = response.getResponseObject();
      getPhoneInfo(driver, user);
    }

    return user;
  }

  /**
   * Get the user by email address
   *
   * @param driver
   * @param userName
   * @return
   * @throws ConnectorException
   */
  @Override
  public ZoomUser getOneByName(ZoomDriver driver, String userName) throws ConnectorException {
    ZoomUser user = null;
    RestRequest req =
        new RestRequest.Builder<>(ZoomUser.class)
            .withGet()
            .withRequestUri("/users/" + userName)
            .build();
    RestResponseData<ZoomUser> response = driver.executeRequest(req);
    user = response.getResponseObject();
    if (response.getResponseStatusCode() == 200) {
      user = response.getResponseObject();
      getPhoneInfo(driver, user);
    }
    return user;
  }

  public ZoomUser getOneByName(
      ZoomDriver driver, String userName, Map<String, Object> prefetchDataMap)
      throws ConnectorException {
    return getOneByName(driver, userName);
  }
  /**
   * Get the phone user profile
   *
   * @param driver
   * @param id
   * @return
   * @throws ConnectorException
   */
  public ZoomPhoneUserProfile getPhoneUserProfile(ZoomDriver driver, String id)
      throws ConnectorException {
    ZoomPhoneUserProfile phoneUserProfile = null;
    RestRequest req =
        new RestRequest.Builder<>(ZoomPhoneUserProfile.class)
            .withGet()
            .withRequestUri("/phone/users/" + id)
            .build();
    RestResponseData<ZoomPhoneUserProfile> response = driver.executeRequest(req);
    phoneUserProfile = response.getResponseObject();
    return phoneUserProfile;
  }

  public void getPhoneInfo(ZoomDriver driver, ZoomUser user) {
    if (user != null
        && user.getId() != null
        && user.getId().trim().length() > 0
        && user.getStatus() != null
        && !user.getStatus().trim().equalsIgnoreCase("pending")) {
      ZoomFeature feature = new ZoomFeature();
      ZoomPhoneUserProfile phoneProfile = getPhoneUserProfile(driver, user.getId());
      if (phoneProfile != null) {
        feature.setZoomPhone(true);
        user.setPhoneProfile(phoneProfile);
        user.setFeature(feature);
        ZoomPhoneSite site = getZoomPhoneSiteFromId(driver, phoneProfile.getSiteId());
        user.setSite(site);
      }
    }
  }

  private Set<ZoomUser> getUsersByStatus(
      ZoomDriver zoomDriver, String status, ResultsPaginator paginator) {

    Set<ZoomUser> users = null;
    boolean getAll = false;
    String additionalQueryString = "?status=" + status;
    if (paginator != null && paginator.hasPagination()) {
      additionalQueryString = additionalQueryString + "&page_size=" + paginator.getPageSize();
      if (paginator.getCurrentPageNumber() == null || paginator.getCurrentPageNumber() <= 0) {
        paginator.setCurrentPageNumber(1);
      }
      additionalQueryString =
          additionalQueryString + "&page_number=" + paginator.getCurrentPageNumber();
    } else {
      getAll = true;
    }
    RestRequest request =
        new RestRequest.Builder<>(ListUsersResponse.class)
            .withGet()
            .withRequestUri("/users" + additionalQueryString)
            .build();
    RestResponseData<ListUsersResponse> data = zoomDriver.executeRequest(request);
    ListUsersResponse response = data.getResponseObject();

    if (response != null) {
      users = response.getUsers();
      paginator.setTotalResults(response.getTotalRecords());
      paginator.setNumberOfProcessedPages(response.getPageNumber());
      paginator.setNumberOfTotalPages(response.getPageCount());
      paginator.setPageSize(response.getPageSize());
      if (response.getUsers() != null && response.getUsers().size() > 0) {
        if (paginator.getNumberOfProcessedResults() == null) {
          paginator.setNumberOfProcessedResults(0);
        }
        paginator.setNumberOfProcessedResults(
            paginator.getNumberOfProcessedResults() + response.getUsers().size());
      }

      while (getAll && response.getPageNumber() < response.getPageCount()) {
        Integer pageNumber = response.getPageNumber() + 1;
        additionalQueryString =
            "?status="
                + status
                + "&page_size="
                + response.getPageSize()
                + "&page_number="
                + pageNumber;
        request =
            new RestRequest.Builder<>(ListUsersResponse.class)
                .withGet()
                .withRequestUri("/users" + additionalQueryString)
                .build();
        data = zoomDriver.executeRequest(request);
        response = data.getResponseObject();
        if (response != null) {
          paginator.setTotalResults(response.getTotalRecords());
          paginator.setNumberOfProcessedPages(response.getPageNumber());
          paginator.setNumberOfTotalPages(response.getPageCount());
          paginator.setPageSize(response.getPageSize());
          if (response.getUsers() != null && response.getUsers().size() > 0) {
            paginator.setNumberOfProcessedResults(
                paginator.getNumberOfProcessedResults() + response.getUsers().size());
            users.addAll(response.getUsers());
          }
        }
      }
    }

    return users;
  }

  private Set<ZoomPhoneSite> getPhoneSiteList(ZoomDriver driver) {
    String uri = "/phone/sites";
    RestRequest req =
        new RestRequest.Builder<>(ListSitesResponse.class).withGet().withRequestUri(uri).build();

    RestResponseData<ListSitesResponse> response = driver.executeRequest(req);

    Set<ZoomPhoneSite> sites = response.getResponseObject().getSites();

    return sites;
  }

  private ZoomPhoneSite getZoomPhoneSiteFromId(ZoomDriver driver, String siteId) {
    ZoomPhoneSite site = null;
    Set<ZoomPhoneSite> aSet = getPhoneSiteList(driver);
    List<ZoomPhoneSite> aList = new ArrayList<>(aSet);
    for (ZoomPhoneSite item : aList) {
      if (item.getId() != null && item.getId().trim().equalsIgnoreCase(siteId.trim())) {
        site = item;
        break;
      }
    }
    return site;
  }

  private ZoomPhoneSite getZoomPhoneSiteFromName(ZoomDriver driver, String siteName) {
    ZoomPhoneSite site = null;
    Set<ZoomPhoneSite> aSet = getPhoneSiteList(driver);
    List<ZoomPhoneSite> aList = new ArrayList<>(aSet);
    for (ZoomPhoneSite item : aList) {
      if (item.getName() != null && item.getName().trim().equalsIgnoreCase(siteName.trim())) {
        site = item;
        break;
      }
    }
    return site;
  }

  private boolean updateUserEmail(ZoomDriver driver, String userId, String userEmail) {
    boolean result = false;
    RestRequest req =
        new RestRequest.Builder<>(Void.class)
            .withPut()
            .withRequestBody("{\"email\": \"" + userEmail + "\"}")
            .withRequestUri("/users/" + userId + "/email")
            .build();

    RestResponseData<Void> response = driver.executeRequest(req);
    if (response.getResponseStatusCode() == 200 || response.getResponseStatusCode() == 204) {
      result = true;
    } else {
      Logger.warn(
          this,
          String.format(
              "Status %d: Cannot change email address of user %s",
              response.getResponseStatusCode(), userId));
    }
    return result;
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

  private Integer updatePhoneUserProfile(
      ZoomDriver driver,
      String userId,
      ZoomPhoneUserProfile current,
      String extension,
      String siteId) {
    Integer statusCode = null;
    String requestBody = null;
    String ext = null;
    String sit = null;
    if (current == null || !current.getExtension().equalsIgnoreCase(extension)) {
      ext = String.format("\"extension_number\":\"%s\"", extension);
    }
    if (current == null || !current.getSiteId().equalsIgnoreCase(siteId)) {
      sit = String.format("\"site_id\":\"%s\"", siteId);
    }
    if (ext != null && sit != null) {
      requestBody = String.format("{%s,%s}", ext, sit);
    } else if (ext != null) {
      requestBody = String.format("{%s}", ext);
    } else if (sit != null) {
      requestBody = String.format("{%s}", sit);
    }
    if (requestBody != null) {
      RestRequest req =
          new RestRequest.Builder<>(Void.class)
              .withPatch()
              .withRequestBody(requestBody)
              .withRequestUri("/phone/users/" + userId)
              .build();
      RestResponseData<Void> response = driver.executeRequest(req);
      if (response != null) {
        statusCode = response.getResponseStatusCode();
      }
    }
    return statusCode;
  }

  private Integer assignPhoneCallingPlans(
      ZoomDriver driver, String userId, Set<Integer> callingPlans) {
    Integer statusCode = null;
    if (callingPlans != null && callingPlans.size() > 0) {
      String requestBody = "{\"calling_plans\": [";
      Boolean firstPlan = true;
      for (Integer plan : callingPlans) {
        if (firstPlan) {
          requestBody += String.format("{\"type\": %d}", plan);
          firstPlan = false;
        } else {
          requestBody += String.format(", {\"type\": %d}", plan);
        }
      }
      requestBody += "]}";
      RestRequest req =
          new RestRequest.Builder<>(Void.class)
              .withPost()
              .withRequestBody(requestBody)
              .withRequestUri("/phone/users/" + userId + "/calling_plans")
              .build();
      RestResponseData<Void> response = driver.executeRequest(req);
      if (response != null) {
        statusCode = response.getResponseStatusCode();
      }
    }
    return statusCode;
  }

  private Integer assignPhoneNumbers(
      ZoomDriver driver, String userId, Set<String> phoneNumbers, Set<ZoomPhoneNumber> current) {
    Integer statusCode = null;
    if (phoneNumbers != null && phoneNumbers.size() > 0) {
      String requestBody = "";
      Boolean first = true;
      for (String phoneNumber : phoneNumbers) {
        boolean match = false;
        if (current != null) {
          for (ZoomPhoneNumber item : current) {
            if (item.getNumber() != null
                && phoneNumber != null
                && item.getNumber().equalsIgnoreCase(phoneNumber)) {
              match = true;
            }
          }
        }
        if (!match) {
          if (first) {
            requestBody += String.format("{\"number\": \"%s\"}", phoneNumber);
            first = false;
          } else {
            requestBody += String.format(", {\"number\": \"%s\"}", phoneNumber);
          }
        }
      }
      if (requestBody.trim().length() > 0) {
        requestBody = "{\"phone_numbers\": [" + requestBody + "]}";
        RestRequest req =
            new RestRequest.Builder<>(String.class)
                .withRequestBody(requestBody)
                .withPost()
                .withRequestUri("/phone/users/" + userId + "/phone_numbers/")
                .build();
        RestResponseData<String> response = driver.executeRequest(req);
        if (response != null) {
          statusCode = response.getResponseStatusCode();
        }
      }
    }
    return statusCode;
  }

  private Integer unAssignPhoneCallingPlans(
      ZoomDriver driver, String userId, Set<Integer> callingPlans) {
    Integer statusCode = null;
    if (callingPlans != null && callingPlans.size() > 0) {
      for (Integer plan : callingPlans) {
        RestRequest req =
            new RestRequest.Builder<>(Void.class)
                .withDelete()
                .withRequestUri("/phone/users/" + userId + "/calling_plans/" + plan)
                .build();
        RestResponseData<Void> response = driver.executeRequest(req);
        if (response != null) {
          statusCode = response.getResponseStatusCode();
        }
      }
    }
    return statusCode;
  }

  private Integer unAssignPhoneNumbers(ZoomDriver driver, String userId, Set<String> phoneNumbers) {
    Integer statusCode = null;
    if (phoneNumbers != null && phoneNumbers.size() > 0) {
      for (String number : phoneNumbers) {
        RestRequest req =
            new RestRequest.Builder<>(Void.class)
                .withDelete()
                .withRequestUri("/phone/users/" + userId + "/phone_numbers/" + number)
                .build();
        RestResponseData<Void> response = driver.executeRequest(req);
        if (response != null) {
          statusCode = response.getResponseStatusCode();
        }
      }
    }
    return statusCode;
  }
  
  /**
   * Handle update on disabled user if config options are enabled
   * @param driver driver
   * @param userId zoom user id
   * @param user delta user object
   */
  private void handleDisabledUser(ZoomDriver driver, String userId, ZoomUser user) {
    // check config enable action
    // this will change the phone feature too
    if (driver.getConfiguration().getUpdateDisabledUsers()) {
      Logger.info(this, String.format("Updating a disabled user %s",  userId));
      updateUserStatus(driver, "active", userId);
      RestRequest req =
          new RestRequest.Builder<>(Void.class)
              .withPatch()
              .withRequestBody(user)
              .withRequestUri("/users/" + userId)
              .build();
      RestResponseData<Void> response = driver.executeRequest(req);
      updateUserStatus(driver, "inactive", userId);
      return;
    }
    
    // check to see if the phone feature should be removed.
    // removal of the phone feature removes numbers and sites, so don't need
    // do to removal of those too. Will coincidentally update other attributes too
    if (driver.getConfiguration().getRemovePhoneFeatureOnDisabledUser()
        && user.getFeature() != null
        && !user.getFeature().getZoomPhone()) {
      Logger.info(this, String.format("Removing phone feature from a disabled user %s",  userId));
      updateUserStatus(driver, "active", userId);
      RestRequest req =
          new RestRequest.Builder<>(Void.class)
              .withPatch()
              .withRequestBody(user)
              .withRequestUri("/users/" + userId)
              .build();
      RestResponseData<Void> response = driver.executeRequest(req);
      updateUserStatus(driver, "inactive", userId);
      return;
    }
     
    // check config for ignore action
    // last possible option here, so no op
    Logger.info(this, String.format("Ignoring a disabled user %s",  userId));
  }
}
