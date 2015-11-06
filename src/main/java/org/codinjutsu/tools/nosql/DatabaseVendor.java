package org.codinjutsu.tools.nosql;

import org.codinjutsu.tools.nosql.commons.utils.GuiUtils;

import javax.swing.*;

public enum DatabaseVendor {

    MONGO("MongoDB","mongo.png", "localhost:27017", "format: host:port. If replicat set: host:port1,host:port2,..."),
    REDIS("RedisDB", "redis.png", "localhost:6379", "format: host:port. If cluster: host:port1,host:port2,..."),
    COUCHBASE("Couchbase", "couchbase.png", "localhost:23232", "format: host:port. If cluster: host:port1,host:port2,...");

    public final String name;
    public final Icon icon;
    public final String defaultUrl;
    public final String tips;

    DatabaseVendor(String name, String iconFilename, String defaultUrl, String tips) {
        this.name = name;
        this.icon = GuiUtils.loadIcon(iconFilename);
        this.defaultUrl = defaultUrl;
        this.tips = tips;
    }

    @Override
    public String toString() {
        return "DatabaseVendor{name='" + name  + "'}";
    }
}
