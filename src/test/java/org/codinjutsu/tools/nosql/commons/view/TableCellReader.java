package org.codinjutsu.tools.nosql.commons.view;

import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;
import org.fest.swing.driver.BasicJTableCellReader;

import javax.swing.*;

public class TableCellReader extends BasicJTableCellReader {

    @Override
    public String valueAt(JTable table, int row, int column) {
        NodeDescriptor nodeDescriptor = (NodeDescriptor) table.getValueAt(row, column);
        if (column == 0) {
            return nodeDescriptor.getFormattedKey();
        }
        return nodeDescriptor.getFormattedValue();
    }
}
