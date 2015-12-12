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

package org.codinjutsu.tools.nosql.mongo.view.console;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.console.ConsoleHistoryController;
import com.intellij.execution.console.ConsoleRootType;
import com.intellij.execution.console.ProcessBackedConsoleExecuteActionHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.mongodb.AuthenticationMechanism;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.NoSqlConfiguration;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.model.AuthenticationSettings;
import org.codinjutsu.tools.nosql.commons.view.console.NoSqlConsoleView;
import org.codinjutsu.tools.nosql.mongo.MongoUtils;
import org.codinjutsu.tools.nosql.mongo.logic.MongoExtraSettings;
import org.codinjutsu.tools.nosql.mongo.model.MongoDatabase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class MongoConsoleRunner extends AbstractConsoleRunnerWithHistory<NoSqlConsoleView> {

    private static final Key<Boolean> MONGO_SHELL_FILE = Key.create("MONGO_SHELL_FILE");
    private final ServerConfiguration serverConfiguration;
    private final MongoDatabase database;


    public MongoConsoleRunner(@NotNull Project project, ServerConfiguration serverConfiguration, MongoDatabase database) {
        super(project, "Mongo Shell", "/tmp");

        this.serverConfiguration = serverConfiguration;
        this.database = database;
    }

    @Override
    protected NoSqlConsoleView createConsoleView() {
        NoSqlConsoleView res = new NoSqlConsoleView(getProject(), "Mongo Console", serverConfiguration);

        PsiFile file = res.getFile();
        assert file.getContext() == null;
        file.putUserData(MONGO_SHELL_FILE, Boolean.TRUE);

        return res;
    }

    @Nullable
    @Override
    protected Process createProcess() throws ExecutionException {

        NoSqlConfiguration noSqlConfiguration = NoSqlConfiguration.getInstance(getProject());
        String shellPath = noSqlConfiguration.getShellPath(DatabaseVendor.MONGO);
        final GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(shellPath);

        commandLine.addParameter(MongoUtils.buildMongoUrl(serverConfiguration, database));

        String shellWorkingDir = serverConfiguration.getShellWorkingDir();
        if (StringUtils.isNotBlank(shellWorkingDir)) {
            commandLine.withWorkDirectory(shellWorkingDir);
        }

        AuthenticationSettings authenticationSettings = serverConfiguration.getAuthenticationSettings();

        String username = authenticationSettings.getUsername();
        if (StringUtils.isNotBlank(username)) {
            commandLine.addParameter("--username");
            commandLine.addParameter(username);
        }

        String password = authenticationSettings.getPassword();
        if (StringUtils.isNotBlank(password)) {
            commandLine.addParameter("--password");
            commandLine.addParameter(password);
        }

        MongoExtraSettings mongoExtraSettings = new MongoExtraSettings(authenticationSettings.getExtras());
        String authenticationDatabase = mongoExtraSettings.getAuthenticationDatabase();
        if (StringUtils.isNotBlank(authenticationDatabase)) {
            commandLine.addParameter("--authenticationDatabase");
            commandLine.addParameter(authenticationDatabase);
        }

        AuthenticationMechanism authenticationMecanism = mongoExtraSettings.getAuthenticationMechanism();
        if (authenticationMecanism != null) {
            commandLine.addParameter("--authenticationMecanism");
            commandLine.addParameter(authenticationMecanism.getMechanismName());
        }

        String shellArgumentsLine = serverConfiguration.getShellArgumentsLine();
        if (StringUtils.isNotBlank(shellArgumentsLine)) {
            commandLine.addParameters(shellArgumentsLine.split(" "));
        }

        return commandLine.createProcess();
    }

    @Override
    protected OSProcessHandler createProcessHandler(Process process) {
        return new OSProcessHandler(process, null);
    }

    @NotNull
    @Override
    protected ProcessBackedConsoleExecuteActionHandler createExecuteActionHandler() {
        ProcessBackedConsoleExecuteActionHandler handler = new ProcessBackedConsoleExecuteActionHandler(getProcessHandler(), false) {
            @Override
            public String getEmptyExecuteAction() {
                return "NoSql.Shell.Execute";
            }
        };
        new ConsoleHistoryController(new ConsoleRootType("Mongo Shell", null) {
        }, null, getConsoleView()).install();
        return handler;
    }
}
