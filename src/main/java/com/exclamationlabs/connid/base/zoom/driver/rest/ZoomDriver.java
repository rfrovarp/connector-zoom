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
