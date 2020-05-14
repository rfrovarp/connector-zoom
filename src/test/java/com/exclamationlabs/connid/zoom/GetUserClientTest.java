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

import com.exclamationlabs.connid.zoom.client.GetUserClient;
import com.exclamationlabs.connid.zoom.model.User;
import com.exclamationlabs.connid.zoom.util.ZoomAbstractClientTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class GetUserClientTest extends ZoomAbstractClientTest {

    @Test
    public void execute() throws IOException {
        String responseData = "{\"id\":\"ZpRAY4X9SEipRS9kS--Img\",\"first_name\":\"Alfred\",\"last_name\":\"Neuman\",\"email\":\"alfred@mad.com\",\"type\":2,\"pmi\":5825080948,\"timezone\":\"America/Chicago\",\"verified\":0,\"created_at\":\"2020-05-06T19:22:24Z\",\"last_login_time\":\"2020-05-10T19:37:29Z\",\"pic_url\":\"https://lh6.googleusercontent.com/-mboZtlAHsM4/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuclRl5BboLrsXCiJ9dRBBD1yEIG2ww/photo.jpg\",\"language\":\"en-US\",\"phone_number\":\"\",\"status\":\"active\"}";
        prepareMockResponse(responseData);

        GetUserClient client = new GetUserClient(connection);
        User user = client.execute("1234");

        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getFirstName());
        assertNotNull(user.getLastName());
        assertNotNull(user.getEmail());
    }
}
