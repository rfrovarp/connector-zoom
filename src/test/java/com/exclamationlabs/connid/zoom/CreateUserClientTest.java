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

import com.exclamationlabs.connid.zoom.client.CreateUserClient;
import com.exclamationlabs.connid.zoom.model.User;
import com.exclamationlabs.connid.zoom.util.ZoomAbstractClientTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class CreateUserClientTest extends ZoomAbstractClientTest {

    @Test
    public void execute() throws IOException {
        final String responseData = "{\"id\":\"keGi76UxSBePr_kFhIaM2Q\",\"first_name\":\"Captain\",\"last_name\":\"America\",\"email\":\"captain@america.com\",\"type\":1}";
        prepareMockResponse(responseData);

        CreateUserClient client = new CreateUserClient(connection);
        User newUser = new User();
        newUser.setFirstName("Hello");
        newUser.setLastName("World");
        newUser.setEmail("hello@world.com");
        String newId = client.execute(newUser);

        assertNotNull(newId);

    }
}
