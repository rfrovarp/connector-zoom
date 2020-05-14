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

package com.exclamationlabs.connid.zoom;

import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ZoomConfiguration extends AbstractConfiguration {

    private static final Log LOG = Log.getLog(ZoomConfiguration.class);

    private final static String API_KEY = "exclamationlabs.connector.zoom.api.key";
    private final static String API_SECRET = "exclamationlabs.connector.zoom.api.secret";

    private final static String SERVICE_URL = "exclamationlabs.connector.zoom.service.url";

    private static Properties configurationProperties;

    private String configurationFilePath;


    @Override
    public void validate() {
        if (StringUtils.isBlank(configurationFilePath)) {
            throw new ConfigurationException("Configuration path not given for Connector.");
        }
    }

    @ConfigurationProperty(
            displayMessageKey = "Zoom Configuration File Path",
            helpMessageKey = "File path for the Zoom Configuration File",
            required = true)
    public String getConfigurationFilePath() {
        return configurationFilePath;
    }

    @SuppressWarnings("unused")
    public void setConfigurationFilePath(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    @Override
    public String toString() {
        return String.format(
                "ZoomConfiguration{configurationFilePath='%s'}",
                configurationFilePath);
    }

    public void configureProperties() throws ConfigurationException {
        LOG.info("*** Properties path: " + getConfigurationFilePath());
        Properties configurationProperties = new Properties();
        try {
            configurationProperties.load(new FileReader(getConfigurationFilePath()));
            validateProperties(configurationProperties);
        } catch (IOException ex) {
            LOG.error(ex, "Error reading Config File at {0}", getConfigurationFilePath());
            throw new ConfigurationException("Failed to read Config File", ex);
        }
        LOG.info("*** Properties validated");
    }

    private static void validateProperties(Properties input) throws ConfigurationException {
        if (input==null) {
            throw new ConfigurationException("Configuration could not read. Properties is null.");
        }
        configurationProperties = input;

        String[] propertyArray = new String[] {
                API_KEY, API_SECRET, SERVICE_URL
        };

        for (String currentProperty: propertyArray) {
            if (configurationProperties.getProperty(currentProperty) == null) {
                throw new ConfigurationException("Missing configuration property: " + currentProperty);
            }
        }

    }


    String getServiceUrl() { return configurationProperties.getProperty(SERVICE_URL); }

    public String getApiKey() { return configurationProperties.getProperty(API_KEY); }

    public String getApiSecret() { return configurationProperties.getProperty(API_SECRET); }

    void setConfigurationProperties(Properties testProperties) {
        configurationProperties = testProperties;
    }
}
