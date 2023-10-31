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

/*
 NOTE: In Zoom, you can only see and update users that are active (email verification is
 complete).  For that reason, the testing here is split between creation and deletion of a 'pending' user,
 while the Update/Get functionality tests against an already existing user that is active.
*/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ZoomConnectorApiIntegrationTest
    extends ApiIntegrationTest<ZoomConfiguration, ZoomConnector> {

  private static String generatedUserId;
  private static String generatedGroupId;

  private static final String existingUserId = "redacted";
  private static final String existingGroupId = "redacted";

  private static final String newName = "Charms " + System.currentTimeMillis();

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
  public void test110UserCreate() {
    // Create a 'pending' user that will be deleted at the end
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(
        new AttributeBuilder()
            .setName(FIRST_NAME.name())
            .addValue("Captain " + UUID.randomUUID())
            .build());
    attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("America").build());
    attributes.add(new AttributeBuilder().setName(TYPE.name()).addValue(UserType.BASIC).build());
    attributes.add(
        new AttributeBuilder()
            .setName(EMAIL.name())
            .addValue("captain" + UUID.randomUUID() + "@america.com")
            .build());

    Uid newId =
        getConnectorFacade()
            .create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());
    assertNotNull(newId);
    assertNotNull(newId.getUidValue());
    generatedUserId = newId.getUidValue();
  }

  @Test
  @Order(112)
  public void test112CrudCreateUserBadEmail() {
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(new AttributeBuilder().setName(FIRST_NAME.name()).addValue("Captain").build());
    attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("America").build());
    attributes.add(new AttributeBuilder().setName(TYPE.name()).addValue(UserType.BASIC).build());
    attributes.add(new AttributeBuilder().setName(EMAIL.name()).addValue("bogus").build());
    assertThrows(
        InvalidAttributeValueException.class,
        () ->
            getConnectorFacade()
                .create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build()));
  }

  @Test
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
  @Order(120)
  public void test120UserModify() {
    // modify the existing user
    Set<AttributeDelta> attributes = new HashSet<>();
    attributes.add(
        new AttributeDeltaBuilder().setName(LAST_NAME.name()).addValueToReplace(newName).build());
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
  @Order(130)
  public void test130UsersGet() {
    results = new ArrayList<>();
    getConnectorFacade()
        .search(ObjectClass.ACCOUNT, null, handler, new OperationOptionsBuilder().build());
    assertTrue(results.size() >= 1);
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getUidValue()));
    assertTrue(StringUtils.isNotBlank(results.get(0).getName().getNameValue()));
  }

  @Test
  @Disabled // Connector no longer supports native pagination since multiple getAll requests are
  // needed
  @Order(131)
  public void test131UsersGetWithPaging() {
    results = new ArrayList<>();
    getConnectorFacade()
        .search(
            ObjectClass.ACCOUNT,
            null,
            handler,
            new OperationOptionsBuilder().setPageSize(3).setPagedResultsOffset(1).build());
    assertEquals(3, results.size());
    assertTrue(StringUtils.isNotBlank(results.get(0).getUid().getUidValue()));
    assertTrue(StringUtils.isNotBlank(results.get(0).getName().getNameValue()));
  }

  @Test
  @Order(140)
  public void test140UserGet() {
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
        StringUtils.startsWithIgnoreCase(
            "Jimmy",
            results.get(0).getAttributeByName(FIRST_NAME.name()).getValue().get(0).toString()));
    assertTrue(
        StringUtils.equalsIgnoreCase(
            newName,
            results.get(0).getAttributeByName(LAST_NAME.name()).getValue().get(0).toString()));
    assertTrue(
        StringUtils.equalsIgnoreCase(
            existingGroupId,
            results.get(0).getAttributeByName(GROUP_IDS.name()).getValue().get(0).toString()));
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
    assertTrue(
        StringUtils.startsWithIgnoreCase(
            "Jimmy",
            results.get(0).getAttributeByName(FIRST_NAME.name()).getValue().get(0).toString()));
    assertTrue(
        StringUtils.equalsIgnoreCase(
            newName,
            results.get(0).getAttributeByName(LAST_NAME.name()).getValue().get(0).toString()));
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
        StringUtils.startsWithIgnoreCase(
            "Jimmy",
            results.get(0).getAttributeByName(FIRST_NAME.name()).getValue().get(0).toString()));
    assertTrue(
        StringUtils.equalsIgnoreCase(
            newName,
            results.get(0).getAttributeByName(LAST_NAME.name()).getValue().get(0).toString()));
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
        StringUtils.startsWithIgnoreCase(
            "Jimmy",
            results.get(0).getAttributeByName(FIRST_NAME.name()).getValue().get(0).toString()));
    assertTrue(
        StringUtils.equalsIgnoreCase(
            newName,
            results.get(0).getAttributeByName(LAST_NAME.name()).getValue().get(0).toString()));
    assertTrue(
        StringUtils.equalsIgnoreCase(
            "active",
            results.get(0).getAttributeByName(STATUS.name()).getValue().get(0).toString()));
  }

  @Test
  @Order(210)
  public void test210GroupCreate() {
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(
        new AttributeBuilder().setName(GROUP_NAME.name()).addValue("Flinstones").build());
    generatedGroupId =
        getConnectorFacade()
            .create(ObjectClass.GROUP, attributes, new OperationOptionsBuilder().build())
            .getUidValue();
  }

  @Test
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
        new AttributeBuilder().setName(Uid.NAME).addValue(generatedGroupId).build();
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
  @Order(290)
  public void test290GroupDelete() {
    getConnectorFacade()
        .delete(
            ObjectClass.GROUP, new Uid(generatedGroupId), new OperationOptionsBuilder().build());
  }

  @Test
  @Order(390)
  public void test390UserDelete() {
    getConnectorFacade()
        .delete(
            ObjectClass.ACCOUNT, new Uid(generatedUserId), new OperationOptionsBuilder().build());
  }
}
