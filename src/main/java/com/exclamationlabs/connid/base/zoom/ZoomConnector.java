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

package com.exclamationlabs.connid.base.zoom;

import com.exclamationlabs.connid.base.connector.BaseFullAccessConnector;
import com.exclamationlabs.connid.base.connector.authenticator.Authenticator;
import com.exclamationlabs.connid.base.connector.authenticator.OAuth2TokenClientCredentialsAuthenticator;
import com.exclamationlabs.connid.base.zoom.adapter.ZoomGroupsAdapter;
import com.exclamationlabs.connid.base.zoom.adapter.ZoomUsersAdapter;
import com.exclamationlabs.connid.base.zoom.configuration.ZoomConfiguration;
import com.exclamationlabs.connid.base.zoom.driver.rest.ZoomDriver;
import java.util.Collections;
import java.util.Map;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(
    displayNameKey = "zoom.connector.display",
    configurationClass = ZoomConfiguration.class)
public class ZoomConnector extends BaseFullAccessConnector<ZoomConfiguration> {

  public ZoomConnector() {
    super(ZoomConfiguration.class);
    setAuthenticator(
        (Authenticator)
            new OAuth2TokenClientCredentialsAuthenticator() {
              @Override
              public String getGrantType() {
                return "account_credentials";
              }

              @Override
              public Map<String, String> getAdditionalFormFields() {
                return Collections.singletonMap("account_id", configuration.getAccountId());
              }
            });
    setDriver(new ZoomDriver());
    setAdapters(new ZoomUsersAdapter(), new ZoomGroupsAdapter());
  }
}
