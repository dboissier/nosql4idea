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

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.codinjutsu.tools.nosql.mongo.MongoUtils;

import java.util.LinkedList;
import java.util.List;

public class MongoQueryOptions {

    private static final int DEFAULT_RESULT_LIMIT = 300;

    private static final BasicDBObject EMPTY_FILTER = new BasicDBObject();
    private final List<Document> operations = new LinkedList<>();

    private Bson filter = EMPTY_FILTER;
    private Bson projection = null;
    private Bson sort;

    private int resultLimit = DEFAULT_RESULT_LIMIT;

    public boolean isAggregate() {
        return !operations.isEmpty();
    }

    public List<Document> getOperations() {
        return operations;
    }

    public void setOperations(String aggregateQuery) {
        operations.clear();
        List operations = (List)MongoUtils.parseJSON(aggregateQuery);
        //noinspection unchecked - safe to ignore - it parses it to a List<Document>
        this.operations.addAll(operations);
    }

    public void setFilter(String query) {
        if (!StringUtils.isBlank(query)) {
            filter = (Bson) JSON.parse(query);
        }
    }

    public Bson getFilter() {
        return filter;
    }

    public void setProjection(String query) {
        if (!StringUtils.isBlank(query)) {
            projection = (Bson) JSON.parse(query);
        }
    }


    public Bson getProjection() {
        return projection;
    }

    public void setSort(String query) {
        if (!StringUtils.isBlank(query)) {
            sort = (Bson) JSON.parse(query);
        }
    }

    public Bson getSort() {
        return sort;
    }

    public int getResultLimit() {
        return resultLimit;
    }

    public void setResultLimit(int resultLimit) {
        this.resultLimit = resultLimit;
    }
}
