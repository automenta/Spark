/**
 * $Revision: $ $Date: $
 *
 * Copyright (C) 2006 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Lesser Public License
 * (LGPL), a copy of which is included in this distribution.
 */
package org.jivesoftware.spark.plugins;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import org.jivesoftware.MainWindowListener;
import org.jivesoftware.Spark;
import org.jivesoftware.resource.Default;
import org.jivesoftware.resource.Res;
import org.jivesoftware.resource.SparkRes;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.Workspace;
import org.jivesoftware.spark.component.ImageTitlePanel;
import org.jivesoftware.spark.component.WrappedLabel;
import org.jivesoftware.spark.ui.PresenceListener;
import org.jivesoftware.spark.ui.status.StatusBar;
import org.jivesoftware.spark.ui.status.StatusItem;

/**
 * Handles tray icon operations inside of Spark. Use to display incoming chat
 * requests, incoming messages and general notifications.
 */
public final class SparkSystemTray implements ActionListener, MainWindowListener {

    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private JPopupMenu notificationDialog;
    private WrappedLabel messageLabel = new WrappedLabel();

    private final MenuItem openMenu = new MenuItem(Res.getString("menuitem.open"));
    private final MenuItem hideMenu = new MenuItem(Res.getString("menuitem.hide"));
    private final MenuItem exitMenu = new MenuItem(Res.getString("menuitem.exit"));
    private final MenuItem logoutMenu = new MenuItem(Res.getString("menuitem.logout.no.status"));

    // Define DND MenuItems
    private final Menu statusMenu = new Menu(Res.getString("menuitem.status"));

    private ImageTitlePanel headerLabel = new ImageTitlePanel();

    private JFrame hideWindow = null;

    private ImageIcon availableIcon;
    private ImageIcon awayIcon;
    private ImageIcon dndIcon;

    /**
     * Creates a new instance of notifications.
     */
    public SparkSystemTray() {
        if (Spark.isMac()) {
            return;
        }

        setupNotificationDialog();

        // Handle tray image.
        availableIcon = Default.getImageIcon(Default.TRAY_IMAGE);
        if (availableIcon == null) {
            availableIcon = SparkRes.getImageIcon(SparkRes.TRAY_IMAGE);
        }

        awayIcon = SparkRes.getImageIcon(SparkRes.MESSAGE_AWAY);
        dndIcon = SparkRes.getImageIcon(SparkRes.MESSAGE_DND);

        trayIcon = new TrayIcon(new ImageIcon(availableIcon.getImage()).getImage());
        trayIcon.setImageAutoSize(true);
        systemTray = SystemTray.getSystemTray();
        setTrayIcon(availableIcon);

        trayIcon.setToolTip(Default.getString(Default.APPLICATION_NAME)); // NORES

        PopupMenu popupMenu = new PopupMenu(Res.getString("title.tray.information"));

        // Add DND Menus
        addStatusMenuItems();

        // Add Open Menu
        openMenu.setFont(new Font("Dialog", Font.BOLD, 11));
        popupMenu.add(openMenu);

        // Add Hide Menu
        popupMenu.add(hideMenu);
        popupMenu.addSeparator();
        // Add Spark Home Menu

        popupMenu.add(statusMenu);

        // Add Listeners
        openMenu.addActionListener(this);
        hideMenu.addActionListener(this);

        Action logoutAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SparkManager.getMainWindow().logout(false);
            }
        };

        logoutAction.putValue(Action.NAME, Res.getString("menuitem.logout.no.status"));
        logoutMenu.addActionListener(logoutAction);

        if (Spark.isWindows()) {
            popupMenu.add(logoutMenu);
        }

        // Add Exit Menu
        popupMenu.add(exitMenu);
        exitMenu.addActionListener(this);

        SparkManager.getMainWindow().addMainWindowListener(this);

        trayIcon.setPopupMenu(popupMenu);
        try {
            systemTray.add(trayIcon);
        } catch (AWTException ex) {
            Logger.getLogger(SparkSystemTray.class.getName()).log(Level.SEVERE, null, ex);
        }

        trayIcon.addActionListener((ActionEvent e) -> {
            showMainWindow();
        });

        SparkManager.getSessionManager().addPresenceListener(this::changePresence);
    }

    /**
     * Change the presence of the tray.
     *
     * @param presence the new presence.
     */
    public void changePresence(Presence presence) {
        if (Spark.isMac()) {
            return;
        }

        if (presence.getMode() == Presence.Mode.available || presence.getMode() == Presence.Mode.chat) {
            setTrayIcon(availableIcon);
        } else if (presence.getMode() == Presence.Mode.away || presence.getMode() == Presence.Mode.xa) {
            setTrayIcon(awayIcon);
        } else {
            setTrayIcon(dndIcon);
        }

        // Get Status Text
        if (presence.isAvailable()) {
            String status = presence.getStatus();
            trayIcon.setToolTip(Default.getString(Default.APPLICATION_NAME) + "\n" + status);
        }
    }

    /**
     * Stops the icon from flashing.
     */
    public void stopFlashing() {
        Workspace workspace = SparkManager.getWorkspace();

        StatusBar statusBox = workspace.getStatusBar();
        Presence presence = statusBox.getPresence();

        changePresence(presence);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (!(o instanceof MenuItem)) {
            /*
             MainWindow window = SparkManager.getMainWindow();
             if (window.isVisible() && window.getState() == Frame.NORMAL) {
             long now = System.currentTimeMillis();
             if (now - madeVisibleTime > 1000) {
             window.setVisible(false);
             return;
             }
             }

             madeVisibleTime = System.currentTimeMillis();
             */
            showMainWindow();
            return;
        }

        final MenuItem item = (MenuItem) e.getSource();
        if (item == openMenu) {
            showMainWindow();
        } else if (item == hideMenu) {
            SparkManager.getMainWindow().setVisible(false);
            hideMenu.setEnabled(false);
        } else if (item == exitMenu) {
            SparkManager.getMainWindow().shutdown();
        } else {
            final String status = item.getLabel();

            // Change Status
            Workspace workspace = SparkManager.getWorkspace();
            StatusItem statusItem = workspace.getStatusBar().getStatusItem(status);
            if (statusItem != null) {
                SparkManager.getSessionManager().changePresence(statusItem.getPresence());
            }
        }
    }

    /**
     * Brings the <code>MainWindow</code> to the front.
     */
    private void showMainWindow() {
        if (hideWindow != null) {
            hideWindow.dispose();
        }

        SparkManager.getMainWindow().setState(Frame.NORMAL);
        SparkManager.getMainWindow().setVisible(true);

        notificationDialog.setVisible(false);

        hideMenu.setEnabled(true);

        SparkManager.getMainWindow().toFront();
    }

    /**
     * Add all Registered DND's to MenuItems.
     */
    private void addStatusMenuItems() {
        Workspace workspace = SparkManager.getWorkspace();
        StatusBar statusBar = workspace.getStatusBar();

        for (Object o : statusBar.getStatusList()) {
            StatusItem item = (StatusItem) o;
            final MenuItem menuItem = new MenuItem(item.getText());
            menuItem.addActionListener(this);
            statusMenu.add(menuItem);
        }
    }

    @Override
    public void shutdown() {
        systemTray.remove(trayIcon);
    }

    @Override
    public void mainWindowActivated() {
    }

    @Override
    public void mainWindowDeactivated() {
    }

    /**
     * Setup notification dialog.
     */
    private void setupNotificationDialog() {
        notificationDialog = new JPopupMenu();
        notificationDialog.setFocusable(false);
        notificationDialog.setLayout(new BorderLayout());

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Point newLoc = new Point((int) screenSize.getWidth() - 200, (int) screenSize.getHeight() - 150);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.white);
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        // Add Header Label
        mainPanel.add(headerLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        // Add Message Label
        final JScrollPane messageScroller = new JScrollPane(messageLabel);
        mainPanel.add(messageScroller, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        messageScroller.setBackground(Color.white);
        messageScroller.setForeground(Color.white);
        messageLabel.setBackground(Color.white);
        messageScroller.getViewport().setBackground(Color.white);

        // JButton okButton = new JButton("Ok");
        mainPanel.setPreferredSize(new Dimension(200, 150));

        headerLabel.setTitle("Spark"); // NORES
        headerLabel.setTitleFont(new Font("Dialog", Font.BOLD, 10));

        messageLabel.setFont(new Font("Dialog", Font.PLAIN, 11));

        notificationDialog.add(mainPanel, BorderLayout.CENTER);
        notificationDialog.pack();
        notificationDialog.setPreferredSize(new Dimension(200, 150));
        notificationDialog.setLocation(newLoc);
        messageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                notificationDialog.setVisible(false);
                showMainWindow();
            }
        });

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                notificationDialog.setVisible(false);
                showMainWindow();
            }
        });
    }

    private void setTrayIcon(ImageIcon icon) {
        trayIcon.setImage(new ImageIcon(icon.getImage()).getImage());
    }

}
