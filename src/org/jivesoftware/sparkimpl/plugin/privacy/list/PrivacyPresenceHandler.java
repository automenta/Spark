package org.jivesoftware.sparkimpl.plugin.privacy.list;

import java.util.Collection;
import org.jivesoftware.resource.SparkRes;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PrivacyItem;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.ui.ContactGroup;
import org.jivesoftware.spark.ui.ContactItem;
import org.jivesoftware.sparkimpl.plugin.privacy.PrivacyManager;

/**
 *
 * @author Bergunde Holger
 */
public class PrivacyPresenceHandler implements SparkPrivacyItemListener {

    /**
     * Send Unavailable (offline status) to jid .
     *
     * @param jid JID to send offline status
     */
    public void sendUnavailableTo(String jid) {
        Presence pack = new Presence(Presence.Type.unavailable);
        pack.setTo(jid);
        SparkManager.getConnection().sendPacket(pack);
    }

    /**
     * Send my presence for user
     *
     * @param jid JID to send presence
     */
    public void sendRealPresenceTo(String jid) {
        Presence presence = SparkManager.getWorkspace().getStatusBar().getPresence();
        Presence pack = new Presence(presence.getType(), presence.getStatus(), 1, presence.getMode());
        pack.setTo(jid);
        SparkManager.getConnection().sendPacket(pack);
    }

    public void setIconsForList(SparkPrivacyList list) {
        list.getPrivacyItems().stream().map((pItem) -> {
            if (pItem.getType().equals(PrivacyItem.Type.jid)) {
                setBlockedIconToContact(pItem.getValue());
                if (pItem.isFilterPresence_out()) {
                    sendUnavailableTo(pItem.getValue());
                }
            }
            return pItem;
        }).filter((pItem) -> (pItem.getType().equals(PrivacyItem.Type.group))).forEach((pItem) -> {
            ContactGroup group = SparkManager.getWorkspace().getContactList().getContactGroup(pItem.getValue());
            group.getContactItems().stream().map((citem) -> {
                setBlockedIconToContact(citem.getJID());
                return citem;
            }).filter((citem) -> (pItem.isFilterPresence_out())).forEach((citem) -> {
                sendUnavailableTo(citem.getJID());
            });
        });
        SparkManager.getContactList().updateUI();
    }

    private void setBlockedIconToContact(String jid) {
        Collection<ContactItem> items = SparkManager.getWorkspace().getContactList().getContactItemsByJID(jid);
        items.stream().filter((contactItem) -> (contactItem != null)).forEach((contactItem) -> {
            contactItem.setSpecialIcon(SparkRes.getImageIcon("PRIVACY_ICON_SMALL"));
        });
    }

    public void removeIconsForList(SparkPrivacyList list) {
        list.getPrivacyItems().stream().map((pItem) -> {
            if (pItem.getType().equals(PrivacyItem.Type.jid)) {
                removeBlockedIconFromContact(pItem.getValue());
                if (pItem.isFilterPresence_out()) {
                    sendRealPresenceTo(pItem.getValue());
                }
            }
            return pItem;
        }).filter((pItem) -> (pItem.getType().equals(PrivacyItem.Type.group))).forEach((pItem) -> {
            ContactGroup group = SparkManager.getWorkspace().getContactList().getContactGroup(pItem.getValue());
            group.getContactItems().stream().map((citem) -> {
                removeBlockedIconFromContact(citem.getJID());
                return citem;
            }).filter((citem) -> (pItem.isFilterPresence_out())).forEach((citem) -> {
                sendRealPresenceTo(citem.getJID());
            });
        });
        SparkManager.getContactList().updateUI();
    }

    private void removeBlockedIconFromContact(String jid) {
        Collection<ContactItem> items = SparkManager.getWorkspace().getContactList().getContactItemsByJID(jid);
        items.stream().filter((item) -> (item != null)).forEach((item) -> {
            item.setSpecialIcon(null);
        });

    }

    @Override
    public void itemAdded(PrivacyItem item, String listname) {
        PrivacyManager pmanager = PrivacyManager.getInstance();
        if (pmanager.getPrivacyList(listname).isActive()) {
            if (item.getType().equals(PrivacyItem.Type.jid)) {
                setBlockedIconToContact(item.getValue());
                if (item.isFilterPresence_out()) {
                    sendUnavailableTo(item.getValue());
                }
            }

            if (item.getType().equals(PrivacyItem.Type.group)) {
                ContactGroup group = SparkManager.getWorkspace().getContactList().getContactGroup(item.getValue());
                group.getContactItems().stream().map((citem) -> {
                    setBlockedIconToContact(citem.getJID());
                    return citem;
                }).filter((citem) -> (item.isFilterPresence_out())).forEach((citem) -> {
                    sendUnavailableTo(citem.getJID());
                });

            }
            SparkManager.getContactList().updateUI();
        }
    }

    @Override
    public void itemRemoved(PrivacyItem item, String listname) {
        PrivacyManager pmanager = PrivacyManager.getInstance();
        if (pmanager.getPrivacyList(listname).isActive()) {
            if (item.getType().equals(PrivacyItem.Type.jid)) {
                removeBlockedIconFromContact(item.getValue());
                if (item.isFilterPresence_out()) {
                    sendRealPresenceTo(item.getValue());
                }
            }

            if (item.getType().equals(PrivacyItem.Type.group)) {
                ContactGroup group = SparkManager.getWorkspace().getContactList().getContactGroup(item.getValue());
                group.getContactItems().stream().map((citem) -> {
                    removeBlockedIconFromContact(citem.getJID());
                    return citem;
                }).filter((citem) -> (item.isFilterPresence_out())).forEach((citem) -> {
                    sendRealPresenceTo(citem.getJID());
                });

            }
            SparkManager.getContactList().updateUI();
        }

    }

}
