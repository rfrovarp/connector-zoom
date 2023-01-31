package com.exclamationlabs.connid.base.zoom.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.rest.BaseRestDriver;
import com.exclamationlabs.connid.base.connector.driver.rest.RestFaultProcessor;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
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
      ResultsPaginator paginator = new ResultsPaginator();
      paginator.setPageSize(3);
      getInvocator(ZoomUser.class).getAll(this, new ResultsFilter(), paginator, null);
    } catch (Exception e) {
      throw new ConnectorException("Test for Zoom connection user failed.", e);
    }
  }

  @Override
  public void close() {}
}
