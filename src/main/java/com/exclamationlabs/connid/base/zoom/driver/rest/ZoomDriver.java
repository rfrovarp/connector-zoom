package com.exclamationlabs.connid.base.zoom.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.rest.BaseRestDriver;
import com.exclamationlabs.connid.base.connector.driver.rest.RestFaultProcessor;
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
            executeGetRequest("/accounts/me/settings", null);
        } catch (Exception e) {
            throw new ConnectorException("Self-identification for Zoom connection user failed.", e);
        }
    }

    @Override
    public void close() {

    }
}
