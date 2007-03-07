/**
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-2005 Jive Software. All rights reserved.
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package org.jivesoftware.sparkplugin;

import org.jivesoftware.resource.SparkRes;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.component.tabbedPane.SparkTab;
import org.jivesoftware.spark.ui.ChatRoom;
import org.jivesoftware.spark.ui.SparkTabHandler;
import org.jivesoftware.sparkplugin.JingleStateManager.JingleRoomState;

import java.awt.Color;
import java.awt.Component;

/**
 * Handles the tab handling for all Jingle calls.
 *
 * @author Derek DeMoro
 */
public class JingleTabHandler extends SparkTabHandler {

    private JingleStateManager manager;

    public JingleTabHandler() {
        manager = JingleStateManager.getInstance();
    }


    public boolean isTabHandled(SparkTab tab, Component component, boolean isSelectedTab, boolean chatFrameFocused) {
        if (component instanceof ChatRoom) {
            JingleRoomState roomState = manager.getJingleRoomState((ChatRoom)component);
            if (roomState == null) {
                // This is not a jingle room.
                return false;
            }

            // This is a room with a jingle session.
            handleJingleRoom(roomState, tab, (ChatRoom)component, isSelectedTab, chatFrameFocused);
            return true;
        }


        return false;
    }

    /**
     * Called when the underlying component has a jingle session.
     *
     * @param state            the JingleRoomState.
     * @param tab              the SparkTab.
     * @param room             the ChatRoom.
     * @param isSelectedTab    true if the tab is selected.
     * @param chatFrameFocused true if the chat frame is in focus.
     */
    private void handleJingleRoom(JingleRoomState state, SparkTab tab, ChatRoom room, boolean isSelectedTab, boolean chatFrameFocused) {
        boolean isTyping = SparkManager.getChatManager().containsTypingNotification(room);

        // Check if is typing.
        if (isTyping) {
            tab.setIcon(SparkRes.getImageIcon(SparkRes.SMALL_MESSAGE_EDIT_IMAGE));
        }
        else if (JingleRoomState.ringing == state) {
            tab.setIcon(JinglePhoneRes.getImageIcon("ANSWER_PHONE_IMAGE"));
        }
        else if (JingleRoomState.inJingleCall == state) {
            tab.setIcon(JinglePhoneRes.getImageIcon("RECEIVER2_IMAGE"));

        }
        else if (JingleRoomState.callWasEnded == state) {
            tab.setIcon(JinglePhoneRes.getImageIcon("HANG_UP_PHONE_16x16_IMAGE"));
        }


        if (!chatFrameFocused || !isSelectedTab) {
            if (room.getUnreadMessageCount() > 0 || JingleRoomState.ringing == state) {
                // Make tab red.
                tab.setTitleColor(Color.red);
                tab.setTabBold(true);
            }

            // Handle unread message count.
            int unreadMessageCount = room.getUnreadMessageCount();
            String appendedMessage = "";
            if (unreadMessageCount > 1) {
                appendedMessage = " (" + unreadMessageCount + ")";
            }

            tab.getTitleLabel().setText(room.getTabTitle() + appendedMessage);

        }

        // Should only set the icon to default if the frame is in focus
        // and the tab is the selected component.
        if (isSelectedTab && chatFrameFocused) {
            tab.setTitleColor(Color.black);
            tab.setTabFont(tab.getDefaultFont());
            tab.getTitleLabel().setText(room.getTabTitle());

            // Clear unread message count.
            room.clearUnreadMessageCount();
        }

    }
}