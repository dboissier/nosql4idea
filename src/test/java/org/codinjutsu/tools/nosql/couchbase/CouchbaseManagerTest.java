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

package org.codinjutsu.tools.nosql.couchbase;

import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.couchbase.logic.CouchbaseManager;
import org.junit.Test;

public class CouchbaseManagerTest {
    @Test
    public void loadServers() throws Exception {
        CouchbaseManager couchbaseManager = new CouchbaseManager();
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerUrl("localhost");
        configuration.setUsername("Administrator");
        configuration.setPassword("coincoin");
        couchbaseManager.loadServer(new DatabaseServer(configuration));
    }

    @Test
    public void testName() throws Exception {


    }
}
