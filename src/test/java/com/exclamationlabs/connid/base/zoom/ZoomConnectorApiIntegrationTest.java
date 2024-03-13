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

import com.exclamationlabs.connid.base.connector.configuration.ConfigurationNameBuilder;
import com.exclamationlabs.connid.base.connector.configuration.ConfigurationReader;
import com.exclamationlabs.connid.base.connector.test.ApiIntegrationTest;
import com.exclamationlabs.connid.base.zoom.configuration.ZoomConfiguration;
import com.exclamationlabs.connid.base.zoom.model.UserType;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.junit.jupiter.api.*;

/**
 * NOTE: In Zoom, you can only update users that are active (email verification is complete). For
 * that reason, the testing here is split between creation and deletion of a 'pending' user, while
 * the Update/Get functionality tests against an already existing user that is active.
 *
 * <p>Replace strings that have value "redacted" with reasonable values from your test environment
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ZoomConnectorApiIntegrationTest
    extends ApiIntegrationTest<ZoomConfiguration, ZoomConnector> {

  private static String generatedUserId;
  private static String generatedGroupId;

  private static final String existingUserId = "redacted";
  private static final String phoneUserId1 = "redacted";
  private static final String phoneUserId1Email = "redacted";
  private static final String phoneUserId2 = "redacted";
  private static final String phoneUserId2Email = "redacted";
  private static final String existingPhone1Site1 = "redacted";
  private static final String existingPhone2Site1 = "redacted";
  private static final String existingPhone1Site2 = "redacted";
  private static final String existingPhone2Site2 = "redacted";
  private static final String existingGroupId = "redacted";
  private static final String generatedGroupName = "redacted";
  private static final String userEmail = "redacted";
  private static final String firstName = "Famous";
  private static final String lastName = "Martian";
  private static final String siteMain = "Main Site";
  private static final String siteCoral = "redacted";

  @Override
  protected ZoomConfiguration getConfiguration() {
    return new ZoomConfiguration(
        new ConfigurationNameBuilder().withConnector(() -> "ZOOM").build());
  }

  @Override
  protected Class<ZoomConnector> getConnectorClass() {
    return ZoomConnector.class;
  }

  @Override
  protected void readConfiguration(ZoomConfiguration zoomConfiguration) {
    ConfigurationReader.setupTestConfiguration(zoomConfiguration);
  }

  @BeforeEach
  public void setup() {
    super.setup();
  }

  @Test
  @Order(50)
  public void test050Test() {
    getConnectorFacade().test();
  }

  @Test
  @Order(60)
  public void test060Schema() {
    assertNotNull(getConnectorFacade().schema());
  }

  @Test
  @Order(100)
  public void test100UserCreate() {
    // Creates a 'pending' user that will be deleted at the end
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(new AttributeBuilder().setName(FIRST_NAME.name()).addValue(firstName).build());
    attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue(lastName).build());
    attributes.add(new AttributeBuilder().setName(TYPE.name()).addValue(UserType.BASIC).build());
    attributes.add(new AttributeBuilder().setName(EMAIL.name()).addValue(userEmail).build());
    Uid newId =
        getConnectorFacade()
            .create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());
    assertNotNull(newId);
    assertNotNull(newId.getUidValue());
    generatedUserId = newId.getUidValue();
  }

  @Test
  @Order(110)
  public void test110PhoneUserCreate() {
    // Creates a 'pending' user that will be deleted at the end
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(new AttributeBuilder().setName(FIRST_NAME.name()).addValue("John").build());
    attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("Johnson").build());
    attributes.add(new AttributeBuilder().setName(TYPE.name()).addValue(UserType.LICENSED).build());
    attributes.add(
        new AttributeBuilder().setName(ZOOM_PHONE_FEATURE.name()).addValue(true).build());
    attributes.add(
        new AttributeBuilder().setName(EMAIL.name()).addValue(phoneUserId1Email).build());
    Uid newId =
        getConnectorFacade()
            .create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());
    assertNotNull(newId);
    assertNotNull(newId.getUidValue());
    generatedUserId = newId.getUidValue();
  }

  @Test
  @Order(111)
  public void test110PhoneUserCreate2() {
    // Creates a 'pending' user that will be deleted at the end
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(new AttributeBuilder().setName(FIRST_NAME.name()).addValue("Jimmy").build());
    attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("Stuart").build());
    attributes.add(new AttributeBuilder().setName(TYPE.name()).addValue(UserType.BASIC).build());
    attributes.add(
        new AttributeBuilder().setName(ZOOM_PHONE_FEATURE.name()).addValue(true).build());
    attributes.add(
        new AttributeBuilder().setName(EMAIL.name()).addValue(phoneUserId2Email).build());

    Uid newId =
        getConnectorFacade()
            .create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());
    assertNotNull(newId);
    assertNotNull(newId.getUidValue());
  }

  @Test
  @Disabled
  @Order(113)
  public void test113CrudCreateUserMissingUserType() {
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(new AttributeBuilder().setName(FIRST_NAME.name()).addValue("Clint").build());
    attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("Barton").build());
    attributes.add(
        new AttributeBuilder().setName(EMAIL.name()).addValue("hawkeye@avengers.com").build());
    assertThrows(
        InvalidAttributeValueException.class,
        () ->
            getConnectorFacade()
                .create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build()));
  }

  @Test
  @Order(140)
  public void test115UserGet() {
    Attribute idAttribute =
        new AttributeBuilder().setName(Uid.NAME).addValue(existingUserId).build();

    results = new ArrayList<>();
    getConnectorFacade()
        .search(
            ObjectClass.ACCOUNT,
            new EqualsFilter(idAttribute),
            handler,
            new OperationOptionsBuilder().build());
    assertEquals(1, results.size());
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getValue().get(0).toString()));
  }

  @Test
  @Order(145)
  public void test116PhoneUserGet() {
    Attribute idAttribute = new AttributeBuilder().setName(Uid.NAME).addValue(phoneUserId1).build();

    results = new ArrayList<>();
    getConnectorFacade()
        .search(
            ObjectClass.ACCOUNT,
            new EqualsFilter(idAttribute),
            handler,
            new OperationOptionsBuilder().build());
    assertEquals(1, results.size());
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getValue().get(0).toString()));
  }

  @Test
  @Order(120)
  public void test120UserModify() {
    // modify the existing user
    Set<AttributeDelta> attributes = new HashSet<>();
    attributes.add(
        new AttributeDeltaBuilder().setName(LANGUAGE.name()).addValueToReplace("en-US").build());
    attributes.add(
        new AttributeDeltaBuilder().setName(TIME_ZONE.name()).addValueToReplace("UTC").build());
    attributes.add(
        new AttributeDeltaBuilder()
            .setName(GROUP_IDS.name())
            .addValueToAdd(existingGroupId)
            .build());
    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(existingUserId),
                attributes,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Order(121)
  public void test121PhoneUserModify() {
    // modify the existing user
    Set<AttributeDelta> delta = new HashSet<>();

    AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_CALLING_PLANS.name()).addValueToAdd(100);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_NUMBERS.name()).addValueToAdd(existingPhone1Site1);
    builder.setName(ZOOM_PHONE_NUMBERS.name()).addValueToAdd(existingPhone2Site1);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(EXTENSION_NUMBER.name()).addValueToReplace("1803");
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(SITE_NAME.name()).addValueToReplace(siteMain);
    delta.add(builder.build());

    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(phoneUserId1),
                delta,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Order(122)
  public void test122PhoneUserModifyRemoveNumbers() {
    // modify the existing user
    Set<AttributeDelta> delta = new HashSet<>();
    AttributeDeltaBuilder builder = new AttributeDeltaBuilder();

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_NUMBERS.name()).addValueToRemove(existingPhone2Site1);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(EXTENSION_NUMBER.name()).addValueToReplace("1803");
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(SITE_NAME.name()).addValueToReplace(siteMain);
    delta.add(builder.build());

    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(phoneUserId1),
                delta,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Order(122)
  public void test122PhoneUserFeatureEnable() {
    // modify the existing user
    Set<AttributeDelta> delta = new HashSet<>();
    AttributeDeltaBuilder builder = new AttributeDeltaBuilder();

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_FEATURE.name()).addValueToReplace(true);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(SITE_NAME.name()).addValueToReplace(siteMain);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(EXTENSION_NUMBER.name()).addValueToReplace("1809");
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_CALLING_PLANS.name()).addValueToAdd(100);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_NUMBERS.name()).addValueToAdd(existingPhone2Site1);
    delta.add(builder.build());

    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(phoneUserId2),
                delta,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Order(123)
  public void test123PhoneUserChangeSites() {
    // modify the existing user
    Set<AttributeDelta> delta = new HashSet<>();
    AttributeDeltaBuilder builder = new AttributeDeltaBuilder();

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_FEATURE.name()).addValueToReplace(true);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(SITE_NAME.name()).addValueToReplace(siteCoral);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_CALLING_PLANS.name()).addValueToAdd(100);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_NUMBERS.name()).addValueToRemove(existingPhone2Site1);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_NUMBERS.name()).addValueToAdd(existingPhone1Site2);
    delta.add(builder.build());
    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(phoneUserId2),
                delta,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Order(124)
  public void test124PhoneUserFeatureDisable() {
    // modify the existing user
    Set<AttributeDelta> delta = new HashSet<>();
    AttributeDeltaBuilder builder = new AttributeDeltaBuilder();

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_FEATURE.name()).addValueToReplace(false);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(SITE_NAME.name()).addValueToReplace(siteMain);
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(EXTENSION_NUMBER.name()).addValueToReplace("1809");
    delta.add(builder.build());

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_NUMBERS.name()).addValueToRemove(existingPhone2Site1);
    delta.add(builder.build());

    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(phoneUserId2),
                delta,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Order(125)
  public void test125PhoneUserFeatureEnable() {
    // modify the existing user
    Set<AttributeDelta> delta = new HashSet<>();
    AttributeDeltaBuilder builder = new AttributeDeltaBuilder();

    builder = new AttributeDeltaBuilder();
    builder.setName(ZOOM_PHONE_FEATURE.name()).addValueToReplace(true);
    delta.add(builder.build());

    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(phoneUserId2),
                delta,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Disabled
  @Order(130)
  public void test130UserList() {
    results = new ArrayList<>();
    getConnectorFacade()
        .search(ObjectClass.ACCOUNT, null, handler, new OperationOptionsBuilder().build());
    assertTrue(results.size() >= 1);
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getUidValue()));
    assertTrue(StringUtils.isNotBlank(results.get(0).getName().getNameValue()));
  }

  @Test
  @Order(131)
  public void test131UserListWithPaging() {
    results = new ArrayList<>();
    getConnectorFacade()
        .search(
            ObjectClass.ACCOUNT,
            null,
            handler,
            new OperationOptionsBuilder().setPageSize(9).setPagedResultsOffset(10).build());
    assertTrue(results.size() >= 1);
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getUidValue()));
    assertTrue(StringUtils.isNotBlank(results.get(0).getName().getNameValue()));
  }

  @Test
  @Order(150)
  public void test150UserRemoveGroup() {
    // modify the existing user
    Set<AttributeDelta> attributes = new HashSet<>();
    attributes.add(
        new AttributeDeltaBuilder()
            .setName(GROUP_IDS.name())
            .addValueToRemove(existingGroupId)
            .build());
    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(existingUserId),
                attributes,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Order(155)
  public void test155UserGetVerifyGroupRemoved() {
    Attribute idAttribute =
        new AttributeBuilder().setName(Uid.NAME).addValue(existingUserId).build();

    results = new ArrayList<>();
    getConnectorFacade()
        .search(
            ObjectClass.ACCOUNT,
            new EqualsFilter(idAttribute),
            handler,
            new OperationOptionsBuilder().build());
    assertEquals(1, results.size());
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getValue().get(0).toString()));
    assertTrue(results.get(0).getAttributeByName(GROUP_IDS.name()).getValue().isEmpty());
  }

  @Test
  @Order(160)
  public void test160UserModifyDisableUser() {
    // modify the existing user
    Set<AttributeDelta> attributes = new HashSet<>();

    attributes.add(
        new AttributeDeltaBuilder().setName(__ENABLE__.name()).addValueToReplace(false).build());
    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(existingUserId),
                attributes,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Order(162)
  public void test162UserGetVerifyUserDisabled() {
    Attribute idAttribute =
        new AttributeBuilder().setName(Uid.NAME).addValue(existingUserId).build();

    results = new ArrayList<>();
    getConnectorFacade()
        .search(
            ObjectClass.ACCOUNT,
            new EqualsFilter(idAttribute),
            handler,
            new OperationOptionsBuilder().build());
    assertEquals(1, results.size());
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getValue().get(0).toString()));
    assertTrue(
        StringUtils.equalsIgnoreCase(
            "inactive",
            results.get(0).getAttributeByName(STATUS.name()).getValue().get(0).toString()));
  }

  @Test
  @Order(164)
  public void test164UserModifyEnableUser() {
    // modify the existing user
    Set<AttributeDelta> attributes = new HashSet<>();

    attributes.add(
        new AttributeDeltaBuilder().setName(__ENABLE__.name()).addValueToReplace(true).build());
    Set<AttributeDelta> response =
        getConnectorFacade()
            .updateDelta(
                ObjectClass.ACCOUNT,
                new Uid(existingUserId),
                attributes,
                new OperationOptionsBuilder().build());
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  @Test
  @Order(166)
  public void test166UserGetVerifyUserEnabled() {
    Attribute idAttribute =
        new AttributeBuilder().setName(Uid.NAME).addValue(existingUserId).build();

    results = new ArrayList<>();
    getConnectorFacade()
        .search(
            ObjectClass.ACCOUNT,
            new EqualsFilter(idAttribute),
            handler,
            new OperationOptionsBuilder().build());
    assertEquals(1, results.size());
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getValue().get(0).toString()));
    assertTrue(
        StringUtils.equalsIgnoreCase(
            "active",
            results.get(0).getAttributeByName(STATUS.name()).getValue().get(0).toString()));
  }

  @Test
  @Disabled
  @Order(210)
  public void test210GroupCreate() {
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(
        new AttributeBuilder().setName(GROUP_NAME.name()).addValue(generatedGroupName).build());
    generatedGroupId =
        getConnectorFacade()
            .create(ObjectClass.GROUP, attributes, new OperationOptionsBuilder().build())
            .getUidValue();
  }

  @Test
  @Disabled
  @Order(220)
  public void test220GroupModify() {
    Set<AttributeDelta> attributes = new HashSet<>();
    attributes.add(
        new AttributeDeltaBuilder()
            .setName(GROUP_NAME.name())
            .addValueToReplace("Flinstones2")
            .build());

    getConnectorFacade()
        .updateDelta(
            ObjectClass.GROUP,
            new Uid(generatedGroupId),
            attributes,
            new OperationOptionsBuilder().build());
  }

  @Test
  @Order(230)
  public void test230GroupsGet() {
    results = new ArrayList<>();
    getConnectorFacade()
        .search(ObjectClass.GROUP, null, handler, new OperationOptionsBuilder().build());
    assertFalse(results.isEmpty());
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getValue().get(0).toString()));
    assertTrue(StringUtils.isNotBlank(results.get(0).getName().getValue().get(0).toString()));
  }

  @Test
  @Order(240)
  public void test240GroupGet() {
    Attribute idAttribute =
        new AttributeBuilder().setName(Uid.NAME).addValue(existingGroupId).build();
    results = new ArrayList<>();
    getConnectorFacade()
        .search(
            ObjectClass.GROUP,
            new EqualsFilter(idAttribute),
            handler,
            new OperationOptionsBuilder().build());
    assertFalse(results.isEmpty());
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getValue().get(0).toString()));
    assertTrue(StringUtils.isNotBlank(results.get(0).getName().getValue().get(0).toString()));
  }

  @Test
  @Disabled
  @Order(290)
  public void test290GroupDelete() {
    getConnectorFacade()
        .delete(
            ObjectClass.GROUP, new Uid(generatedGroupId), new OperationOptionsBuilder().build());
  }

  @Test
  @Disabled
  @Order(390)
  public void test390UserDelete() {
    getConnectorFacade()
        .delete(
            ObjectClass.ACCOUNT, new Uid(generatedUserId), new OperationOptionsBuilder().build());
  }
}
