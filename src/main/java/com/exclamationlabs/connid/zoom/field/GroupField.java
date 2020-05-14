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

package com.exclamationlabs.connid.zoom.field;

import org.identityconnectors.framework.common.objects.AttributeInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum GroupField {
    GROUP_ID(String.class, AttributeInfo.Flags.NOT_UPDATEABLE),
    GROUP_NAME(String.class, AttributeInfo.Flags.REQUIRED),
    TOTAL_MEMBERS(Integer.class, AttributeInfo.Flags.NOT_UPDATEABLE);

    private Class<?> dataType;
    private Set<AttributeInfo.Flags> dataFlags;

    GroupField() {
        dataType = String.class;
        dataFlags = new HashSet<>();
    }

    GroupField(Class typeClass, AttributeInfo.Flags... flags) {
        dataType = typeClass;
        dataFlags = new HashSet<>();
        dataFlags.addAll(Arrays.asList(flags));
    }

    public Class getDataType() {
        return dataType;
    }

    public Set<AttributeInfo.Flags> getDataFlags() {
        return dataFlags;
    }
}
