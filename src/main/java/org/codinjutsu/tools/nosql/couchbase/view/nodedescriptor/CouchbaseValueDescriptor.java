/*
 * Copyright (c) 2013 David Boissier
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

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.nosql.commons.style.StyleAttributesProvider;
import org.codinjutsu.tools.nosql.commons.utils.StringUtils;
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;

public class CouchbaseValueDescriptor implements NodeDescriptor {

    private final int index;
    protected Object value;
    private final SimpleTextAttributes valueTextAttributes;

    public static CouchbaseValueDescriptor createDescriptor(int index, Object value) {
        if (value instanceof String) {
            return new CouchbaseStringValueDescriptor(index, (String) value);
        }
        return new CouchbaseValueDescriptor(index, value, StyleAttributesProvider.getStringAttribute());

    }

    private CouchbaseValueDescriptor(int index, Object value, SimpleTextAttributes valueTextAttributes) {
        this.index = index;
        this.value = value;
        this.valueTextAttributes = valueTextAttributes;
    }

    public void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {
        if (!isNodeExpanded) {
            cellRenderer.append(getFormattedValue(), valueTextAttributes);
        }
    }

    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.append(getFormattedKey(), StyleAttributesProvider.getIndexAttribute());
    }

    public String getFormattedKey() {
        return String.format("[%s]", index);
    }

    public String getFormattedValue() {
        return String.format("%s", getValueAndAbbreviateIfNecessary());
    }

    protected String getValueAndAbbreviateIfNecessary() {
        String stringifiedValue = value.toString();
        if (stringifiedValue.length() > MAX_LENGTH) {
            return StringUtils.abbreviateInCenter(stringifiedValue, MAX_LENGTH);
        }
        return stringifiedValue;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    private static class CouchbaseStringValueDescriptor extends CouchbaseValueDescriptor {

        private CouchbaseStringValueDescriptor(int index, String value) {
            super(index, value, StyleAttributesProvider.getStringAttribute());
        }

        @Override
        public String getFormattedValue() {
            return String.format("\"%s\"", getValueAndAbbreviateIfNecessary());
        }
    }
}
