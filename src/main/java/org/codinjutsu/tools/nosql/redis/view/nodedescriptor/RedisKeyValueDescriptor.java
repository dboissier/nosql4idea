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

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.nosql.commons.style.StyleAttributesProvider;
import org.codinjutsu.tools.nosql.commons.utils.StringUtils;
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.redis.RedisUtils;
import org.codinjutsu.tools.nosql.redis.model.RedisKeyType;
import redis.clients.jedis.Tuple;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisKeyValueDescriptor implements NodeDescriptor {


    private final RedisKeyType keyType;
    private final String key;
    private final Object value;

    private final SimpleTextAttributes valueTextAttributes;
    private final Icon icon;

    public static RedisKeyValueDescriptor createDescriptor(RedisKeyType keyType, String key, Object value) {
        return new RedisKeyValueDescriptor(keyType, key, value, StyleAttributesProvider.getStringAttribute());
    }

    public static NodeDescriptor createDescriptor(String key, String value) {
        return createDescriptor(null, key, value);
    }

    public RedisKeyValueDescriptor(RedisKeyType keyType, String key, Object value, SimpleTextAttributes valueTextAttributes) {
        this.keyType = keyType;
        this.key = key;
        this.value = value;
        this.icon = findIcon(value);
        this.valueTextAttributes = valueTextAttributes;
    }

    private Icon findIcon(Object object) {
        if (object instanceof List) {
            return AllIcons.Json.Property_brackets;
        } else if (object instanceof Set || object instanceof Map) {
            return AllIcons.Json.Property_braces;
        }
        return null;
    }


    @Override
    public void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {
        if (!isNodeExpanded) {
            cellRenderer.append(getFormattedValue(), valueTextAttributes);
        }
    }

    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.setIcon(this.icon);
        if (this.keyType != null) {
            cellRenderer.append(this.keyType.name(), StyleAttributesProvider.getIndexAttribute());
            cellRenderer.append(" ");
        }
        cellRenderer.append(getFormattedKey(), StyleAttributesProvider.getKeyValueAttribute());
    }

    @Override
    public String getFormattedKey() {
        return key;
    }

    @Override
    public String getFormattedValue() {
        if (RedisKeyType.ZSET.equals(keyType)) {
            return getValueAndAbbreviateIfNecessary(RedisUtils.stringifySortedSet((Set<Tuple>) getValue()));
        } else if (RedisKeyType.SET.equals(keyType)) {
            return getValueAndAbbreviateIfNecessary(RedisUtils.stringifySet((Set) getValue()));
        }
        return getValueAndAbbreviateIfNecessary(getValue().toString());
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {

    }

    public String getKey() {
        return key;
    }

    public RedisKeyType getKeyType() {
        return keyType;
    }

    protected String getValueAndAbbreviateIfNecessary(String stringifiedValue) {
        if (stringifiedValue.length() > MAX_LENGTH) {
            return StringUtils.abbreviateInCenter(stringifiedValue, MAX_LENGTH);
        }
        return stringifiedValue;
    }
}
