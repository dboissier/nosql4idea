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

package org.codinjutsu.tools.nosql.mongo.logic;

import com.mongodb.AuthenticationMechanism;

import java.util.HashMap;
import java.util.Map;

public class MongoExtraSettings {

    private static final String DEFAULT_AUTH_DATABASE = "admin";

    private static final String SSL = "ssl";
    private static final String AUTH_DATABASE = "authDatabase";
    private static final String AUTH_MECHANISM = "authMechanism";
    private Map<String, String> extras = new HashMap<>();

    public MongoExtraSettings() {
    }

    public MongoExtraSettings(Map<String, String> extras) {
        this.extras = extras;
    }

    public String getAuthenticationDatabase() {
        String authDatabase = extras.get(AUTH_DATABASE);
        return authDatabase == null ? DEFAULT_AUTH_DATABASE : authDatabase;
    }

    public AuthenticationMechanism getAuthenticationMechanism() {
        String authMecanism = extras.get(AUTH_MECHANISM);
        return authMecanism == null ? null : AuthenticationMechanism.valueOf(authMecanism);
    }

    public boolean isSsl() {
        String isSsl = extras.get(SSL);
        return isSsl == null ? false : Boolean.valueOf(isSsl);
    }

    public void setAuthenticationDatabase(String authenticationDatabase) {
        extras.put(AUTH_DATABASE, authenticationDatabase);
    }

    public void setAuthenticationMechanism(AuthenticationMechanism authenticationMechanism) {
        extras.put(AUTH_MECHANISM, authenticationMechanism.name());
    }

    public void setSsl(boolean isSsl) {
        extras.put(SSL, String.valueOf(isSsl));
    }

    public Map<String, String> get() {
        return extras;
    }
}
