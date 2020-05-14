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
import com.exclamationlabs.connid.zoom.model.User;
import com.exclamationlabs.connid.zoom.model.UserType;
import com.exclamationlabs.connid.zoom.util.TestZoomConfiguration;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserCRUDTest {

    private static final String FIXED_GROUP_ID = "loiFdqtuR4WoCq2Rn3G8uw"; // fixed test data - Avengers


    private ZoomConnector connector;

    private static String generatedUserId;

    @Before
    public void setup() {
        connector = new ZoomConnector();
        connector.init(new TestZoomConfiguration());
    }

    @Test
    public void test10CrudCreateUser() {
        CreateUserClient client = new CreateUserClient(connector.getConnection());
        User user = new User();
        user.setEmail("captain@america.com");
        user.setFirstName("Captain");
        user.setLastName("America");
        user.setType(UserType.BASIC);
        generatedUserId = client.execute(user);
        assertNotNull(generatedUserId);
    }

    @Test(expected = InvalidAttributeValueException.class)
    public void test10CrudCreateUserBadEmail() {
        CreateUserClient client = new CreateUserClient(connector.getConnection());
        User user = new User();
        user.setEmail("bogus");
        user.setFirstName("Clint");
        user.setLastName("Barton");
        user.setType(UserType.BASIC);
        generatedUserId = client.execute(user);
        assertNotNull(generatedUserId);
    }

    @Test(expected = InvalidAttributeValueException.class)
    public void test10CrudCreateUserMissingUserType() {
        CreateUserClient client = new CreateUserClient(connector.getConnection());
        User user = new User();
        user.setEmail("hawkeye@avengers.com");
        user.setFirstName("Clint");
        user.setLastName("Barton");
        generatedUserId = client.execute(user);
        assertNotNull(generatedUserId);
    }

    @Test
    public void test20CrudReadSingleUser() {

        GetUserClient client = new GetUserClient(connector.getConnection());
        User result = client.execute(generatedUserId);
        assertNotNull(result);
        // Note: If a user's status is pending, only `id` and `created_at` fields will be returned. The value of `created_at` will be the time at which the API call was made until the user activates their account.

        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
    }


    @Test
    public void test21CrudReadUsers() {
        ListUsersClient client = new ListUsersClient(connector.getConnection());
        List<User> userList = client.execute();

        assertNotNull(userList);
        assertFalse(userList.isEmpty());
    }


    @Test
    public void test30CrudUpdate() {
        UpdateUserClient client = new UpdateUserClient(connector.getConnection());

        User updateUser = new User();
        updateUser.setId(generatedUserId);
        updateUser.setLastName("America II");

        client.execute(updateUser);
    }

    // Cannot automatically test this since only email-validated users can really be added to groups
    @Test
    @Ignore
     public void test31AddGroupToUser() {
         AddGroupToUserClient client = new AddGroupToUserClient(connector.getConnection());
         client.execute(FIXED_GROUP_ID, generatedUserId);
     }

     // Cannot automatically test this since only email-validated users can really be added to groups
     @Test
     @Ignore
     public void test32RemoveGroupFromUser() {
         RemoveGroupFromUserClient client = new RemoveGroupFromUserClient(connector.getConnection());
         client.execute(FIXED_GROUP_ID, generatedUserId);
     }

    @Test
    public void test40CrudDelete() {
        DeleteUserClient client = new DeleteUserClient(connector.getConnection());
        client.execute(generatedUserId);
    }

}
