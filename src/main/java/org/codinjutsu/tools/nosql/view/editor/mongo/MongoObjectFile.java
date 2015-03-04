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

package org.codinjutsu.tools.nosql.view.editor.mongo;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.util.LocalTimeCounter;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoCollection;
import org.codinjutsu.tools.nosql.view.editor.NoSqlFileSystem;
import org.codinjutsu.tools.nosql.view.editor.NoSqlObjectFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MongoObjectFile extends NoSqlObjectFile {

    private MongoCollection collection;

    public MongoObjectFile(Project project, ServerConfiguration configuration, MongoCollection collection) {
        super(project, configuration, String.format("%s/%s/%s", configuration.getLabel(), collection.getDatabaseName(), collection.getName()));
        this.collection = collection;

    }

    @NotNull
    public FileType getFileType() {
        return MongoFakeFileType.INSTANCE;
    }


    public MongoCollection getCollection() {
        return collection;
    }
}
