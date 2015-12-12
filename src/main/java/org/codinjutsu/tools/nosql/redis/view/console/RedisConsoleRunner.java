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

package org.codinjutsu.tools.nosql.redis.view.console;

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
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.NoSqlConfiguration;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.view.console.NoSqlConsoleView;
import org.codinjutsu.tools.nosql.redis.model.RedisDatabase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class RedisConsoleRunner extends AbstractConsoleRunnerWithHistory<NoSqlConsoleView> {

    private static final Key<Boolean> SHELL_FILE = Key.create("REDIS_SHELL_FILE");
    private final ServerConfiguration serverConfiguration;
    private final RedisDatabase database;


    public RedisConsoleRunner(@NotNull Project project, ServerConfiguration serverConfiguration, RedisDatabase database) {
        super(project, "Redis Shell", "/tmp");

        this.serverConfiguration = serverConfiguration;
        this.database = database;
    }

    @Override
    protected NoSqlConsoleView createConsoleView() {
        NoSqlConsoleView res = new NoSqlConsoleView(getProject(), "Redis Console", serverConfiguration);

        PsiFile file = res.getFile();
        assert file.getContext() == null;
        file.putUserData(SHELL_FILE, Boolean.TRUE);

        return res;
    }

    @Nullable
    @Override
    protected Process createProcess() throws ExecutionException {

        NoSqlConfiguration noSqlConfiguration = NoSqlConfiguration.getInstance(getProject());
        String shellPath = noSqlConfiguration.getShellPath(DatabaseVendor.REDIS);
        final GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(shellPath);

        commandLine.addParameter("-n");
        commandLine.addParameter(database.getName());

        String shellWorkingDir = serverConfiguration.getShellWorkingDir();
        if (StringUtils.isNotBlank(shellWorkingDir)) {
            commandLine.setWorkDirectory(shellWorkingDir);
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
        new ConsoleHistoryController(new ConsoleRootType("Redis Shell", null) {
        }, null, getConsoleView()).install();
        return handler;
    }
}
