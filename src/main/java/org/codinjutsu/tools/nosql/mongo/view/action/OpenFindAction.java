package org.codinjutsu.tools.nosql.mongo.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.nosql.mongo.view.MongoPanel;

import java.awt.event.KeyEvent;

public class OpenFindAction extends AnAction implements DumbAware {
    private final MongoPanel mongoPanel;

    public OpenFindAction(MongoPanel mongoPanel) {
        super("Find", "Open Find editor", AllIcons.Actions.Find);
        this.mongoPanel = mongoPanel;
        registerCustomShortcutSet(KeyEvent.VK_F, KeyEvent.CTRL_MASK, mongoPanel);
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        if (!mongoPanel.isFindEditorOpened()) {
            mongoPanel.openFindEditor();
        } else {
            mongoPanel.focusOnEditor();
        }
    }
}
