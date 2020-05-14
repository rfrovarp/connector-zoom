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

package com.exclamationlabs.connid.zoom.client;

import com.exclamationlabs.connid.zoom.ZoomConnection;
import com.exclamationlabs.connid.zoom.model.Group;
import org.apache.http.client.methods.HttpPatch;

public class UpdateGroupClient {

    private final ZoomConnection connection;

    public UpdateGroupClient(ZoomConnection input) {
        connection = input;
    }

    public String execute(Group group) {
        Group modifyGroup = new Group();
        // Cannot send key in update JSON, and name is the only field to update,
        // so create a new object w/ just the name set
        modifyGroup.setName(group.getName());

        HttpPatch request = connection.createPatchRequest("/groups/" + group.getId(), modifyGroup);
        connection.executeRequest(request, null);

        return group.getId();
    }
}
