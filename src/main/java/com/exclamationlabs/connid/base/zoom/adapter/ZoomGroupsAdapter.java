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
import static com.exclamationlabs.connid.base.zoom.attribute.ZoomGroupAttribute.*;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_UPDATEABLE;

import com.exclamationlabs.connid.base.connector.adapter.AdapterValueTypeConverter;
import com.exclamationlabs.connid.base.connector.adapter.BaseAdapter;
import com.exclamationlabs.connid.base.connector.attribute.ConnectorAttribute;
import com.exclamationlabs.connid.base.zoom.configuration.ZoomConfiguration;
import com.exclamationlabs.connid.base.zoom.model.ZoomGroup;
import java.util.HashSet;
import java.util.Set;
import org.identityconnectors.framework.common.objects.*;

public class ZoomGroupsAdapter extends BaseAdapter<ZoomGroup, ZoomConfiguration> {

  @Override
  public ObjectClass getType() {
    return ObjectClass.GROUP;
  }

  @Override
  public Class<ZoomGroup> getIdentityModelClass() {
    return ZoomGroup.class;
  }

  @Override
  public Set<ConnectorAttribute> getConnectorAttributes() {
    Set<ConnectorAttribute> result = new HashSet<>();
    result.add(new ConnectorAttribute(Uid.NAME, GROUP_ID.name(), STRING, NOT_UPDATEABLE));
    result.add(new ConnectorAttribute(Name.NAME, GROUP_NAME.name(), STRING, NOT_UPDATEABLE));
    result.add(new ConnectorAttribute(TOTAL_MEMBERS.name(), INTEGER, NOT_UPDATEABLE));
    return result;
  }

  @Override
  protected ZoomGroup constructModel(
      Set<Attribute> attributes,
      Set<Attribute> multiValueAdded,
      Set<Attribute> multiValueRemoved,
      boolean creation) {
    ZoomGroup group = new ZoomGroup();
    group.setId(AdapterValueTypeConverter.getIdentityIdAttributeValue(attributes));
    group.setName(
        AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, GROUP_NAME));
    group.setTotalMembers(
        AdapterValueTypeConverter.getSingleAttributeValue(
            Integer.class, attributes, TOTAL_MEMBERS));
    return group;
  }

  @Override
  protected Set<Attribute> constructAttributes(ZoomGroup group) {
    Set<Attribute> attributes = new HashSet<>();
    attributes.add(AttributeBuilder.build(TOTAL_MEMBERS.name(), group.getTotalMembers()));

    return attributes;
  }
}
