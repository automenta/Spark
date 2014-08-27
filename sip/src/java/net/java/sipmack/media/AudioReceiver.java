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
package net.java.sipmack.media;

import java.io.IOException;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.protocol.DataSource;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;

/**
 * This class implements receive methods and listeners to be used in
 * AudioChannel
 *
 * @author Thiago Camargo
 */
public class AudioReceiver implements ReceiveStreamListener, SessionListener,
        ControllerListener {

    boolean dataReceived = false;

    Object dataSync;

    public AudioReceiver(Object dataSync) {
        this.dataSync = dataSync;
    }

    /**
     * JingleSessionListener.
     */
    @Override
    public synchronized void update(SessionEvent evt) {
        if (evt instanceof NewParticipantEvent) {
            Participant p = ((NewParticipantEvent) evt).getParticipant();
            System.err.println("  - A new participant had just joined: " + p.getCNAME());
        }
    }

    /**
     * ReceiveStreamListener
     */
    @Override
    public synchronized void update(ReceiveStreamEvent evt) {

        Participant participant = evt.getParticipant();    // could be null.
        ReceiveStream stream = evt.getReceiveStream();  // could be null.

        if (evt instanceof RemotePayloadChangeEvent) {
            System.err.println("  - Received an RTP PayloadChangeEvent.");
            System.err.println("Sorry, cannot handle payload change.");
            // System.exit(0);

        } else if (evt instanceof NewReceiveStreamEvent) {

            try {
                stream = evt.getReceiveStream();
                DataSource ds = stream.getDataSource();

                // Find out the formats.
                RTPControl ctl = (RTPControl) ds.getControl("javax.jmf.rtp.RTPControl");
                if (ctl != null) {
                    System.err.println("  - Recevied new RTP stream: " + ctl.getFormat());
                } else {
                    System.err.println("  - Recevied new RTP stream");
                }

                if (participant == null) {
                    System.err.println("      The sender of this stream had yet to be identified.");
                } else {
                    System.err.println("      The stream comes from: " + participant.getCNAME());
                }

                // create a player by passing datasource to the Media Manager
                Player p = javax.media.Manager.createPlayer(ds);
                if (p == null) {
                    return;
                }

                p.addControllerListener(this);
                p.realize();

                // Notify intialize() that a new stream had arrived.
                synchronized (dataSync) {
                    dataReceived = true;
                    dataSync.notifyAll();
                }

            } catch (IOException | NoPlayerException e) {
                System.err.println("NewReceiveStreamEvent exception " + e.getMessage());
                return;
            }

        } else if (evt instanceof StreamMappedEvent) {

            if (stream != null && stream.getDataSource() != null) {
                DataSource ds = stream.getDataSource();
                // Find out the formats.
                RTPControl ctl = (RTPControl) ds.getControl("javax.jmf.rtp.RTPControl");
                System.err.println("  - The previously unidentified stream ");
                if (ctl != null) {
                    System.err.println("      " + ctl.getFormat());
                }
                System.err.println("      had now been identified as sent by: " + participant.getCNAME());
            }
        } else if (evt instanceof ByeEvent) {

            System.err.println("  - Got \"bye\" from: " + participant.getCNAME());

        }

    }

    /**
     * ControllerListener for the Players.
     */
    @Override
    public synchronized void controllerUpdate(ControllerEvent ce) {

        Player p = (Player) ce.getSourceController();

        if (p == null) {
            return;
        }

        // Get this when the internal players are realized.
        if (ce instanceof RealizeCompleteEvent) {
            p.start();
        }

        if (ce instanceof ControllerErrorEvent) {
            p.removeControllerListener(this);
            System.err.println("Receiver internal error: " + ce);
        }

    }
}
