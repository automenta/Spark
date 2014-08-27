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
package org.jivesoftware.spark.component;

import javax.swing.JPanel;

/**
 * An implementation of a colored background panel. Allows implementations to
 * specify an image to use in the background of the panel.
 */
public class BackgroundPanel extends JPanel {

    private static final long serialVersionUID = 1655446485534962735L;

    /**
     * Creates a background panel using the default Spark background image.
     */
    public BackgroundPanel() {
    }

}
