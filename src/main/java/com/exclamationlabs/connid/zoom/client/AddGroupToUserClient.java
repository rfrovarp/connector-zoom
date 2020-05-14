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
import com.exclamationlabs.connid.zoom.model.GroupMember;
import com.exclamationlabs.connid.zoom.model.request.GroupMembersRequest;
import com.exclamationlabs.connid.zoom.model.response.GroupMembersResponse;
import org.apache.http.client.methods.HttpPost;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.util.ArrayList;
import java.util.List;

public class AddGroupToUserClient {

    private static final Log LOG = Log.getLog(AddGroupToUserClient.class);

    private final ZoomConnection connection;

    public AddGroupToUserClient(ZoomConnection input) {
        connection = input;
    }

    public void execute(String groupId, String userId) {
        LOG.info("add group id {0} to user id {1}", groupId, userId);
        List<GroupMember> memberList = new ArrayList<>();
        memberList.add(new GroupMember(userId));
        GroupMembersRequest membersRequest = new GroupMembersRequest(memberList);

        HttpPost request = connection.createPostRequest("/groups/" +
                groupId + "/members", membersRequest);
        GroupMembersResponse response = connection.executeRequest(request, GroupMembersResponse.class);
        if (response == null || response.getAddedAt() == null) {
            throw new ConnectorException("Unexpected response received while adding user to group");
        }

    }
}
