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

package org.codinjutsu.tools.nosql.commons.view;

import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.DatabaseVendorClientManager;
import org.codinjutsu.tools.nosql.DatabaseVendorUIManager;

public class ServerConfigurationPanelFactory {

    private final Project project;
    private final DatabaseVendorClientManager databaseVendorClientManager;
    private final DatabaseVendorUIManager databaseVendorUIManager;

    public ServerConfigurationPanelFactory(Project project,
                                           DatabaseVendorClientManager databaseVendorClientManager,
                                           DatabaseVendorUIManager databaseVendorUIManager) {
        this.project = project;
        this.databaseVendorClientManager = databaseVendorClientManager;
        this.databaseVendorUIManager = databaseVendorUIManager;
    }

    public ServerConfigurationPanel create(DatabaseVendor databaseVendor) {
        return new ServerConfigurationPanel(
                this.project,
                databaseVendor,
                databaseVendorClientManager.get(databaseVendor),
                databaseVendorUIManager.get(databaseVendor).createAythenticationView()
        );
    }
}
