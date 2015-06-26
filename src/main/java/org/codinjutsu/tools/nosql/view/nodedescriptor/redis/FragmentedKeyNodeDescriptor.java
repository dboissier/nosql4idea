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
import org.codinjutsu.tools.nosql.database.redis.model.RedisKeyType;
import org.codinjutsu.tools.nosql.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.view.style.StyleAttributesProvider;

public class FragmentedKeyNodeDescriptor implements NodeDescriptor {

    private final String keyFragment;
    private final SimpleTextAttributes keyAttribute;

    public FragmentedKeyNodeDescriptor(String keyFragment, SimpleTextAttributes keyAttribute) {
        this.keyFragment = keyFragment;
        this.keyAttribute = keyAttribute;
    }

    public static FragmentedKeyNodeDescriptor createDescriptor(String key) {
        return new FragmentedKeyNodeDescriptor(key, StyleAttributesProvider.getKeyValueAttribute());
    }

    public String getKeyFragment() {
        return keyFragment;
    }

    @Override
    public void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {

    }

    @Override
    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.append(keyFragment, keyAttribute);
    }

    @Override
    public String getFormattedKey() {
        return keyFragment;
    }

    @Override
    public String getFormattedValue() {
        return null;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void setValue(Object value) {

    }
}