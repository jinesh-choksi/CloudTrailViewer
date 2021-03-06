/*    
CloudTrail Log Viewer, is a Java desktop application for reading AWS CloudTrail
logs files.

Copyright (C) 2015  Mark P. Haskins

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.haskins.jcloudtrailerviewer.components;

import com.haskins.jcloudtrailerviewer.frame.ScanChartWindow;
import com.haskins.jcloudtrailerviewer.frame.CombinedWindow;
import com.haskins.jcloudtrailerviewer.frame.ChartWindow;
import com.haskins.jcloudtrailerviewer.PropertiesSingleton;
import com.haskins.jcloudtrailerviewer.event.EventLoader;
import com.haskins.jcloudtrailerviewer.event.EventsDatabase;
import com.haskins.jcloudtrailerviewer.jCloudTrailViewer;
import com.haskins.jcloudtrailerviewer.model.ChartData;
import com.haskins.jcloudtrailerviewer.model.MenuDefinition;
import com.haskins.jcloudtrailerviewer.model.MenusDefinition;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Provides the Menu that is attached to the JDesktop
 * 
 * @author mark.haskins
 */
public class MainMenu extends JMenuBar implements ActionListener, KeyListener {
    
    private final JFileChooser fileChooser = new JFileChooser();
    
    private final EventsDatabase eventsDatabase;
    private final EventLoader eventLoader;
    
    private final Map<String, JMenu> menusMap = new HashMap<>();
    
    private final JTextField menuScanTextField = new JTextField();
        
    /**
     * Default Constructor.
     * 
     * Takes an event loader and an event database
     * 
     * @param eventLoader an instance of EventLoader 
     * @param database and instance of EventsDatase
     */
    public MainMenu(EventLoader eventLoader, EventsDatabase database) {
        
        this.eventLoader = eventLoader;
        eventsDatabase = database;
          
        buildMenu();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // ActionListener
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void actionPerformed(ActionEvent e) {
        
        String actionCommand = e.getActionCommand();
        
        switch(actionCommand) {
            case "LoadLocal":
                loadFiles();
                break;
            case "LoadS3":
                loadS3Files();
                break;
            case "EventsByService":
                showEventsByServiceChart();
                break;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // KeyListener
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ENTER && menuScanTextField.getText().trim().length() > 0) {
            
            MenuDefinition def = new MenuDefinition();
            def.setName(menuScanTextField.getText());
            def.setContains("FreeformFilter:" + menuScanTextField.getText().trim());
            
            JInternalFrame panel = new CombinedWindow(def.getName(), null, def);
            panel.setVisible(true);

            jCloudTrailViewer.DESKTOP.add(panel);

            try {
                panel.setSelected(true);
            }
            catch (java.beans.PropertyVetoException pve) {
            }
            
            menuScanTextField.setText("");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }
    
    @Override
    public void keyTyped(KeyEvent e) { }
        
    ////////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////////
    private void buildMenu() {
        
        fileChooser.setMultiSelectionEnabled(true);
        
        menuScanTextField.addKeyListener(this);
        
        // -- Menu : File
        JMenu menuFile = new JMenu("File");
        JMenuItem exit = new JMenuItem(new AbstractAction("Exit") {
            
            @Override
            public void actionPerformed(ActionEvent t) {
                
                System.exit(0);
            }
        });
        
        menuFile.add(exit);
        
        
        // -- Menu : Events
        JMenu menuEvents = new JMenu("Events");
        
        JMenuItem loadLocal = new JMenuItem("Load Local Files");
        loadLocal.setActionCommand("LoadLocal");
        loadLocal.addActionListener(this);
        
        JMenuItem loadS3 = new JMenuItem("Load S3 Files");
        loadS3.setActionCommand("LoadS3");
        loadS3.addActionListener(this);
        
        if (!PropertiesSingleton.getInstance().validS3Credentials()) {
            loadS3.setEnabled(false);
        }
        
        JMenuItem clearDatabase = new JMenuItem(new AbstractAction("Clear Events") {
            
            @Override
            public void actionPerformed(ActionEvent t) {
                eventsDatabase.clear();
            }
        });
        
        menuEvents.add(loadLocal);
        menuEvents.add(loadS3);
        menuEvents.addSeparator();
        menuEvents.add(clearDatabase);
        
        
        // -- Menu : Services
        JMenu menuServices = new JMenu("Services");
        
        JMenuItem eventsByService = new JMenuItem("Events by Service");
        eventsByService.setActionCommand("EventsByService");
        eventsByService.addActionListener(this);
        
        menuServices.add(eventsByService);
        
        
        // -- Menu : Scan
        JPanel scanSearchPanel = new JPanel();
        scanSearchPanel.setBackground(Color.WHITE);
        scanSearchPanel.setLayout(new BorderLayout());
        
        JLabel iconLabel = new JLabel();
        try{
            iconLabel.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("icons/Search.png")));
        } catch (Exception e)
        { }
        
        scanSearchPanel.add(iconLabel, BorderLayout.WEST);
        scanSearchPanel.add(menuScanTextField, BorderLayout.CENTER);
        
        JMenu menuScan = new JMenu("Scan");
        menuScan.add(scanSearchPanel);
        menuScan.addSeparator();
        menuScan.addMenuListener(new MenuListener(){
            @Override
            public void menuCanceled(MenuEvent e) {}
            @Override
            public void menuDeselected(MenuEvent e) {}
            @Override
            public void menuSelected(MenuEvent e) {
                EventQueue.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        menuScanTextField.grabFocus();
                    }
                });
            }
        });
        
        menusMap.put("Scan", menuScan);
        
        // -- Menu : About
        JMenu menuAbout = new JMenu("Help");
        
        JMenuItem about = new JMenuItem("Version " + jCloudTrailViewer.VERSION);
        
        menuAbout.add(about);
        
        this.add(menuFile);
        this.add(menuEvents);
        this.add(menuServices);
        this.add(menuScan);
        
        createMenusFromFile();
        
        this.add(menuAbout);
    }
    
    private void loadFiles() {
        eventLoader.showFileBrowser();
    }
    
    private void loadS3Files() {
        eventLoader.showS3Browser();
    }
    
    private void showEventsByServiceChart() {
        
        if (eventsDatabase.getEventsPerService().size() > 0) {
            
            ChartData chartData = new ChartData();
            chartData.setChartStyle("Bar");
            chartData.setChartSource("EventSource");
            chartData.setTop(5);

            ChartWindow chart = new ChartWindow(chartData, eventsDatabase.getEvents());
            chart.setVisible(true);

            jCloudTrailViewer.DESKTOP.add(chart);

            try {
                chart.setSelected(true);
            }
            catch (java.beans.PropertyVetoException pve) {
            }
           
        }
        else {

            JOptionPane.showMessageDialog(
                jCloudTrailViewer.DESKTOP,
                "No Events Loaded!",
                "Data Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }
        
    private void createMenusFromFile() {
        
        ObjectMapper mapper = new ObjectMapper();
        
        MenusDefinition menus = null;
        
        try {
            
            String path = "./config/features.json";
            menus = mapper.readValue(new File(path), MenusDefinition.class);

        } catch (Exception e1) {
            
            Logger.getLogger(MainMenu.class.getName()).log(Level.WARNING, "Couldn't load features");
                        
            try {
                
                /**
                 * This is here so I can find the file when running with within netbeans
                 */
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                File file = new File(classloader.getResource("config/features.json").getFile());

                menus = mapper.readValue(file, MenusDefinition.class);
                
            } catch (Exception e2) {
                Logger.getLogger(PropertiesSingleton.class.getName()).log(Level.WARNING, "Still no features file found");
            }
        }  
        
        if (menus != null) {
            createMenusFromModels(menus);
        }
    }
      
    private JMenu getMenu(String name, JMenu parent) {

        JMenu menu = null;
        
        int numMenus=parent.getItemCount();
        for (int i=0; i<numMenus; i++) {
            
            JMenuItem tmpMenu = parent.getItem(i);
            if (tmpMenu instanceof JMenu) {
       
                if (tmpMenu.getName() != null) {
                    String tmpName = tmpMenu.getName();
                    if (tmpName.equalsIgnoreCase(name)) {

                        menu = (JMenu)tmpMenu;
                        break;
                    }
                }
            }
        }
        
        return menu;
    }
    
    private void createMenusFromModels(MenusDefinition menus) {
            
        List<MenuDefinition> definitions = menus.getMenus();
        for (final MenuDefinition def : definitions) {
            
            boolean newTopLevelMenu = false;
            
            String menuName = def.getMenu();
            if (!menusMap.containsKey(menuName)) {   
                menusMap.put(menuName, new JMenu(menuName));
                newTopLevelMenu = true;
            }
            
            JMenuItem menuItem = new JMenuItem(new AbstractAction(def.getName()) {

                @Override
                public void actionPerformed(ActionEvent t) {
                    
                    JInternalFrame panel = null;
                    
                    if ( (def.getActions() != null && def.getActions().size() > 0) ||
                         (def.getContains() != null && def.getContains().length() > 0) ) {
                        
                        panel = new CombinedWindow(def.getName(), null, def);
                        
                    } else if (def.getProperty() != null && def.getProperty().length() > 0) {
                        
                        panel = new ScanChartWindow(def);
                    }
                    
                    if (panel != null) {
                        
                        panel.setVisible(true);

                        jCloudTrailViewer.DESKTOP.add(panel);

                        try {
                            panel.setSelected(true);
                        }
                        catch (java.beans.PropertyVetoException pve) {
                        }
                    }
                }
            });
            
            JMenu parentMenu = menusMap.get(menuName);
            
            if (def.getSubMenu() != null && def.getSubMenu().length() > 0) {
                
                JMenu subMenu = getMenu(def.getSubMenu(), parentMenu);
                if (subMenu == null) {
                    
                    subMenu = new JMenu(def.getSubMenu());
                    subMenu.add(menuItem);
                    
                    parentMenu.add(subMenu);
                }
                
            } else {
                parentMenu.add(menuItem);
            }
                        
            if (newTopLevelMenu) {
                this.add(parentMenu);
            }
                        
        }
    }
}
