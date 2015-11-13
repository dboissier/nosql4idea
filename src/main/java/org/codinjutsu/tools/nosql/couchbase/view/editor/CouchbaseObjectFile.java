package org.codinjutsu.tools.nosql.couchbase.view.editor;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.view.editor.NoSqlDatabaseObjectFile;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseDatabase;
import org.jetbrains.annotations.NotNull;

public class CouchbaseObjectFile extends NoSqlDatabaseObjectFile {
    private final CouchbaseDatabase couchbaseDatabase;

    public CouchbaseObjectFile(Project project, ServerConfiguration configuration, CouchbaseDatabase database) {
        super(project, configuration, String.format("%s/%s", configuration.getLabel(), database.getName()));
        this.couchbaseDatabase = database;
    }

    @NotNull
    public FileType getFileType() {
        return CouchbaseFakeFileType.INSTANCE;
    }

    public CouchbaseDatabase getDatabase() {
        return couchbaseDatabase;
    }
}
