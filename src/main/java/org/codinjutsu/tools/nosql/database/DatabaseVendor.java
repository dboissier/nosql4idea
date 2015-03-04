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

package org.codinjutsu.tools.nosql.database;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.utils.GuiUtils;

import javax.swing.*;

public enum DatabaseVendor {
    MONGO("MongoDB", "mongo.png", "localhost:27017", "format: host:port. If replicat set: host:port1,host:port2,..."),
    REDIS("RedisDB", "redis.png", "localhost:6379", "format: host:port. If cluster: host:port1,host:port2,...");
    public final Icon icon;
    public final String label;
    public final String defaultUrl;
    public final String info;

    DatabaseVendor(String vendorName, String iconName, String defaultUrl, String info) {
        label = vendorName;
        this.defaultUrl = defaultUrl;
        this.info = info;
        icon = GuiUtils.loadIcon(iconName);
    }

    public static DatabaseVendor get(String databaseVendorName) {
        for (DatabaseVendor databaseVendor : DatabaseVendor.values()) {
            if (StringUtils.equals(databaseVendorName, databaseVendor.label)) {
                return databaseVendor;
            }
        }
        return MONGO;
    }
}
