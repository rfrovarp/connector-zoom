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

package com.exclamationlabs.connid.zoom.adapter;

import com.exclamationlabs.connid.zoom.ZoomConnection;
import com.exclamationlabs.connid.zoom.client.*;
import com.exclamationlabs.connid.zoom.field.UserField;
import com.exclamationlabs.connid.zoom.model.User;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;

import java.util.*;
import java.util.stream.Collectors;

public class UsersAdapter implements AccessManagementAdapter {

    private static final Log LOG = Log.getLog(UsersAdapter.class);

    private ZoomConnection connection;


    public UsersAdapter(ZoomConnection in) {
        connection = in;
    }

    @Override
    public Uid create(Set<Attribute> attributes) {
        LOG.info("Enter Create Zoom user with supplied attributes {0}", attributes);
        CreateUserClient client = new CreateUserClient(connection);

        User user = new User();
        attributes.forEach(item -> map(user, item));
        String uidValue = client.execute(user);

        LOG.info("UID for new user is {0}", uidValue);

        performGroupUpdate(uidValue, attributes, false);
        return new Uid(uidValue);
    }

    @Override
    public Uid update(Uid uid, Set<Attribute> attributes) {
        LOG.info("Enter Update Zoom user with supplied attributes {0} for uid {1}",
                attributes, uid.getUidValue());
        User user = new User();
        user.setId(uid.getUidValue());
        attributes.forEach(item -> map(user, item));
        UpdateUserClient client = new UpdateUserClient(connection);
        client.execute(user);
        LOG.info("Updated user id {0}", uid.getUidValue());

        performGroupUpdate(uid.getUidValue(), attributes, true);
        return uid;
    }

    @Override
    public void delete(Uid uid) {
        LOG.info("Delete Zoom User {0}", uid.getUidValue());
        DeleteUserClient client = new DeleteUserClient(connection);
        client.execute(uid.getUidValue());
        LOG.info("Delete Zoom User {0} completed.", uid.getUidValue());
    }

    @Override
    public void get(String query, ResultsHandler resultsHandler) {
        if (queryAllRecords(query)) {
            LOG.info("Get All Zoom Users");
            ListUsersClient client = new ListUsersClient(connection);
            List<User> userList = client.execute();

            for (User current : userList) {
                resultsHandler.handle(
                        new ConnectorObjectBuilder()
                                .setUid(current.getId())
                                .setName(current.getEmail())
                                .setObjectClass(ObjectClass.ACCOUNT)
                                .build());
            }
        } else {
            // Query for single user
            LOG.info("Get user information for uid {0}", query);
            GetUserClient client = new GetUserClient(connection);
            User lookup = client.execute(query);
            if (lookup != null) {
                resultsHandler.handle(userToConnectorObject(lookup));
            }
        }
    }

    private void map(User user, Attribute attribute) {

        if (Name.NAME.equalsIgnoreCase(attribute.getName())) {
            user.setEmail(readAttributeValue(attribute).toString());
        } else if (Uid.NAME.equalsIgnoreCase(attribute.getName())) {
            user.setId(readAttributeValue(attribute).toString());
        } else {
            UserField field = UserField.valueOf(attribute.getName());

            switch (field) {
                case FIRST_NAME:
                    user.setFirstName(readAttributeValue(attribute).toString());
                    break;
                case LAST_NAME:
                    user.setLastName(readAttributeValue(attribute).toString());
                    break;
                case EMAIL:
                    user.setEmail(readAttributeValue(attribute).toString());
                    break;
                case LANGUAGE:
                    user.setLanguage(readAttributeValue(attribute).toString());
                    break;
                case TIME_ZONE:
                    user.setTimezone(readAttributeValue(attribute).toString());
                    break;
                case STATUS:
                    user.setStatus(readAttributeValue(attribute).toString());
                    break;
                case TYPE:
                    String typeValue = readAttributeValue(attribute).toString();
                    if (typeValue != null) {
                        user.setType(Integer.parseInt(readAttributeValue(attribute).toString()));
                    }
                    break;

                default:
                    LOG.info("Attribute " + attribute.getName() + " not recognized.");
                    break;
            }
        }
    }

    private static ConnectorObject userToConnectorObject(final User user) {

        final ConnectorObjectBuilder builder = new ConnectorObjectBuilder()
                .setObjectClass(ObjectClass.ACCOUNT)
                .setUid(user.getId())
                .setName(user.getEmail())
                .addAttribute(UserField.USER_ID.name(), user.getId())
                .addAttribute(UserField.FIRST_NAME.name(), user.getFirstName())
                .addAttribute(UserField.LAST_NAME.name(), user.getLastName())
                .addAttribute(UserField.EMAIL.name(), user.getEmail())
                .addAttribute(UserField.LANGUAGE.name(), user.getLanguage())
                .addAttribute(UserField.TIME_ZONE.name(), user.getTimezone())
                .addAttribute(UserField.STATUS.name(), user.getStatus())
                .addAttribute(UserField.TYPE.name(), user.getType())
                .addAttribute(UserField.PHONE_NUMBER.name(), user.getPhoneNumber())
                .addAttribute(UserField.CREATED_AT.name(), user.getCreatedAt())
                .addAttribute(UserField.LAST_LOGIN_TIME.name(), user.getLastLoginTime())
                .addAttribute(UserField.VERIFIED.name(), user.getVerified())
                .addAttribute(UserField.PERSONAL_MEETING_ID.name(), user.getPersonalMeetingId())
                .addAttribute(UserField.GROUP_IDS.name(), user.getGroupIds());

        return builder.build();
    }

    private void performGroupUpdate(String uidValue, Set<Attribute> attributes, boolean lookupCurrent) {
        List<String> currentGroupIds = new ArrayList<>();
        if (lookupCurrent) {
            GetUserClient client = new GetUserClient(connection);
            User lookup = client.execute(uidValue);
            currentGroupIds = lookup.getGroupIds();
        }

        LOG.info("Check for updated group ID's for user, current group ids {0}", currentGroupIds);

        Optional<Attribute> hasUpdatedGroupIds = attributes.stream().filter(current -> current.getName().equals(UserField.GROUP_IDS.name())).findFirst();
        if (hasUpdatedGroupIds.isPresent()) {
            List<String> updatedGroupIds;
            if (hasUpdatedGroupIds.get().getValue() != null) {
                updatedGroupIds = hasUpdatedGroupIds.get().getValue().stream().map(
                        Object::toString).collect(Collectors.toList());
            } else {
                updatedGroupIds = new ArrayList<>();
            }

            LOG.info("Updated group ids present: {0}", updatedGroupIds);
            updateGroupsForUser(uidValue, currentGroupIds, updatedGroupIds);
        }
    }

    private void updateGroupsForUser(String userId, List<String> currentGroupIds,
                                    List<String> updatedGroupIds) {
        RemoveGroupFromUserClient removeClient = new RemoveGroupFromUserClient(connection);
        AddGroupToUserClient addClient = new AddGroupToUserClient(connection);

        for (String groupId : currentGroupIds) {
            if (! updatedGroupIds.contains(groupId)) {
                // group was removed
                removeClient.execute(groupId, userId);
                LOG.info("Successfully removed group id {0} from user id {1}", groupId, userId);
            }
        }

        for (String groupId : updatedGroupIds) {
            if (! currentGroupIds.contains(groupId)) {
                // new group was added
                addClient.execute(groupId, userId);
                LOG.info("Successfully added group id {0} to user id {1}", groupId, userId);
            }
        }

    }

}
