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

import static com.exclamationlabs.connid.base.zoom.attribute.ZoomGroupAttribute.*;
import static com.exclamationlabs.connid.base.zoom.attribute.ZoomUserAttribute.*;
import static org.junit.jupiter.api.Assertions.*;

import com.exclamationlabs.connid.base.connector.test.util.ConnectorMockRestTest;
import com.exclamationlabs.connid.base.connector.test.util.ConnectorTestUtils;
import com.exclamationlabs.connid.base.zoom.configuration.ZoomConfiguration;
import com.exclamationlabs.connid.base.zoom.driver.rest.ZoomDriver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ZoomConnectorTest extends ConnectorMockRestTest {

  private ZoomConnector connector;

  @BeforeEach
  public void setup() {
    connector =
        new ZoomConnector() {
          @Override
          public void init(Configuration configuration) {
            setAuthenticator(null);
            setDriver(
                new ZoomDriver() {
                  @Override
                  protected HttpClient createClient() {
                    return stubClient;
                  }
                });
            super.init(configuration);
          }
        };
    ZoomConfiguration configuration = new ZoomConfiguration();
    configuration.setServiceUrl("test");
    configuration.setTokenUrl("test");
    configuration.setClientId("test1");
    configuration.setAccountId("1234");
    configuration.setClientSecret(new GuardedString("test2".toCharArray()));
    connector.init(configuration);
  }

  @Test
  public void test100Test() {
    final String responseData =
        "{\"id\":\"ZpRAY4X9SEipRS9kS--Img\",\"group_ids\":[\"5555\"],\"first_name\":\"Alfred\",\"last_name\":\"Neuman\",\"email\":\"alfred@mad.com\",\"type\":2,\"pmi\":5825080948,\"timezone\":\"America/Chicago\",\"verified\":0,\"created_at\":\"2020-05-06T19:22:24Z\",\"last_login_time\":\"2020-05-10T19:37:29Z\",\"pic_url\":\"https://lh6.googleusercontent.com/-mboZtlAHsM4/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuclRl5BboLrsXCiJ9dRBBD1yEIG2ww/photo.jpg\",\"language\":\"en-US\",\"phone_number\":\"\",\"status\":\"active\"}";
    prepareMockResponse(responseData);
    connector.test();
  }

  @Test
  public void test110UserCreate() {
    final String responseData =
        "{\"id\":\"keGi76UxSBePr_kFhIaM2Q\",\"first_name\":\"Captain\",\"last_name\":\"America\",\"email\":\"captain@america.com\",\"type\":1}";
    final String addGroupResponseData = "{\"ids\":\"\",\"added_at\":\"2020-05-13T18:31:35Z\"}";
    prepareMockResponse(responseData, addGroupResponseData);

    Set<Attribute> attributes = new HashSet<>();
    attributes.add(
        new AttributeBuilder().setName(EMAIL.name()).addValue("test@tester.com").build());

    attributes.add(new AttributeBuilder().setName(FIRST_NAME.name()).addValue("John").build());
    attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("Doe").build());
    attributes.add(new AttributeBuilder().setName(GROUP_IDS.name()).addValue("5678").build());

    Uid newId =
        connector.create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());
    assertNotNull(newId);
    assertNotNull(newId.getUidValue());
  }

  @Test
  public void test120UserModify() {
    final String getUserResponseData =
        "{\"id\":\"ZpRAY4X9SEipRS9kS--Img\",\"group_ids\":[\"5555\"],\"first_name\":\"Alfred\",\"last_name\":\"Neuman\",\"email\":\"alfred@mad.com\",\"type\":2,\"pmi\":5825080948,\"timezone\":\"America/Chicago\",\"verified\":0,\"created_at\":\"2020-05-06T19:22:24Z\",\"last_login_time\":\"2020-05-10T19:37:29Z\",\"pic_url\":\"https://lh6.googleusercontent.com/-mboZtlAHsM4/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuclRl5BboLrsXCiJ9dRBBD1yEIG2ww/photo.jpg\",\"language\":\"en-US\",\"phone_number\":\"\",\"status\":\"active\"}";
    final String addGroupResponseData = "{\"ids\":\"\",\"added_at\":\"2020-05-13T18:31:35Z\"}";

    prepareMockResponse(getUserResponseData, addGroupResponseData);

    Set<AttributeDelta> attributes = new HashSet<>();
    attributes.add(
        new AttributeDeltaBuilder().setName(LAST_NAME.name()).addValueToReplace("Johnson").build());
    attributes.add(
        new AttributeDeltaBuilder().setName(USER_ID.name()).addValueToReplace("1234").build());

    attributes.add(
        new AttributeDeltaBuilder().setName(GROUP_IDS.name()).addValueToReplace("5678").build());

    Set<AttributeDelta> response =
        connector.updateDelta(
            ObjectClass.ACCOUNT,
            new Uid("1234"),
            attributes,
            new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  public void test130UsersGet() {
    final String responseData =
        "{\"page_count\":1,\"page_number\":1,\"page_size\":30,\"total_records\":1,\"users\":[{\"id\":\"ZpRAY4X9SEipRS9kS--Img\",\"first_name\":\"Alfred\",\"last_name\":\"Neuman\",\"email\":\"alfred@mad.com\",\"type\":2,\"pmi\":5825080948,\"timezone\":\"America/Chicago\",\"verified\":0,\"created_at\":\"2020-05-06T19:22:24Z\",\"last_login_time\":\"2020-05-10T19:37:29Z\",\"pic_url\":\"https://lh6.googleusercontent.com/-mboZtlAHsM4/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuclRl5BboLrsXCiJ9dRBBD1yEIG2ww/photo.jpg\",\"language\":\"en-US\",\"phone_number\":\"\",\"status\":\"active\"}]}";
    prepareMockResponse(responseData, responseData, responseData);

    List<String> idValues = new ArrayList<>();
    List<String> nameValues = new ArrayList<>();
    ResultsHandler resultsHandler = ConnectorTestUtils.buildResultsHandler(idValues, nameValues);

    connector.executeQuery(
        ObjectClass.ACCOUNT, "", resultsHandler, new OperationOptionsBuilder().build());
    assertTrue(idValues.size() >= 1);
    assertTrue(StringUtils.isNotBlank(idValues.get(0)));
    assertTrue(StringUtils.isNotBlank(nameValues.get(0)));
  }

  @Test
  public void test140UserGet() {
    final String responseData =
        "{\"id\":\"ZpRAY4X9SEipRS9kS--Img\",\"group_ids\":[\"5555\"],\"first_name\":\"Alfred\",\"last_name\":\"Neuman\",\"email\":\"alfred@mad.com\",\"type\":2,\"pmi\":5825080948,\"timezone\":\"America/Chicago\",\"verified\":0,\"created_at\":\"2020-05-06T19:22:24Z\",\"last_login_time\":\"2020-05-10T19:37:29Z\",\"pic_url\":\"https://lh6.googleusercontent.com/-mboZtlAHsM4/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuclRl5BboLrsXCiJ9dRBBD1yEIG2ww/photo.jpg\",\"language\":\"en-US\",\"phone_number\":\"\",\"status\":\"active\"}";
    prepareMockResponse(responseData);

    List<String> idValues = new ArrayList<>();
    List<String> nameValues = new ArrayList<>();
    ResultsHandler resultsHandler = ConnectorTestUtils.buildResultsHandler(idValues, nameValues);

    connector.executeQuery(
        ObjectClass.ACCOUNT, "1234", resultsHandler, new OperationOptionsBuilder().build());
    assertEquals(1, idValues.size());
    assertTrue(StringUtils.isNotBlank(idValues.get(0)));
  }

  @Test
  public void test210GroupCreate() {
    final String responseData =
        "{\"id\":\"yRU7LBa6RmenCOjsoEJkxw\",\"name\":\"Alpha Flight\",\"total_members\":0}";
    prepareMockResponse(responseData);
    Set<Attribute> attributes = new HashSet<>();

    attributes.add(
        new AttributeBuilder().setName(GROUP_NAME.name()).addValue("role name1").build());

    Uid newId =
        connector.create(ObjectClass.GROUP, attributes, new OperationOptionsBuilder().build());
    assertNotNull(newId);
    assertNotNull(newId.getUidValue());
  }

  @Test
  public void test220GroupModify() {
    prepareMockResponse();
    Set<AttributeDelta> attributes = new HashSet<>();
    attributes.add(
        new AttributeDeltaBuilder()
            .setName(GROUP_NAME.name())
            .addValueToReplace("role name2")
            .build());

    Set<AttributeDelta> response =
        connector.updateDelta(
            ObjectClass.GROUP, new Uid("1234"), attributes, new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  public void test230GroupsGet() {
    final String responseData =
        "{\"total_records\":3,\"groups\":[{\"id\":\"tAKM1nXqSSS4kgtNu91_uQ\",\"name\":\"Alpha Flight\",\"total_members\":0},{\"id\":\"loiFdqtuR4WoCq2Rn3G8uw\",\"name\":\"Avengers\",\"total_members\":0},{\"id\":\"nu7kJQ4PRwWrlyXoGHHopg\",\"name\":\"West Coast Avengers\",\"total_members\":0}]}";
    prepareMockResponse(responseData);
    List<String> idValues = new ArrayList<>();
    List<String> nameValues = new ArrayList<>();
    ResultsHandler resultsHandler = ConnectorTestUtils.buildResultsHandler(idValues, nameValues);

    connector.executeQuery(
        ObjectClass.GROUP, "", resultsHandler, new OperationOptionsBuilder().build());
    assertTrue(idValues.size() >= 1);
    assertTrue(StringUtils.isNotBlank(idValues.get(0)));
    assertTrue(StringUtils.isNotBlank(nameValues.get(0)));
  }

  @Test
  public void test240GroupGet() {
    final String responseData =
        "{\"id\":\"AxBcuvQeQSaP-Imhrludgw\",\"name\":\"Alpha Flight\",\"total_members\":0}";
    prepareMockResponse(responseData);
    List<String> idValues = new ArrayList<>();
    List<String> nameValues = new ArrayList<>();
    ResultsHandler resultsHandler = ConnectorTestUtils.buildResultsHandler(idValues, nameValues);

    connector.executeQuery(
        ObjectClass.GROUP, "1234", resultsHandler, new OperationOptionsBuilder().build());
    assertEquals(1, idValues.size());
    assertTrue(StringUtils.isNotBlank(idValues.get(0)));
    assertTrue(StringUtils.isNotBlank(nameValues.get(0)));
  }

  @Test
  public void test290GroupDelete() {
    prepareMockResponse();
    connector.delete(ObjectClass.GROUP, new Uid("1234"), new OperationOptionsBuilder().build());
  }

  @Test
  public void test390UserDelete() {
    prepareMockResponse();
    connector.delete(ObjectClass.ACCOUNT, new Uid("1234"), new OperationOptionsBuilder().build());
  }
}
