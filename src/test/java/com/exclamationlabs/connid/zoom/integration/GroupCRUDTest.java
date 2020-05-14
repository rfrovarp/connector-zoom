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

package com.exclamationlabs.connid.zoom.integration;

import com.exclamationlabs.connid.zoom.ZoomConnector;
import com.exclamationlabs.connid.zoom.client.*;
import com.exclamationlabs.connid.zoom.model.Group;
import com.exclamationlabs.connid.zoom.util.TestZoomConfiguration;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GroupCRUDTest {

    private ZoomConnector connector;

    private static String generatedGroupId;

    @Before
    public void setup() {
        connector = new ZoomConnector();
        connector.init(new TestZoomConfiguration());
    }


    @Test
    public void test10CrudCreateGroup() {
        CreateGroupClient client = new CreateGroupClient(connector.getConnection());
        Group group = new Group();
        group.setName("Alpha Flight");
        generatedGroupId = client.execute(group);
        assertNotNull(generatedGroupId);
    }

    @Test(expected = AlreadyExistsException.class)
    public void test11CrudCreateGroupAlreadyExists() {
        CreateGroupClient client = new CreateGroupClient(connector.getConnection());
        Group group = new Group();
        group.setName("Alpha Flight");
        client.execute(group);
    }

    @Test
    public void test20CrudReadSingleGroup() {
        GetGroupClient client = new GetGroupClient(connector.getConnection());
        Group result = client.execute(generatedGroupId);
        assertNotNull(result);
        assertNotNull(result.getName());
    }

    @Test
    public void test21CrudReadGroups() {
        ListGroupsClient client = new ListGroupsClient(connector.getConnection());
        List<Group> groupList = client.execute();

        assertNotNull(groupList);
        assertFalse(groupList.isEmpty());
    }

    @Test
    public void test30CrudUpdate() {
        UpdateGroupClient client = new UpdateGroupClient(connector.getConnection());

        Group group = new Group();
        group.setId(generatedGroupId);
        group.setName("Alpha Flight 2");

        String uidResponse = client.execute(group);
        assertNotNull(uidResponse);
        assertEquals(generatedGroupId, uidResponse);
    }

    @Test
    public void test41CrudDelete() {
        DeleteGroupClient client = new DeleteGroupClient(connector.getConnection());
        client.execute(generatedGroupId);
    }

}
