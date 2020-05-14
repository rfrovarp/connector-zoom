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

import com.exclamationlabs.connid.zoom.client.UpdateGroupClient;
import com.exclamationlabs.connid.zoom.model.Group;
import com.exclamationlabs.connid.zoom.util.ZoomAbstractClientTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class UpdateGroupClientTest extends ZoomAbstractClientTest {

    @Test
    public void execute() throws IOException {
        final String ITEM_ID = "797979";
        prepareMockResponseEmpty();

        UpdateGroupClient client = new UpdateGroupClient(connection);
        Group updateGroup = new Group();
        updateGroup.setId(ITEM_ID);
        updateGroup.setName("Hello");
        String newId = client.execute(updateGroup);

        assertNotNull(newId);
        assertEquals(ITEM_ID, newId);

    }
}
