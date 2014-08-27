/**
 * $RCSfile: ,v $ $Revision: $ $Date: $
 *
 * Copyright (C) 2004-2011 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jivesoftware.spark.search;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import org.jivesoftware.Spark;
import org.jivesoftware.resource.Default;
import org.jivesoftware.resource.Res;
import org.jivesoftware.resource.SparkRes;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.Workspace;
import org.jivesoftware.spark.component.IconTextField;
import org.jivesoftware.spark.util.ResourceUtils;
import org.jivesoftware.spark.util.SwingWorker;

public class SearchService extends JPanel {

    private static final long serialVersionUID = 6407801290193187867L;
    private IconTextField findField;
    private boolean newSearch;

    private Searchable activeSearchable;

    public SearchService() {
        setLayout(new GridBagLayout());
        findField = new IconTextField(SparkRes.getImageIcon(SparkRes.SEARCH_USER_16x16));

        final JLabel findLabel = new JLabel();

        ResourceUtils.resLabel(findLabel, findField, Res.getString("label.find"));

        // add(findLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        boolean hide = Default.getBoolean(Default.HIDE_PERSON_SEARCH_FIELD);
        if (!hide) {
            if (Spark.isMac()) {
                add(findField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 30), 0, 0));
            } else {
                add(findField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            }
        }

        // Check for secure connection
        if (SparkManager.getConnection().isSecureConnection()) {
            final JLabel lockLabel = new JLabel();
            lockLabel.setHorizontalTextPosition(JLabel.LEFT);
            lockLabel.setIcon(SparkRes.getImageIcon(SparkRes.LOCK_16x16));
            if (!hide) {
                if (Spark.isMac()) {
                    add(lockLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 15), 0, 0));

                } else {
                    add(lockLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));

                }
            }
            lockLabel.setToolTipText(Res.getString("message.spark.secure"));
        }

        findField.setToolTipText(Res.getString("message.search.for.contacts"));

        findField.getTextComponent().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    final Icon previousIcon = findField.getIcon();

                    findField.setIcon(SparkRes.getImageIcon(SparkRes.BUSY_IMAGE));
                    findField.validate();
                    findField.repaint();

                    SwingWorker worker = new SwingWorker() {
                        @Override
                        public Object construct() {
                            activeSearchable.search(findField.getText());
                            return true;
                        }

                        @Override
                        public void finished() {
                            findField.setIcon(previousIcon);
                            findField.setText("");
                        }
                    };

                    worker.start();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        findField.getTextComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (newSearch) {
                    findField.setText("");
                    findField.getTextComponent().setForeground((Color) UIManager.get("TextField.foreground"));
                    newSearch = false;
                }
            }
        });

        Workspace workspace = SparkManager.getWorkspace();
        workspace.add(this, new GridBagConstraints(0, 10, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        workspace.invalidate();
        workspace.validate();
        workspace.repaint();

        findField.getImageComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Collection<Searchable> searchables = SparkManager.getSearchManager().getSearchServices();
                if (searchables.size() <= 1) {
                    return;
                }

                // Show popup
                final JPopupMenu popup = new JPopupMenu();
                searchables.stream().map((searchable) -> {
                    Action action = new AbstractAction() {
                        private static final long serialVersionUID = 1289193809077193703L;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            setActiveSearchService(searchable);
                        }
                    };
                    action.putValue(Action.SMALL_ICON, searchable.getIcon());
                    action.putValue(Action.NAME, searchable.getName());
                    return action;
                }).forEach((action) -> {
                    popup.add(action);
                });

                popup.show(findField, 0, findField.getHeight());

            }
        });
    }

    public void setActiveSearchService(final Searchable searchable) {
        this.activeSearchable = searchable;

        newSearch = true;
        findField.requestFocus();
        findField.getTextComponent().setForeground((Color) UIManager.get("TextField.lightforeground"));
        findField.setIcon(searchable.getIcon());
        findField.setText(searchable.getDefaultText());
        findField.setToolTipText(searchable.getToolTip());

        findField.getTextComponent().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                findField.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                findField.getTextComponent().setForeground((Color) UIManager.get("TextField.lightforeground"));
                findField.setText(searchable.getDefaultText());
            }
        });
    }

    protected IconTextField getFindField() {
        return findField;
    }

}
