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

import com.exclamationlabs.connid.base.connector.configuration.ConfigurationNameBuilder;
import com.exclamationlabs.connid.base.connector.test.IntegrationTest;
import com.exclamationlabs.connid.base.connector.test.util.ConnectorTestUtils;
import com.exclamationlabs.connid.base.zoom.configuration.ZoomConfiguration;
import com.exclamationlabs.connid.base.zoom.model.UserType;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.exclamationlabs.connid.base.zoom.attribute.ZoomGroupAttribute.*;
import static com.exclamationlabs.connid.base.zoom.attribute.ZoomUserAttribute.*;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ZoomConnectorIntegrationTest extends IntegrationTest {

    private ZoomConnector connector;

    private static String generatedUserId;
    private static String generatedGroupId;

    @Override
    public String getConfigurationName() {
        return new ConfigurationNameBuilder().withConnector(() -> "ZOOM").build();
    }

    @Before
    public void setup() {
        connector = new ZoomConnector();
        setup(connector, new ZoomConfiguration(getConfigurationName()));
    }

    @Test
    public void test100Test() {
        connector.test();
    }


    @Test
    public void test110UserCreate() {

        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(FIRST_NAME.name()).addValue("Captain").build());
        attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("America").build());
        attributes.add(new AttributeBuilder().setName(TYPE.name()).addValue(UserType.BASIC).build());
        attributes.add(new AttributeBuilder().setName(EMAIL.name()).addValue("captain@america.com").build());

        Uid newId = connector.create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
        generatedUserId = newId.getUidValue();
    }

    @Ignore // NOTE: Test not working because Zoom is improperly returning XML error response, not JSON response.  This was not
    // happening before
    @Test(expected = InvalidAttributeValueException.class)
    public void test112CrudCreateUserBadEmail() {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(FIRST_NAME.name()).addValue("Captain").build());
        attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("America").build());
        attributes.add(new AttributeBuilder().setName(TYPE.name()).addValue(UserType.BASIC).build());
        attributes.add(new AttributeBuilder().setName(EMAIL.name()).addValue("bogus").build());
        connector.create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());
    }

    @Ignore // NOTE: Test not working because Zoom is improperly returning XML error response, not JSON response.  This was not
    // happening before
    @Test(expected = InvalidAttributeValueException.class)
    public void test113CrudCreateUserMissingUserType() {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(FIRST_NAME.name()).addValue("Clint").build());
        attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("Barton").build());
        attributes.add(new AttributeBuilder().setName(EMAIL.name()).addValue("hawkeye@avengers.com").build());
        connector.create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());
    }

    @Test
    public void test120UserModify() {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(LAST_NAME.name()).addValue("America2").build());

        Uid newId = connector.update(ObjectClass.ACCOUNT, new Uid(generatedUserId), attributes, new OperationOptionsBuilder().build());
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
    }

    @Test
    public void test130UsersGet() {
        List<String> idValues = new ArrayList<>();
        List<String> nameValues = new ArrayList<>();
        ResultsHandler resultsHandler = ConnectorTestUtils.buildResultsHandler(idValues, nameValues);

        connector.executeQuery(ObjectClass.ACCOUNT, "", resultsHandler, new OperationOptionsBuilder().build());
        assertTrue(idValues.size() >= 1);
        assertTrue(StringUtils.isNotBlank(idValues.get(0)));
        assertTrue(StringUtils.isNotBlank(nameValues.get(0)));
    }


    @Test
    public void test140UserGet() {
        List<String> idValues = new ArrayList<>();
        List<String> nameValues = new ArrayList<>();
        ResultsHandler resultsHandler = ConnectorTestUtils.buildResultsHandler(idValues, nameValues);

        connector.executeQuery(ObjectClass.ACCOUNT, generatedUserId, resultsHandler, new OperationOptionsBuilder().build());
        assertEquals(1, idValues.size());
        assertTrue(StringUtils.isNotBlank(idValues.get(0)));
    }


    @Test
    public void test210GroupCreate() {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GROUP_NAME.name()).addValue("Flinstones").build());
        generatedGroupId = connector.create(ObjectClass.GROUP, attributes, new OperationOptionsBuilder().build()).getUidValue();
    }

    @Test
    public void test220GroupModify() {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GROUP_NAME.name()).addValue("Flinstones2").build());

        connector.update(ObjectClass.GROUP, new Uid(generatedGroupId), attributes, new OperationOptionsBuilder().build());
    }


    @Test
    public void test230GroupsGet() {
        List<String> idValues = new ArrayList<>();
        List<String> nameValues = new ArrayList<>();
        ResultsHandler resultsHandler = ConnectorTestUtils.buildResultsHandler(idValues, nameValues);

        connector.executeQuery(ObjectClass.GROUP, "", resultsHandler, new OperationOptionsBuilder().build());
        assertTrue(idValues.size() >= 1);
        assertTrue(StringUtils.isNotBlank(idValues.get(0)));
        assertTrue(StringUtils.isNotBlank(nameValues.get(0)));

   }

    @Test
    public void test240GroupGet() {
        List<String> idValues = new ArrayList<>();
        List<String> nameValues = new ArrayList<>();
        ResultsHandler resultsHandler = ConnectorTestUtils.buildResultsHandler(idValues, nameValues);

        connector.executeQuery(ObjectClass.GROUP, generatedGroupId, resultsHandler, new OperationOptionsBuilder().build());
    }

    @Test
    public void test290GroupDelete() {
        connector.delete(ObjectClass.GROUP, new Uid(generatedGroupId), new OperationOptionsBuilder().build());
    }

    @Test
    public void test390UserDelete() {
        connector.delete(ObjectClass.ACCOUNT, new Uid(generatedUserId), new OperationOptionsBuilder().build());
    }



}