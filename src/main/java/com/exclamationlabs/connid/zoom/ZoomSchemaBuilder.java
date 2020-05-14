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

import com.exclamationlabs.connid.zoom.field.GroupField;
import com.exclamationlabs.connid.zoom.field.UserField;
import org.identityconnectors.framework.common.objects.*;

class ZoomSchemaBuilder {


    private ZoomSchemaBuilder() {}

    static Schema build() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(ZoomConnector.class);

        schemaBuilder.defineObjectClass(getUserSchemaData());
        schemaBuilder.defineObjectClass(getGroupsSchemaData());

        return schemaBuilder.build();
    }

    private static ObjectClassInfo getUserSchemaData() {
        ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();
        builder.setType(ObjectClass.ACCOUNT_NAME);

        for (UserField current : UserField.values()) {
            AttributeInfoBuilder attributeInfo = new AttributeInfoBuilder(current.name());
            attributeInfo.setType(current.getDataType());
            attributeInfo.setFlags(current.getDataFlags());

            builder.addAttributeInfo(attributeInfo.build());
        }

        return builder.build();
    }

    private static ObjectClassInfo getGroupsSchemaData() {

        ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();
        builder.setType(ObjectClass.GROUP_NAME);
        for (GroupField current : GroupField.values()) {
            AttributeInfoBuilder attributeInfo = new AttributeInfoBuilder(current.name());
            attributeInfo.setType(current.getDataType());
            attributeInfo.setFlags(current.getDataFlags());

            builder.addAttributeInfo(attributeInfo.build());
        }
        return builder.build();
    }
}

