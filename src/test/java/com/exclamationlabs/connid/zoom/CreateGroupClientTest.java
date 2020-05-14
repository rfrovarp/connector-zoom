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

import com.exclamationlabs.connid.zoom.client.CreateGroupClient;
import com.exclamationlabs.connid.zoom.model.Group;
import com.exclamationlabs.connid.zoom.util.ZoomAbstractClientTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class CreateGroupClientTest extends ZoomAbstractClientTest {

    @Test
    public void execute() throws IOException {
        final String responseData = "{\"id\":\"yRU7LBa6RmenCOjsoEJkxw\",\"name\":\"Alpha Flight\",\"total_members\":0}";
        prepareMockResponse(responseData);

        CreateGroupClient client = new CreateGroupClient(connection);
        Group newGroup = new Group();
        newGroup.setName("Hello");
        String newId = client.execute(newGroup);

        assertNotNull(newId);
        assertEquals("yRU7LBa6RmenCOjsoEJkxw", newId);
    }
}
