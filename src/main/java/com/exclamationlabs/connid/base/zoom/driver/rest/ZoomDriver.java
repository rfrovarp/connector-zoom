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

import com.exclamationlabs.connid.base.connector.driver.rest.BaseRestDriver;
import com.exclamationlabs.connid.base.connector.driver.rest.RestFaultProcessor;
import com.exclamationlabs.connid.base.connector.driver.rest.RestRequest;
import com.exclamationlabs.connid.base.zoom.configuration.ZoomConfiguration;
import com.exclamationlabs.connid.base.zoom.model.ZoomGroup;
import com.exclamationlabs.connid.base.zoom.model.ZoomUser;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class ZoomDriver extends BaseRestDriver<ZoomConfiguration> {

  public ZoomDriver() {
    super();
    addInvocator(ZoomUser.class, new ZoomUsersInvocator());
    addInvocator(ZoomGroup.class, new ZoomGroupsInvocator());
  }

  @Override
  protected boolean usesBearerAuthorization() {
    return true;
  }

  @Override
  protected RestFaultProcessor getFaultProcessor() {
    return ZoomFaultProcessor.getInstance();
  }

  @Override
  protected String getBaseServiceUrl() {
    return getConfiguration().getServiceUrl();
  }

  @Override
  public void test() throws ConnectorException {
    try {
      ZoomUser adminUser =
          executeRequest(
                  new RestRequest.Builder<>(ZoomUser.class)
                      .withGet()
                      .withRequestUri("/users/me")
                      .build())
              .getResponseObject();
      if (adminUser == null || adminUser.getId() == null) {
        throw new ConnectorException("Invalid admin user response");
      }
    } catch (Exception e) {
      throw new ConnectorException("Test for Zoom connection user failed.", e);
    }
  }

  @Override
  public void close() {}
}
