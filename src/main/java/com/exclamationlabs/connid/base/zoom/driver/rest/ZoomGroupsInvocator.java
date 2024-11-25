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
import com.exclamationlabs.connid.base.zoom.model.ZoomGroup;
import com.exclamationlabs.connid.base.zoom.model.response.ListGroupsResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class ZoomGroupsInvocator implements DriverInvocator<ZoomDriver, ZoomGroup> {

  @Override
  public String create(ZoomDriver zoomDriver, ZoomGroup groupModel) throws ConnectorException {

    try {
      ZoomGroup newGroup =
          zoomDriver
              .executeRequest(
                  new RestRequest.Builder<>(ZoomGroup.class)
                      .withPost()
                      .withRequestUri("/groups")
                      .withRequestBody(groupModel)
                      .build())
              .getResponseObject();

      if (newGroup == null || newGroup.getId() == null) {
        throw new ConnectorException("Response from group creation was invalid");
      }

      return newGroup.getId();
    } catch (PaidAccountRequiredException paid) {
      throw new ConnectorException(
          "Creation of group not possible without paid Zoom subscription", paid);
    }
  }

  @Override
  public void update(ZoomDriver zoomDriver, String groupId, ZoomGroup groupModel)
      throws ConnectorException {
    try {
      ZoomGroup modifyGroup = new ZoomGroup();
      // Cannot send key in update JSON, and name is the only field to update,
      // so create a new object w/ just the name set
      modifyGroup.setName(groupModel.getName());

      zoomDriver.executeRequest(
          new RestRequest.Builder<>(Void.class)
              .withPatch()
              .withRequestUri("/groups/" + groupId)
              .withRequestBody(modifyGroup)
              .build());
    } catch (PaidAccountRequiredException paid) {
      Logger.warn(
          this,
          String.format(
              "Update of group id %s not possible without paid Zoom subscription", groupId),
          paid);
    }
  }

  @Override
  public void delete(ZoomDriver zoomDriver, String groupId) throws ConnectorException {
    if (zoomDriver.getConfiguration().getEntitlementDeletionEnabled()) {
      try {
        zoomDriver.executeRequest(
                new RestRequest.Builder<>(Void.class)
                        .withDelete()
                        .withRequestUri("/groups/" + groupId)
                        .build());
      } catch (PaidAccountRequiredException paid) {
        Logger.warn(
                this,
                String.format(
                        "Deletion of group id %s not possible without paid Zoom subscription", groupId),
                paid);
      }
    } else {
      throw new UnsupportedOperationException("Delete not supported for Zoom groups");
    }
  }

  @Override
  public Set<ZoomGroup> getAll(
      ZoomDriver zoomDriver, ResultsFilter filter, ResultsPaginator paginator, Integer forceNum)
      throws ConnectorException {
    try {
      ListGroupsResponse response =
          zoomDriver
              .executeRequest(
                  new RestRequest.Builder<>(ListGroupsResponse.class)
                      .withGet()
                      .withRequestUri("/groups")
                      .build())
              .getResponseObject();
      if (paginator.hasPagination()) {
        paginator.setNoMoreResults(true);
      }
      return response.getGroups();
    } catch (PaidAccountRequiredException paid) {
      Logger.warn(
          this,
          "Retrieval of groups not possible without paid Zoom subscription.  Returning no groups.",
          paid);
      return Collections.emptySet();
    }
  }

  @Override
  public ZoomGroup getOne(ZoomDriver zoomDriver, String groupId, Map<String, Object> dataMap)
      throws ConnectorException {
    try {
      return zoomDriver
          .executeRequest(
              new RestRequest.Builder<>(ZoomGroup.class)
                  .withGet()
                  .withRequestUri("/groups/" + groupId)
                  .build())
          .getResponseObject();
    } catch (PaidAccountRequiredException paid) {
      Logger.warn(
          this,
          "Retrieval of groups not possible without paid Zoom subscription.  Returning no match.",
          paid);

      return null;
    }
  }

  /**
   * @param driver Driver belonging to this Invocator and providing interaction with the applicable
   *     destination system.
   * @param name String holding the identifier for the object being sought on the destination
   *     system.
   * @param prefetchDataMap Map of prefetch data applicable to the Identity Model and that may be
   *     understood by the invocator.
   * @return A ZoomGroup object with the name specified
   * @throws ConnectorException
   */
  @Override
  public ZoomGroup getOneByName(ZoomDriver driver, String name, Map<String, Object> prefetchDataMap)
      throws ConnectorException {
    ZoomGroup item = null;
    Set<ZoomGroup> groups = getAll(driver, null, new ResultsPaginator(), null);
    if (groups != null && groups.size() > 0) {
      for (ZoomGroup group : groups) {
        if (name != null
            && group.getName() != null
            && group.getName().trim().equalsIgnoreCase(name.trim())) {
          item = getOne(driver, group.getId(), prefetchDataMap);
        }
      }
    }
    return item;
  }
}
