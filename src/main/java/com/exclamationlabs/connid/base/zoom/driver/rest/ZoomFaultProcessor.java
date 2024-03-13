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

import static com.exclamationlabs.connid.base.zoom.model.response.fault.ErrorResponseCode.*;

import com.exclamationlabs.connid.base.connector.driver.exception.DriverRenewableTokenExpiredException;
import com.exclamationlabs.connid.base.connector.driver.rest.RestFaultProcessor;
import com.exclamationlabs.connid.base.connector.logging.Logger;
import com.exclamationlabs.connid.base.zoom.model.response.fault.ErrorResponse;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;

public class ZoomFaultProcessor implements RestFaultProcessor {

  private static final ZoomFaultProcessor instance = new ZoomFaultProcessor();

  public static ZoomFaultProcessor getInstance() {
    return instance;
  }

  public void process(HttpResponse httpResponse, GsonBuilder gsonBuilder) {
    String rawResponse;
    try {
      rawResponse = EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
      Logger.info(this, String.format("Raw Fault response %s", rawResponse));

      Header responseType = httpResponse.getFirstHeader("Content-Type");
      String responseTypeValue = responseType.getValue();
      if (!StringUtils.contains(responseTypeValue, ContentType.APPLICATION_JSON.getMimeType())) {
        // received non-JSON error response from Zoom unable to process
        String errorMessage = "Unable to parse Zoom response, not valid JSON: ";
        Logger.info(this, String.format("%s %s", errorMessage, rawResponse));
        throw new ConnectorException(errorMessage + rawResponse);
      }

      handleFaultResponse(rawResponse, gsonBuilder);

    } catch (IOException e) {
      throw new ConnectorException(
          "Unable to read fault response from Zoom response. "
              + "Status: "
              + httpResponse.getStatusLine().getStatusCode()
              + ", "
              + httpResponse.getStatusLine().getReasonPhrase(),
          e);
    }
  }

  private void handleFaultResponse(String rawResponse, GsonBuilder gsonBuilder) {
    ErrorResponse faultData = gsonBuilder.create().fromJson(rawResponse, ErrorResponse.class);
    if (faultData != null) {
      if (faultData.getCode() != null) {
        if (checkRecognizedFaultCodes(faultData)) {
          // other fault condition
          throw new ConnectorException(
              "Unknown fault received from Zoom.  Code: "
                  + faultData.getCode()
                  + "; Message: "
                  + faultData.getMessage());
        } else {
          // Logger.warn(this, faultData.getMessage());
          return;
        }
      }
    }
    throw new ConnectorException(
        "Unknown fault received from Zoom. Raw response JSON: " + rawResponse);
  }

  private Boolean checkRecognizedFaultCodes(ErrorResponse faultData) {
    switch (faultData.getCode()) {
      case PAID_SUBSCRIPTION_REQUIRED:
        throw new PaidAccountRequiredException(faultData.getMessage());

      case USER_NOT_FOUND:
      case GROUP_NOT_FOUND:
        // ignore fault and return to Midpoint
        return false;

      case GROUP_NAME_ALREADY_EXISTS:
        throw new AlreadyExistsException(
            "Supplied User/Group already exists. Please enter different input.");
      case USER_ALREADY_EXISTS:
        return false;

      case VALIDATION_FAILED:
        throw new InvalidAttributeValueException(
            "Validation Failed. " + faultData.getErrorDetails());
      case TOKEN_EXPIRED:
        throw new DriverRenewableTokenExpiredException(
            String.valueOf(faultData.getCode()) + " " + faultData.getMessage());
    }
    return true;
  }

  private Boolean checkRecognizedFaultMessages(ErrorResponse faultData) {
    if (faultData != null
        && faultData.getMessage() != null
        && (!faultData.getMessage().isEmpty())) {
      String message = faultData.getMessage();
      if (message.contains("User does not exist")) {
        Logger.info(this, message);
        return true;
      } else {
        Logger.error(this, message);
        throw new ConnectorException(
            "Unhandled Exception Received from Zoom.  Message: " + faultData.getMessage());
      }
    }
    return false;
  }
}
