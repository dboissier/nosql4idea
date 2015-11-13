/*
 * Copyright (c) 2015 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.nosql.couchbase.view.nodedescriptor;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.nosql.commons.style.StyleAttributesProvider;
import org.codinjutsu.tools.nosql.commons.utils.StringUtils;
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;

public class CouchbaseKeyValueDescriptor implements NodeDescriptor {

    protected static final String TO_STRING_TEMPLATE = "\"%s\" : %s";


    protected final String key;
    protected final Object value;

    private final SimpleTextAttributes valueTextAttributes;

    public static CouchbaseKeyValueDescriptor createDescriptor(String key, Object value) {
        if (value == null) {
            return new CouchbaseKeyNullValueDescriptor(key);
        }
        if (value instanceof String) {
            return new CouchbaseKeyStringValueDescriptor(key, (String) value);
        }


        SimpleTextAttributes textAttributes = StyleAttributesProvider.getStringAttribute();
        if (value instanceof Boolean) {
            textAttributes = StyleAttributesProvider.getBooleanAttribute();
        } else if (value instanceof Number) {
            textAttributes = StyleAttributesProvider.getNumberAttribute();
        } else if (value instanceof JsonObject || value instanceof JsonArray) {
            textAttributes = StyleAttributesProvider.getObjectAttribute();
        }
        return new CouchbaseKeyValueDescriptor(key, value, textAttributes);
    }

    private CouchbaseKeyValueDescriptor(String key, Object value, SimpleTextAttributes valueTextAttributes) {
        this.key = key;
        this.value = value;
        this.valueTextAttributes = valueTextAttributes;
    }


    public void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {
        if (!isNodeExpanded) {
            cellRenderer.append(getValueAndAbbreviateIfNecessary(), valueTextAttributes);
        }
    }

    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.append(getFormattedKey(), StyleAttributesProvider.getKeyValueAttribute());
    }

    public String getFormattedKey() {
        return key;
    }

    public String getFormattedValue() {
        return getValueAndAbbreviateIfNecessary();
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {

    }

    @Override
    public String toString() {
        return String.format(TO_STRING_TEMPLATE, key, value);
    }

    protected String getValueAndAbbreviateIfNecessary() {
        String stringifiedValue = value.toString();
        if (stringifiedValue.length() > MAX_LENGTH) {
            return StringUtils.abbreviateInCenter(stringifiedValue, MAX_LENGTH);
        }
        return stringifiedValue;
    }

    private static class CouchbaseKeyNullValueDescriptor extends CouchbaseKeyValueDescriptor {

        private CouchbaseKeyNullValueDescriptor(String key) {
            super(key, null, StyleAttributesProvider.getNullAttribute());
        }

        protected String getValueAndAbbreviateIfNecessary() {
            return "null";
        }
    }

    private static class CouchbaseKeyStringValueDescriptor extends CouchbaseKeyValueDescriptor {

        private static final String STRING_SURROUNDED = "\"%s\"";
        private static final String TO_STRING_FOR_STRING_VALUE_TEMPLATE = "\"%s\" : \"%s\"";

        private CouchbaseKeyStringValueDescriptor(String key, String value) {
            super(key, value, StyleAttributesProvider.getStringAttribute());
        }

        @Override
        protected String getValueAndAbbreviateIfNecessary() {
            return String.format(STRING_SURROUNDED, value);
        }

        @Override
        public String toString() {
            return String.format(TO_STRING_FOR_STRING_VALUE_TEMPLATE, key, value);
        }
    }

}
