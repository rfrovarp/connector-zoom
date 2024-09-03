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

package com.exclamationlabs.connid.base.zoom.adapter;

import static com.exclamationlabs.connid.base.connector.attribute.ConnectorAttributeDataType.*;
import static com.exclamationlabs.connid.base.zoom.attribute.ZoomUserAttribute.*;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.*;

import com.exclamationlabs.connid.base.connector.adapter.AdapterValueTypeConverter;
import com.exclamationlabs.connid.base.connector.adapter.BaseAdapter;
import com.exclamationlabs.connid.base.connector.attribute.ConnectorAttribute;
import com.exclamationlabs.connid.base.zoom.configuration.ZoomConfiguration;
import com.exclamationlabs.connid.base.zoom.model.*;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.framework.common.objects.*;

public class ZoomUsersAdapter extends BaseAdapter<ZoomUser, ZoomConfiguration> {

  @Override
  public ObjectClass getType() {
    return ObjectClass.ACCOUNT;
  }

  @Override
  public Class<ZoomUser> getIdentityModelClass() {
    return ZoomUser.class;
  }

  @Override
  public Set<ConnectorAttribute> getConnectorAttributes() {
    Set<ConnectorAttribute> result = new HashSet<>();
    result.add(new ConnectorAttribute(Uid.NAME, USER_ID.name(), STRING, NOT_UPDATEABLE, REQUIRED));
    result.add(new ConnectorAttribute(FIRST_NAME.name(), STRING));
    result.add(new ConnectorAttribute(LAST_NAME.name(), STRING));
    result.add(new ConnectorAttribute(Name.NAME, EMAIL.name(), STRING, REQUIRED));

    result.add(new ConnectorAttribute(PASSWORD.name(), STRING, NOT_UPDATEABLE));
    result.add(new ConnectorAttribute(LANGUAGE.name(), STRING));
    result.add(new ConnectorAttribute(TIME_ZONE.name(), STRING));
    result.add(new ConnectorAttribute(PHONE_NUMBER.name(), STRING, NOT_CREATABLE));
    result.add(new ConnectorAttribute(PHONE_COUNTRY.name(), STRING));
    result.add(new ConnectorAttribute(STATUS.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
    result.add(new ConnectorAttribute(TYPE.name(), INTEGER, NOT_UPDATEABLE));
    result.add(new ConnectorAttribute(CREATED_AT.name(), STRING, NOT_UPDATEABLE));
    result.add(new ConnectorAttribute(LAST_LOGIN_TIME.name(), STRING, NOT_UPDATEABLE));
    result.add(new ConnectorAttribute(VERIFIED.name(), STRING, NOT_UPDATEABLE));
    result.add(new ConnectorAttribute(PERSONAL_MEETING_ID.name(), LONG, NOT_UPDATEABLE));

    result.add(new ConnectorAttribute(GROUP_IDS.name(), ASSIGNMENT_IDENTIFIER, MULTIVALUED));

    result.add(new ConnectorAttribute(ZOOM_PHONE_FEATURE.name(), BOOLEAN));
    result.add(new ConnectorAttribute(ZOOM_PHONE_STATUS.name(), STRING, NOT_CREATABLE));
    result.add(new ConnectorAttribute(EXTENSION_NUMBER.name(), STRING));
    result.add(
        new ConnectorAttribute(
            ZOOM_PHONE_NUMBERS.name(), STRING, MULTIVALUED, NOT_RETURNED_BY_DEFAULT));
    result.add(
        new ConnectorAttribute(
            ZOOM_PHONE_CALLING_PLANS.name(), INTEGER, MULTIVALUED, NOT_RETURNED_BY_DEFAULT));
    result.add(new ConnectorAttribute(SITE_ID.name(), STRING));
    result.add(new ConnectorAttribute(SITE_NAME.name(), STRING));
    result.add(new ConnectorAttribute(SITE_CODE.name(), INTEGER));
    result.add(new ConnectorAttribute(ZOOM_ONE_FEATURE_TYPE.name(), STRING, NOT_UPDATEABLE));
    // result.add(new ConnectorAttribute(SMS_ENABLED.name(), BOOLEAN));
    return result;
  }

  @Override
  protected ZoomUser constructModel(
      Set<Attribute> attributes,
      Set<Attribute> multiValueAdded,
      Set<Attribute> multiValueRemoved,
      boolean creation) {
    ZoomUser user = new ZoomUser();
    user.setId(AdapterValueTypeConverter.getIdentityIdAttributeValue(attributes));
    user.setFirstName(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, FIRST_NAME));
    user.setLastName(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, LAST_NAME));

    user.setEmail(AdapterValueTypeConverter.getIdentityNameAttributeValue(attributes));
    if (user.getEmail() == null || user.getEmail().isEmpty()) {
      user.setEmail(
          AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, EMAIL));
    }

    user.setTimezone(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, TIME_ZONE));

    user.setPassword(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, PASSWORD));
    user.setLanguage(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, LANGUAGE));
    user.setTimezone(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, TIME_ZONE));
    user.setPhoneNumber(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, PHONE_NUMBER));
    user.setPhoneCountry(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, PHONE_COUNTRY));
    Boolean status =
        AdapterValueTypeConverter.getSingleAttributeValue(Boolean.class, attributes, __ENABLE__);
    if (status != null) {
      user.setStatus(status ? "active" : "inactive");
    }

    user.setType(
        AdapterValueTypeConverter.getSingleAttributeValue(Integer.class, attributes, TYPE));
    user.setCreatedAt(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, CREATED_AT));
    user.setLastLoginTime(
        AdapterValueTypeConverter.getSingleAttributeValue(
            String.class, attributes, LAST_LOGIN_TIME));
    user.setVerified(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, VERIFIED));
    user.setPersonalMeetingId(
        AdapterValueTypeConverter.getSingleAttributeValue(
            Long.class, attributes, PERSONAL_MEETING_ID));

    user.setGroupsToRemove(readAssignments(multiValueRemoved, GROUP_IDS));
    user.setGroupsToAdd(readAssignments(multiValueAdded, GROUP_IDS));
    // Adding Zoom Phone feature.
    Boolean isZoomPhone =
        AdapterValueTypeConverter.getSingleAttributeValue(
            Boolean.class, attributes, ZOOM_PHONE_FEATURE);
    ZoomFeature feature = new ZoomFeature();
    feature.setZoomPhone(isZoomPhone);
    user.setFeature(feature);

    // Adding the Calling Plans
    user.setOutboundAdd(new ZoomPhoneUserProfile());
    user.setOutboundRemove(new ZoomPhoneUserProfile());
    user.setPhoneProfile(new ZoomPhoneUserProfile());
    Set<Integer> cpAdd = readAssignmentsAsInteger(multiValueAdded, ZOOM_PHONE_CALLING_PLANS);
    if (cpAdd != null) {
      user.getOutboundAdd().setPlans(cpAdd);
    }
    Set<Integer> cpRemove = readAssignmentsAsInteger(multiValueRemoved, ZOOM_PHONE_CALLING_PLANS);
    if (cpRemove != null) {
      user.getOutboundRemove().setPlans(cpRemove);
    }
    Set<Integer> cp = readAssignmentsAsInteger(attributes, ZOOM_PHONE_CALLING_PLANS);
    if (cp != null) {
      user.getPhoneProfile().setPlans(cp);
    }

    // Add Zoom Phone Numbers
    Set<String> phoneAdd = readAssignments(multiValueAdded, ZOOM_PHONE_NUMBERS);
    if (phoneAdd != null) {
      user.getOutboundAdd().setPhones(phoneAdd);
    }
    Set<String> phoneRemove = readAssignments(multiValueRemoved, ZOOM_PHONE_NUMBERS);
    if (phoneRemove != null) {
      user.getOutboundRemove().setPhones(phoneRemove);
    }
    Set<String> phoneNumbers = readAssignments(attributes, ZOOM_PHONE_NUMBERS);
    if (phoneNumbers != null) {
      user.getPhoneProfile().setPhones(phoneNumbers);
    }
    // Set Extension
    String extension =
        AdapterValueTypeConverter.getSingleAttributeValue(
            String.class, attributes, EXTENSION_NUMBER);
    user.getPhoneProfile().setExtension(extension);
    // Set Site Info
    user.setSite(new ZoomPhoneSite());
    user.getSite()
        .setId(
            AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, SITE_ID));
    user.getSite()
        .setCode(
            AdapterValueTypeConverter.getSingleAttributeValue(
                Integer.class, attributes, SITE_CODE));
    user.getSite()
        .setName(
            AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, SITE_NAME));
    return user;
  }

  @Override
  protected Set<Attribute> constructAttributes(ZoomUser user) {
    Set<Attribute> attributes = new HashSet<>();

    attributes.add(AttributeBuilder.build(FIRST_NAME.name(), user.getFirstName()));
    attributes.add(AttributeBuilder.build(LAST_NAME.name(), user.getLastName()));
    attributes.add(AttributeBuilder.build(LANGUAGE.name(), user.getLanguage()));
    attributes.add(AttributeBuilder.build(TIME_ZONE.name(), user.getTimezone()));

    attributes.add(AttributeBuilder.build(TYPE.name(), user.getType()));
    attributes.add(AttributeBuilder.build(PHONE_NUMBER.name(), user.getPhoneNumber()));
    attributes.add(AttributeBuilder.build(PHONE_COUNTRY.name(), user.getPhoneCountry()));
    attributes.add(AttributeBuilder.build(CREATED_AT.name(), user.getCreatedAt()));
    attributes.add(AttributeBuilder.build(LAST_LOGIN_TIME.name(), user.getLastLoginTime()));
    attributes.add(AttributeBuilder.build(VERIFIED.name(), user.getVerified()));
    attributes.add(AttributeBuilder.build(PERSONAL_MEETING_ID.name(), user.getPersonalMeetingId()));
    attributes.add(AttributeBuilder.build(GROUP_IDS.name(), user.getGroupIds()));
    attributes.add(AttributeBuilder.build(STATUS.name(), user.getStatus()));
    boolean administrativeStatus = (StringUtils.equalsIgnoreCase(user.getStatus(), "active"));
    attributes.add(AttributeBuilder.build(__ENABLE__.name(), administrativeStatus));
    if (user.getFeature() != null) {
      attributes.add(
          AttributeBuilder.build(ZOOM_PHONE_FEATURE.name(), user.getFeature().getZoomPhone()));
    }
    if (user.getPhoneProfile() != null) {
      Set<Integer> plans = null;
      if (user.getPhoneProfile().getCallingPlans() != null
          && user.getPhoneProfile().getCallingPlans().size() > 0) {
        plans = new HashSet<>();
        for (ZoomCallingPlan item : user.getPhoneProfile().getCallingPlans()) {
          plans.add(item.getType());
        }
      }
      attributes.add(AttributeBuilder.build(ZOOM_PHONE_CALLING_PLANS.name(), plans));

      Set<String> numbers = null;
      if (user.getPhoneProfile().getPhoneNumbers() != null
          && user.getPhoneProfile().getPhoneNumbers().size() > 0) {
        numbers = new HashSet<>();
        for (ZoomPhoneNumber item : user.getPhoneProfile().getPhoneNumbers()) {
          numbers.add(item.getNumber());
        }
      }
      attributes.add(AttributeBuilder.build(ZOOM_PHONE_NUMBERS.name(), numbers));

      attributes.add(
          AttributeBuilder.build(EXTENSION_NUMBER.name(), user.getPhoneProfile().getExtension()));
      attributes.add(AttributeBuilder.build(SITE_ID.name(), user.getPhoneProfile().getSiteId()));
    }

    if (user.getSite() != null) {
      attributes.add(AttributeBuilder.build(SITE_CODE.name(), user.getSite().getCode()));
      attributes.add(AttributeBuilder.build(SITE_NAME.name(), user.getSite().getName()));
    }

    return attributes;
  }
  /**
   * This utility method can be used within adapter constructModel() method in order to construct a
   * list of identifiers for assignment to another object type
   *
   * @param attributes Set of attributes from Midpoint
   * @param attributeName Enum value pertain to the list of object identifiers for another type.
   *     This enum type (as string) is expected to possibly be present in @param attributes.
   * @return List of string identifiers for another object type.
   */
  protected Set<Integer> readAssignmentsAsInteger(
      Set<Attribute> attributes, Enum<?> attributeName) {
    Set<?> data =
        AdapterValueTypeConverter.getMultipleAttributeValue(Set.class, attributes, attributeName);

    Set<Integer> ids = new HashSet<>();
    if (data != null) {
      for (Object item : data) {
        if (item != null) {
          if (item instanceof Integer) {
            ids.add((Integer) item);
          } else {
            ids.add(Integer.valueOf(item.toString()));
          }
        }
      }
    }

    if (!ids.isEmpty()) {
      return ids;
    } else {
      return null;
    }
  }
}
