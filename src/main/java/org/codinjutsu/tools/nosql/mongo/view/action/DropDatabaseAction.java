package org.codinjutsu.tools.nosql.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.nosql.NoSqlExplorerPanel;
import org.codinjutsu.tools.nosql.commons.style.StyleAttributesProvider;

import javax.swing.*;

/**
 * Created by piddubnyi on 06.11.14 .
 */
public class DropDatabaseAction extends AnAction implements DumbAware {

    private static final Icon REMOVE_ICON = StyleAttributesProvider.getClearAllIcon();

    private final NoSqlExplorerPanel noSqlExplorerPanel;

    public DropDatabaseAction(NoSqlExplorerPanel noSqlExplorerPanel) {
        super("Drop Database", "Drop the selected database", REMOVE_ICON);
        this.noSqlExplorerPanel = noSqlExplorerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        int result = JOptionPane.showConfirmDialog (null, String.format("Do you REALLY want to drop the '%s' database?", noSqlExplorerPanel.getSelectedMongoDatabase().getName()),"Warning",JOptionPane.YES_NO_OPTION);

        if(result == JOptionPane.YES_OPTION){
            noSqlExplorerPanel.dropDatabase();
        }
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(noSqlExplorerPanel.getSelectedMongoDatabase() != null);
    }
}