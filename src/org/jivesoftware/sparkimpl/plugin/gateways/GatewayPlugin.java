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
package org.jivesoftware.sparkimpl.plugin.gateways;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.jivesoftware.resource.Res;
import org.jivesoftware.resource.SparkRes;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;
import org.jivesoftware.spark.ChatManager;
import org.jivesoftware.spark.PresenceManager;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.component.MessageDialog;
import org.jivesoftware.spark.component.VerticalFlowLayout;
import org.jivesoftware.spark.plugin.Plugin;
import org.jivesoftware.spark.ui.ContactGroup;
import org.jivesoftware.spark.ui.ContactItem;
import org.jivesoftware.spark.ui.ContactItemHandler;
import org.jivesoftware.spark.ui.ContactList;
import org.jivesoftware.spark.ui.PresenceListener;
import org.jivesoftware.spark.util.SwingWorker;
import org.jivesoftware.spark.util.log.Log;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.AIMTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.FacebookTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.GTalkTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.GaduGaduTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.ICQTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.IRCTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.MSNTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.MySpaceTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.QQTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.SametimeTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.SimpleTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.Transport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.TransportUtils;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.XMPPTransport;
import org.jivesoftware.sparkimpl.plugin.gateways.transports.YahooTransport;
import org.jivesoftware.sparkimpl.settings.local.LocalPreferences;
import org.jivesoftware.sparkimpl.settings.local.SettingsManager;

/**
 * Handles Gateways/Transports in Spark.
 *
 * @author Derek DeMoro
 */
public class GatewayPlugin implements Plugin, ContactItemHandler {

    /**
     * Defined Static Variable for Gateways. *
     */
    public static final String GATEWAY = "gateway";
    private boolean useTab;

    private final Map<Transport, GatewayItem> uiMap = new HashMap<>();
    ;
    private final JPanel transferTab = new JPanel();

    @Override
    public void initialize() {
        ProviderManager.getInstance().addIQProvider(Gateway.ELEMENT_NAME, Gateway.NAMESPACE, new Gateway.Provider());
        LocalPreferences localPref = SettingsManager.getLocalPreferences();
        useTab = localPref.getShowTransportTab();
        transferTab.setBackground((Color) UIManager.get("ContactItem.background"));
        SwingWorker thread = new SwingWorker() {
            @Override
            public Object construct() {
                try {
                    // Let's try and avoid any timing issues with the gateway presence.
                    Thread.sleep(5000);
                    populateTransports();
                } catch (Exception e) {
                    Log.error(e);
                    return false;
                }

                return true;
            }

            @Override
            public void finished() {

                transferTab.setLayout(new VerticalFlowLayout(0, 0, 0, true, false));
                Boolean transportExists = (Boolean) get();
                if (!transportExists) {
                    return;
                }

                if (TransportUtils.getTransports().size() > 0 && useTab) {
                    SparkManager.getWorkspace().getWorkspacePane().addTab(Res.getString("title.transports"), SparkRes.getImageIcon(SparkRes.TRANSPORT_ICON), transferTab);
                }

                TransportUtils.getTransports().stream().forEach((transport) -> {
                    addTransport(transport);
                });

                // Register presences.
                registerPresenceListener();
            }
        };

        thread.start();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean canShutDown() {
        return false;
    }

    @Override
    public void uninstall() {
    }

    private void populateTransports() throws Exception {
        DiscoverItems discoItems = SparkManager.getSessionManager().getDiscoveredItems();

        DiscoverItems.Item item;

        Iterator<DiscoverItems.Item> items = discoItems.getItems();
        while (items.hasNext()) {
            item = items.next();
            String entityName = item.getEntityID();
            if (entityName != null) {
                if (entityName.startsWith("aim.")) {
                    AIMTransport aim = new AIMTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), aim);
                } else if (entityName.startsWith("msn.")) {
                    MSNTransport msn = new MSNTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), msn);
                } else if (entityName.startsWith("yahoo.")) {
                    YahooTransport yahoo = new YahooTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), yahoo);
                } else if (entityName.startsWith("icq.")) {
                    ICQTransport icq = new ICQTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), icq);
                } else if (entityName.startsWith("gtalk.")) {
                    GTalkTransport gtalk = new GTalkTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), gtalk);
                } else if (entityName.startsWith("xmpp.")) {
                    XMPPTransport xmppTransport = new XMPPTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), xmppTransport);
                } else if (entityName.startsWith("irc.")) {
                    IRCTransport ircTransport = new IRCTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), ircTransport);
                } else if (entityName.startsWith("sip.") || entityName.startsWith("simple.")) {
                    SimpleTransport simpleTransport = new SimpleTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), simpleTransport);
                } else if (entityName.startsWith("gadugadu.")) {
                    GaduGaduTransport gadugaduTransport = new GaduGaduTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), gadugaduTransport);
                } else if (entityName.startsWith("qq.")) {
                    QQTransport qqTransport = new QQTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), qqTransport);
                } else if (entityName.startsWith("sametime.")) {
                    SametimeTransport sametimeTransport = new SametimeTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), sametimeTransport);
                } else if (entityName.startsWith("facebook.")) {
                    FacebookTransport facebookTransport = new FacebookTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), facebookTransport);
                } else if (entityName.startsWith("myspace.") || entityName.startsWith("myspaceim.")) {
                    MySpaceTransport myspaceTransport = new MySpaceTransport(item.getEntityID());
                    TransportUtils.addTransport(item.getEntityID(), myspaceTransport);
                }
            }
        }

    }

    private void addTransport(final Transport transport) {
        GatewayItem item = null;
        if (useTab) {
            item = new GatewayTabItem(transport);
            transferTab.add((Component) item);
        } else {
            item = new GatewayButton(transport);
        }
        uiMap.put(transport, item);
        //  transferTab.add(button);
        //  transferTab.add(new GatewayTabItem(transport));
    }

    private void registerPresenceListener() {
        PacketFilter orFilter = new OrFilter(new PacketTypeFilter(Presence.class), new PacketTypeFilter(Message.class));

        SparkManager.getConnection().addPacketListener((Packet packet) -> {
            if (packet instanceof Presence) {
                Presence presence = (Presence) packet;
                Transport transport = TransportUtils.getTransport(packet.getFrom());
                if (transport != null) {
                    boolean registered = true;
                    if (presence.getType() == Presence.Type.unavailable) {
                        registered = false;
                    }
                    
                    GatewayItem button = uiMap.get(transport);
                    
                    button.signedIn(registered);
                    
                    SwingWorker worker = new SwingWorker() {
                        
                        @Override
                        public Object construct() {
                            transferTab.revalidate();
                            transferTab.repaint();
                            return 41;
                        }
                    };
                    worker.start();
                }
            } else if (packet instanceof Message) {
                Message message = (Message) packet;
                String from = message.getFrom();
                boolean hasError = message.getType() == Message.Type.error;
                String body = message.getBody();
                
                if (from != null && hasError) {
                    Transport transport = TransportUtils.getTransport(from);
                    if (transport != null) {
                        String title = "Alert from " + transport.getName();
                        // Show error
                        MessageDialog.showAlert(body, title, "Information", SparkRes.getImageIcon(SparkRes.INFORMATION_IMAGE));
                    }
                }
            }
        }, orFilter);

        ChatManager chatManager = SparkManager.getChatManager();
        chatManager.addContactItemHandler(this);

        // Iterate through Contacts and check for
        final ContactList contactList = SparkManager.getWorkspace().getContactList();
        contactList.getContactGroups().stream().forEach((contactGroup) -> {
            contactGroup.getContactItems().stream().forEach((contactItem) -> {
                Presence presence = contactItem.getPresence();
                if (presence.isAvailable()) {
                    String domain = StringUtils.parseServer(presence.getFrom());
                    Transport transport = TransportUtils.getTransport(domain);
                    if (transport != null) {
                        handlePresence(contactItem, presence);
                        contactGroup.fireContactGroupUpdated();
                    }
                }
            });
        });

        SparkManager.getSessionManager().addPresenceListener((Presence presence) -> {
            for (Transport transport : TransportUtils.getTransports()) {
                GatewayItem button = uiMap.get(transport);
                if (button.isLoggedIn()) {
                    if (!presence.isAvailable()) {
                        return;
                    }
                    // Create new presence
                    Presence p = new Presence(presence.getType(), presence.getStatus(), presence.getPriority(), presence.getMode());
                    p.setTo(transport.getServiceName());
                    SparkManager.getConnection().sendPacket(p);
                }
            }
        });
    }

    @Override
    public boolean handlePresence(ContactItem item, Presence presence) {
        if (presence.isAvailable()) {
            String domain = StringUtils.parseServer(presence.getFrom());
            Transport transport = TransportUtils.getTransport(domain);
            if (transport != null) {
                if (presence.getType() == Presence.Type.available) {
                    item.setSpecialIcon(transport.getIcon());
                } else {
                    item.setSpecialIcon(transport.getInactiveIcon());
                }
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean handleDoubleClick(ContactItem item) {
        return false;
    }

    @Override
    public Icon getIcon(String jid) {
        String domain = StringUtils.parseServer(jid);
        Transport transport = TransportUtils.getTransport(domain);
        if (transport != null) {
            if (PresenceManager.isOnline(jid)) {
                return transport.getIcon();
            } else {
                return transport.getInactiveIcon();
            }
        }
        return null;
    }

    @Override
    public Icon getTabIcon(Presence presence) {
        return null;
    }
}
