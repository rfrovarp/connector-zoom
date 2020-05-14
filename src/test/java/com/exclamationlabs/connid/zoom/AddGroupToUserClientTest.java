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

import com.exclamationlabs.connid.zoom.client.AddGroupToUserClient;
import com.exclamationlabs.connid.zoom.util.ZoomAbstractClientTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class AddGroupToUserClientTest extends ZoomAbstractClientTest {

    @Test
    public void execute() throws IOException {
        String responseData = "{\"ids\":\"\",\"added_at\":\"2020-05-13T18:31:35Z\"}";
        prepareMockResponse(responseData);

        AddGroupToUserClient client = new AddGroupToUserClient(connection);
        client.execute("7890", "123");
    }
}
