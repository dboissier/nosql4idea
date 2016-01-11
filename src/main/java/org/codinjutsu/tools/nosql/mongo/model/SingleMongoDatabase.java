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

package org.codinjutsu.tools.nosql.mongo.model;

import org.codinjutsu.tools.nosql.commons.model.Database;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SingleMongoDatabase extends Database {

    private final SortedSet<SingleMongoCollection> collections = new TreeSet<SingleMongoCollection>();

    public SingleMongoDatabase(String name) {
        super(name);
    }

    public String getName() {
        return name;
    }

    public Set<SingleMongoCollection> getCollections() {
        return collections;
    }

    public void addCollection(SingleMongoCollection singleMongoCollection) {
        collections.add(singleMongoCollection);
    }
}
