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
package org.jivesoftware.sparkplugin.components;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JButton;
import org.jivesoftware.spark.plugin.phone.resource.PhoneRes;

/**
 *
 */
public class CloseButton extends JButton implements MouseListener {

    private static final long serialVersionUID = -439443751174954392L;
    private final Icon normalIcon;
    private final Icon hoverIcon;
    private final Icon downIcon;

    public CloseButton() {
        super();

        normalIcon = PhoneRes.getImageIcon("CLOSE_BUTTON");
        hoverIcon = PhoneRes.getImageIcon("CLOSE_BUTTON_HOVER");
        downIcon = PhoneRes.getImageIcon("CLOSE_BUTTON_DOWN");
        setIcon(normalIcon);
        decorate();

        addMouseListener(this);
    }

    /**
     * Decorates the button with the approriate UI configurations.
     */
    private void decorate() {
        setBorderPainted(false);
        setOpaque(true);

        setContentAreaFilled(false);
        setMargin(new Insets(0, 0, 0, 0));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        setIcon(downIcon);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        setIcon(normalIcon);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setIcon(hoverIcon);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setIcon(normalIcon);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
