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

package org.codinjutsu.tools.nosql;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredListCellRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SelectDatabaseVendorDialog extends DialogWrapper {


    private JPanel mainPanel;
    private ComboBox databaseVendorCombobox;

    protected SelectDatabaseVendorDialog(Component parent) {
        super(parent, true);
        databaseVendorCombobox.setName("databaseVendorCombobox");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    @Override
    protected void init() {
        super.init();
        initCombobox();
    }

    private void initCombobox() {

        databaseVendorCombobox.setModel(new DefaultComboBoxModel(DatabaseVendor.values()));
        databaseVendorCombobox.setRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                DatabaseVendor databaseVendor = (DatabaseVendor) value;
                setIcon(databaseVendor.icon);
                append(databaseVendor.name);
            }
        });

        databaseVendorCombobox.setSelectedItem(DatabaseVendor.MONGO);
    }

    public DatabaseVendor getSelectedDatabaseVendor() {
        return (DatabaseVendor) databaseVendorCombobox.getSelectedItem();
    }
}
