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

package org.codinjutsu.tools.nosql.view;

import org.codinjutsu.tools.nosql.view.nodedescriptor.NodeDescriptor;
import org.fest.swing.driver.BasicJTableCellReader;

import javax.swing.*;

/**
* Created by dboissier on 5/03/15.
*/
class TableCellReader extends BasicJTableCellReader {
    @Override
    public String valueAt(JTable table, int row, int column) {
        NodeDescriptor nodeDescriptor = (NodeDescriptor) table.getValueAt(row, column);
        if (column == 0) {
            return nodeDescriptor.getFormattedKey();
        }
        return nodeDescriptor.getFormattedValue();
    }
}
