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

package org.codinjutsu.tools.nosql.mongo;

import org.bson.Document;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.mongo.model.SingleMongoDatabase;

public class MongoUtils {

    private MongoUtils() {
    }

    public static Object parseJSON(String json) {
        // todo: hacky, but works for now.
        // Need to find a better way to do this
        if (json.startsWith("[")) {
            return Document.parse("{'x':" + json + "}").get("x");
        }

        return Document.parse(json);
    }

    public static String buildMongoUrl(ServerConfiguration serverConfiguration, SingleMongoDatabase database) {
        return String.format("%s/%s", serverConfiguration.getServerUrl(), database == null ? "test" : database.getName());
    }
}
