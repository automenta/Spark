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
package net.java.sipmack.softphone.gui;

import java.util.ArrayList;
import java.util.List;
import net.java.sipmack.common.AlertManager;
import net.java.sipmack.common.Log;
import net.java.sipmack.events.UserActionListener;
import net.java.sipmack.sip.Call;
import net.java.sipmack.sip.Interlocutor;
import net.java.sipmack.sip.InterlocutorUI;
import net.java.sipmack.softphone.listeners.InterlocutorListener;

/**
 * The <code>GuiManager</code> class that Manage all the actions and Events of
 * User Interface.
 *
 * @author Thiago Rocha Camargo (thiago@jivesoftware.com)
 * @version 1.0, 20/07/2006
 */
public class GuiManager implements GuiCallback, DefaultGuiManager {

    private final List<InterlocutorUI> interlocutors = new ArrayList<>();

    private final AlertManager alertManager = new AlertManager();

    public List<UserActionListener> actionHandlers = new ArrayList<>();

    public List<InterlocutorListener> interlocutorListeners = new ArrayList<>();

    private boolean autoAnswer = false;

    /**
     * Constructor of the class. Instantiate DTMFSounds and create the GUI
     */
    public GuiManager() {
    }

    /**
     * Loads the config form SIPConfig class
     */
    public void loadConfig() {
    }

    /**
     * Sets the actionListener
     *
     * @param ual UserActionListener that will handle actions
     */
    public void addUserActionListener(UserActionListener ual) {
        actionHandlers.add(ual);
    }

    /**
     * Adds an InterlocutorListener
     *
     * @param interlocutorListener
     */
    public void addInterlocutorListener(InterlocutorListener interlocutorListener) {
        interlocutorListeners.add(interlocutorListener);
    }

    /**
     * Removes an InterlocutorListener
     *
     * @param interlocutorListener
     */
    public void removeInterlocutorListener(InterlocutorListener interlocutorListener) {
        interlocutorListeners.remove(interlocutorListener);
    }

    /**
     * Add a new interlocutor
     *
     * @param interlocutors InterlocutorUI to be added.
     */
    public synchronized void addInterlocutor(InterlocutorUI interlocutors) {
        interlocutors.setCallback(this);

        this.interlocutors.add(interlocutors);

        interlocutorListeners.stream().forEach((interlocutorListener) -> {
            interlocutorListener.interlocutorAdded(interlocutors);
        });
    }

    /**
     * Update the interlocutor
     *
     * @param interlocutorUI To be updated
     */
    @Override
    public void update(InterlocutorUI interlocutorUI) {

    }

    /**
     * Returns the current interlocutors
     *
     * @return List<InterlocutorUI>
     */
    @Override
    public List<InterlocutorUI> getInterlocutors() {
        return interlocutors;
    }

    /**
     * Counts the current interlocutors number
     */
    public int countInterlocutors() {
        return interlocutors.size();
    }

    /**
     * Remove an interlocutor
     *
     * @param interlocutorUI To be removed
     */
    @Override
    public synchronized void remove(InterlocutorUI interlocutorUI) {
        interlocutors.remove(interlocutorUI);
        interlocutorListeners.stream().forEach((interlocutorListener) -> {
            interlocutorListener.interlocutorRemoved(interlocutorUI);
        });
    }

    /**
     * Start to play a wav.
     *
     * @param alertResourceName The wav to be played
     */
    @Override
    public void startAlert(String alertResourceName) {
        try {
            alertManager.startAlert(alertResourceName);
        } catch (Throwable ex) {
            // OK, no one cares really
        }
    }

    /**
     * Stop to play a wav.
     *
     * @param alertResourceName The wav to be stop
     */
    @Override
    public void stopAlert(String alertResourceName) {
        try {
            alertManager.stopAlert(alertResourceName);
        } catch (Throwable ex) {
            // OK, no one cares really
        }
    }

    /**
     * Stop all waves.
     */
    public void stopAllAlerts() {
        try {
            alertManager.stopAllAlerts();
        } catch (Throwable ex) {
            // OK, no one cares really
        }
    }

    /**
     * Answer the current ringing call
     */
    @Override
    public boolean answer() {
        if (interlocutors.size() < 1) {
            if (Log.debugging) Log.debug("answer", "No interlocutors");
            return false;
        }

        boolean found = false;

        for (InterlocutorUI interlocutor : interlocutors) {
            Interlocutor inter = (Interlocutor) interlocutor;
            if (!inter.getCall().isIncoming() || !inter.getCall().getState().equals(Call.ALERTING)) {
                continue;
            }
            found = true;
            actionHandlers.stream().forEach((ual) -> {
                ual.handleAnswerRequest(inter);
            });
        }
        if (Log.debugging) Log.debug("answer", "Answered");
        return found;
    }

    /**
     * Hold all current calls. In fact it holds all medias depending of the
     * server.
     */
    @Override
    public void holdAll() {
        if (interlocutors.size() < 1) {
            if (Log.debugging) Log.debug("hold", "No interlocutors");
            return;
        }

        interlocutors.stream().forEach((interlocutor) -> {
            boolean mic = interlocutor.onHoldMic(), cam = interlocutor.onHoldCam();
            actionHandlers.stream().forEach((ual) -> {
                ual.handleHold(interlocutor, !mic, cam);
            });
        });
    }

    /**
     * Hold current call of associated interlocutor. In fact it holds all medias
     * depending of the server.
     *
     * @param interlocutor interlocutor that will be holded
     */
    @Override
    public void hold(InterlocutorUI interlocutor) {
        boolean mic = interlocutor.onHoldMic(), cam = interlocutor.onHoldCam();
        actionHandlers.stream().forEach((ual) -> {
            ual.handleHold(interlocutor, !mic, cam);
        });
    }

    /**
     * Mute all current calls.
     */
    @Override
    public void muteAll(boolean mic) {
        if (interlocutors.size() < 1) {
            if (Log.debugging) Log.debug("mute", "No interlocutors");
            return;
        }
        interlocutors.stream().forEach((interlocutor) -> {
            actionHandlers.stream().forEach((ual) -> {
                ual.handleMute(interlocutor, mic);
            });
        });
    }

    /**
     * Mute the current call associated with the informed interlocutor.
     *
     * @param interlocutor
     * @param mic
     */
    @Override
    public void mute(InterlocutorUI interlocutor, boolean mic) {
        actionHandlers.stream().forEach((ual) -> {
            ual.handleMute(interlocutor, mic);
        });
    }

    /**
     * Send a DTMF Tone to all current calls
     *
     * @param digit DTMF digit to be sent
     */
    @Override
    public void sendDTMF(String digit) {
        if (interlocutors.size() < 1) {
            if (Log.debugging) Log.debug("sendDTMF", "No interlocutors");
            return;
        }
        int selectedRow = 0;
        Interlocutor inter = (Interlocutor) interlocutors.get(selectedRow);
        actionHandlers.stream().forEach((ual) -> {
            ual.handleDTMF(inter, digit);
        });
    }

    /**
     * Dial a number
     *
     * @param callee Number to be called
     */
    @Override
    public void dial(String callee) {
        actionHandlers.stream().forEach((ual) -> {
            ual.handleDialRequest(callee);
        });
    }

    /**
     * Hangup the current call
     */
    @Override
    public boolean hangupAll() {
        if (interlocutors.size() < 1) {
            if (Log.debugging) Log.debug("hangup", "No interlocutors");
            return false;
        }
        Interlocutor inter;
        for (int i = 0; i < interlocutors.size(); i++) {
            inter = (Interlocutor) interlocutors.get(i);
            for (UserActionListener ual : actionHandlers) {
                ual.handleHangupRequest(inter);
            }
        }
        return true;
    }

    /**
     * Hangup the call associated with the informed InterlocutorUI
     *
     * @param interlocutorUI
     * @return
     */
    @Override
    public boolean hangup(InterlocutorUI interlocutorUI) {
        boolean result = true;
        for (UserActionListener ual : actionHandlers) {
            result = ual.handleHangupRequest((Interlocutor) interlocutorUI) ? result : false;
        }
        return result;
    }

    /**
     * Set the autoAnswer option
     *
     * @param value The value to be set
     */
    public void setAutoAnswer(boolean value) {
        autoAnswer = value;
    }

    /**
     * Get the autoAnswer option
     *
     * @return The value
     */
    @Override
    public boolean getAutoAnswer() {
        return autoAnswer;
    }

}
