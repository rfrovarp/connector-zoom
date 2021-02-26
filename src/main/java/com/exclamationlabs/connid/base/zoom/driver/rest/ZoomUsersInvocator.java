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

package com.exclamationlabs.connid.base.zoom.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.zoom.model.GroupMember;
import com.exclamationlabs.connid.base.zoom.model.ZoomUser;
import com.exclamationlabs.connid.base.zoom.model.UserCreationType;
import com.exclamationlabs.connid.base.zoom.model.request.GroupMembersRequest;
import com.exclamationlabs.connid.base.zoom.model.request.UserCreationRequest;
import com.exclamationlabs.connid.base.zoom.model.response.GroupMembersResponse;
import com.exclamationlabs.connid.base.zoom.model.response.ListUsersResponse;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.util.ArrayList;
import java.util.List;

public class ZoomUsersInvocator implements DriverInvocator<ZoomDriver, ZoomUser> {

    private static final Log LOG = Log.getLog(ZoomUsersInvocator.class);

    @Override
    public String create(ZoomDriver zoomDriver, ZoomUser zoomUser) throws ConnectorException {

        UserCreationRequest requestData = new
                UserCreationRequest(UserCreationType.CREATE.getZoomName(), zoomUser);

        ZoomUser newUser = zoomDriver.executePostRequest("/users",
                ZoomUser.class, requestData);

        if (newUser == null) {
            throw new ConnectorException("Response from user creation was invalid");
        }

        if (zoomUser.getGroupIds() != null && !zoomUser.getGroupIds().isEmpty()) {
            updateGroupAssignments(zoomDriver, newUser.getId(), zoomUser.getGroupIds(), false);
        }

        return newUser.getId();
    }

    @Override
    public void update(ZoomDriver zoomDriver, String userId, ZoomUser userModel) throws ConnectorException {

        zoomDriver.executePatchRequest("/users/" + userModel.getId(), null, userModel);

        if (userModel.getGroupIds() != null) {
            updateGroupAssignments(zoomDriver, userModel.getId(), userModel.getGroupIds(), true);
        }
    }

    @Override
    public void delete(ZoomDriver zoomDriver, String userId) throws ConnectorException {
        zoomDriver.executeDeleteRequest("/users/" + userId, null);
    }

    @Override
    public List<ZoomUser> getAll(ZoomDriver zoomDriver) throws ConnectorException {
        ListUsersResponse response = zoomDriver.executeGetRequest("/users", ListUsersResponse.class);
        return response.getUsers();
    }

    @Override
    public ZoomUser getOne(ZoomDriver zoomDriver, String userId) throws ConnectorException {
        return zoomDriver.executeGetRequest("/users/" + userId, ZoomUser.class);
    }

    private void updateGroupAssignments(ZoomDriver driver, String userId, List<String> updatedGroupIds, boolean userUpdate) {
        List<String> currentGroupIds = new ArrayList<>();
        if (userUpdate) {
            ZoomUser temp = getOne(driver, userId);
            currentGroupIds = temp.getGroupIds();
        }

        for (String groupId : currentGroupIds) {
            if (! updatedGroupIds.contains(groupId)) {
                // group was removed
                driver.executeDeleteRequest("/groups/" + groupId + "/members/" + userId, null);
                LOG.info("Successfully removed group id {0} from user id {1}", groupId, userId);
            }
        }

        for (String groupId : updatedGroupIds) {
            if (! currentGroupIds.contains(groupId)) {
                // new group was added
                addGroupToUser(driver, groupId, userId);
                LOG.info("Successfully added group id {0} to user id {1}", groupId, userId);
            }
        }

    }

    private void addGroupToUser(ZoomDriver driver, String groupId, String userId) {
        List<GroupMember> memberList = new ArrayList<>();
        memberList.add(new GroupMember(userId));
        GroupMembersRequest membersRequest = new GroupMembersRequest(memberList);

        GroupMembersResponse response = driver.executePostRequest(
                "/groups/" + groupId + "/members", GroupMembersResponse.class, membersRequest);
        if (response == null || response.getAddedAt() == null) {
            throw new ConnectorException("Unexpected response received while adding user to group");
        }
    }

}
