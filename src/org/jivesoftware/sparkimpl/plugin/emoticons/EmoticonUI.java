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
package org.jivesoftware.sparkimpl.plugin.emoticons;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.jivesoftware.spark.component.RolloverButton;

public class EmoticonUI extends JPanel {

    private static final long serialVersionUID = 2360054381356167669L;
    private EmoticonPickListener listener;

    public EmoticonUI() {
        setBackground(Color.white);

        final EmoticonManager manager = EmoticonManager.getInstance();

        Collection<Emoticon> emoticons = manager.getActiveEmoticonSet();

        if (emoticons != null) {

            int no = emoticons.size();

            int rows = no / 5;

            setLayout(new GridLayout(rows, 5));

            emoticons.stream().map((emoticon) -> emoticon.getEquivalants().get(0)).map((text) -> {
                String name = manager.getActiveEmoticonSetName();
                final Emoticon smileEmoticon = manager.getEmoticon(name, text);
                URL smileURL = manager.getEmoticonURL(smileEmoticon);
                ImageIcon icon = new ImageIcon(smileURL);
                RolloverButton emotButton = new RolloverButton();
                emotButton.setIcon(icon);
                emotButton.addActionListener((ActionEvent e) -> {
                    listener.emoticonPicked(text);
                });
                return emotButton;
            }).forEach((emotButton) -> {
                add(emotButton);
            });
        }
    }

    public void setEmoticonPickListener(EmoticonPickListener listener) {
        this.listener = listener;
    }

    public interface EmoticonPickListener {

        void emoticonPicked(String emoticon);
    }
}
