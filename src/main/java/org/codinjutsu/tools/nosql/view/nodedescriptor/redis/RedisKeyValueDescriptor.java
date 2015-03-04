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

package org.codinjutsu.tools.nosql.view.nodedescriptor.redis;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.nosql.database.redis.RedisUtils;
import org.codinjutsu.tools.nosql.database.redis.model.RedisKeyType;
import org.codinjutsu.tools.nosql.database.redis.model.RedisRecord;
import org.codinjutsu.tools.nosql.database.redis.model.RedisResult;
import org.codinjutsu.tools.nosql.utils.StringUtils;
import org.codinjutsu.tools.nosql.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.view.style.StyleAttributesProvider;
import redis.clients.jedis.Tuple;

import java.util.Set;

public class RedisKeyValueDescriptor implements NodeDescriptor {
    private static final String STRING_SURROUNDED = "\"%s\"";


    private final RedisKeyType keyType;
    private final String key;
    private final Object value;

    private final SimpleTextAttributes valueTextAttributes;

    public static RedisKeyValueDescriptor createDescriptor(RedisKeyType keyType, String key, Object value) {
        return new RedisKeyValueDescriptor(keyType, key, value, StyleAttributesProvider.getStringAttribute());
    }

    public RedisKeyValueDescriptor(RedisKeyType keyType, String key, Object value, SimpleTextAttributes valueTextAttributes) {
        this.keyType = keyType;
        this.key = key;
        this.value = value;
        this.valueTextAttributes = valueTextAttributes;
    }

    @Override
    public void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {
        if (!isNodeExpanded) {
            cellRenderer.append(getFormattedValue(), valueTextAttributes);
        }
    }


    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.append(getFormattedKey(), StyleAttributesProvider.getKeyValueAttribute());
    }

    @Override
    public String getFormattedKey() {
        return String.format(STRING_SURROUNDED, key);
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

    protected String getValueAndAbbreviateIfNecessary(String stringifiedValue) {
        if (stringifiedValue.length() > MAX_LENGTH) {
            return StringUtils.abbreviateInCenter(stringifiedValue, MAX_LENGTH);
        }
        return stringifiedValue;
    }
}
