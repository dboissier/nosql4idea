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

package org.codinjutsu.tools.nosql.redis.view.nodedescriptor;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.commons.style.StyleAttributesProvider;
import redis.clients.jedis.Tuple;

public class RedisValueDescriptor implements NodeDescriptor {

    private final int index;
    protected Object value;
    private final SimpleTextAttributes valueTextAttributes;

    public static RedisValueDescriptor createDescriptor(int index, Object value) {
        return new RedisValueDescriptor(index, value, StyleAttributesProvider.getStringAttribute());
    }
    public static RedisValueDescriptor createUnindexedDescriptor(Object value) {
        return new RedisUnindexedValueDescriptor(value, StyleAttributesProvider.getStringAttribute());
    }

    private RedisValueDescriptor(int index, Object value, SimpleTextAttributes valueTextAttributes) {

        this.index = index;
        this.value = value;
        this.valueTextAttributes = valueTextAttributes;
    }

    @Override
    public void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {
        if (!isNodeExpanded) {
            cellRenderer.append(getFormattedValue(), valueTextAttributes);
        }
    }

    @Override
    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.append(getFormattedKey(), StyleAttributesProvider.getIndexAttribute());
    }

    @Override
    public String getFormattedKey() {
        return String.format("[%s]", index);
    }

    @Override
    public String getFormattedValue() {
        if (getValue() instanceof  Tuple) {
            Tuple tupleValue = (Tuple) getValue();
            return String.format("(%s, %s)", tupleValue.getElement(), tupleValue.getScore());
        }
        return String.valueOf(getValue());
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {

    }

    private static class RedisUnindexedValueDescriptor extends RedisValueDescriptor{

        private RedisUnindexedValueDescriptor(Object value, SimpleTextAttributes valueTextAttributes) {
            super(0, value, valueTextAttributes);
        }

        @Override
        public String getFormattedKey() {
            return "-";
        }
    }
}
