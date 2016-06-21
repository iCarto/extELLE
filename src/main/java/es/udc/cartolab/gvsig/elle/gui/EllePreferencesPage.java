/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coru�a
 * 
 * This file is part of ELLE
 * 
 * ELLE is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 * 
 * ELLE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with ELLE.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package es.udc.cartolab.gvsig.elle.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.andami.Launcher;
import org.gvsig.andami.PluginServices;
import org.gvsig.andami.preferences.AbstractPreferencePage;
import org.gvsig.andami.preferences.StoreException;
import org.gvsig.utils.XMLEntity;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

public class EllePreferencesPage extends AbstractPreferencePage implements ActionListener {

    /* key names */
    public static final String DEFAULT_LEGEND_DIR_KEY_NAME = "LegendDir";
    public static final String DEFAULT_LEGEND_FILE_TYPE_KEY_NAME = "LegendFileType";

    /* default values */
    private static final String DEFAULT_LEGEND_DIR = Launcher.getAppHomeDir();
    public static final String DEFAULT_LEGEND_FILE_TYPE = "gvl";

    protected String id;
    private ImageIcon icon;
    private boolean panelStarted = false;
    private JTextField legendDirField;
    private JButton legendDirButton;
    private JComboBox legendTypeCBox;
    private String title = "ELLE";

    public EllePreferencesPage() {
	super();
	id = this.getClass().getName();
	icon = new ImageIcon(this.getClass().getClassLoader().getResource("images/logo.png"));
	panelStarted = false;
    }

    public void setChangesApplied() {
	setChanged(false);
    }

    public void storeValues() throws StoreException {
	PluginServices ps = PluginServices.getPluginServices(this);
	XMLEntity xml = ps.getPersistentXML();

	storeDir(xml);
	storeType(xml);

    }

    private void storeDir(XMLEntity xml) throws StoreException {
	String legendDir = legendDirField.getText();
	File f = new File(legendDir);
	if (f.exists() && f.isDirectory() && f.canRead()) {
	    xml.putProperty(DEFAULT_LEGEND_DIR_KEY_NAME,
		    legendDir);
	} else {
	    String message = String.format("%s no es un directorio v�lido", legendDir);
	    throw new StoreException(message);
	}
    }

    private void storeType(XMLEntity xml) {
	String type = legendTypeCBox.getSelectedItem().toString();
	xml.putProperty(DEFAULT_LEGEND_FILE_TYPE_KEY_NAME, type);
    }


    public String getID() {
	return id;
    }


    public ImageIcon getIcon() {
	return icon;
    }


    public JPanel getPanel() {

	if (!panelStarted ) {
	    panelStarted = true;

	    InputStream stream = getClass().getClassLoader().getResourceAsStream("forms/preferences.jfrm");
	    
	    FormPanel form;
	    try {
		form = new FormPanel(stream);
		form.setFocusTraversalPolicyProvider(true);

		    JLabel legendLabel = form.getLabel("legendLabel");
		    legendLabel.setText(PluginServices.getText(this, "legend_directory"));

		    legendDirField = form.getTextField("legendField");
		    legendDirButton = (JButton) form.getButton("legendButton");
		    legendDirButton.addActionListener(this);

		    JLabel legendTypeLabel = form.getLabel("legendTypeLabel");
		    legendTypeLabel.setText(PluginServices.getText(this, "legend_type_pref"));

		    legendTypeCBox = form.getComboBox("legendTypeCB");
		    legendTypeCBox.addItem("gvl");
		    legendTypeCBox.addItem("sld");

		    addComponent(form);
	    } catch (FormException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    

	}

	return this;
    }


    public String getTitle() {
	return title;
    }


    public void initializeDefaults() {

	legendDirField.setText(DEFAULT_LEGEND_DIR);

	setCB(DEFAULT_LEGEND_FILE_TYPE);

    }

    private void setCB(String text) {
	int itemCount = legendTypeCBox.getItemCount();
	for (int i=0; i<itemCount; i++) {
	    if (text.equals(legendTypeCBox.getItemAt(i))) {
		legendTypeCBox.setSelectedIndex(i);
		break;
	    }
	}
    }


    public void initializeValues() {

	if (!panelStarted) {
	    getPanel();
	}

	PluginServices ps = PluginServices.getPluginServices(this);
	XMLEntity xml = ps.getPersistentXML();

	// Default Projection
	String legendDir = null;
	if (xml.contains(DEFAULT_LEGEND_DIR_KEY_NAME)) {
	    legendDir = xml.getStringProperty(DEFAULT_LEGEND_DIR_KEY_NAME);
	} else {
	    legendDir = DEFAULT_LEGEND_DIR;
	}

	legendDirField.setText(legendDir);

	String type = DEFAULT_LEGEND_FILE_TYPE;
	if (xml.contains(DEFAULT_LEGEND_FILE_TYPE_KEY_NAME)) {
	    type = xml.getStringProperty(DEFAULT_LEGEND_FILE_TYPE_KEY_NAME);
	}

	setCB(type.toLowerCase());

    }


    public boolean isValueChanged() {
	return super.hasChanged();
    }

    public void actionPerformed(ActionEvent event) {
	if (event.getSource()==legendDirButton) {
	    File currentDirectory = new File(legendDirField.getText());
	    JFileChooser chooser;
	    if (!(currentDirectory.exists() &&
		    currentDirectory.isDirectory() &&
		    currentDirectory.canRead())) {
		currentDirectory = new File(DEFAULT_LEGEND_DIR);
	    }
	    chooser = new JFileChooser(currentDirectory);

	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    int returnVal = chooser.showOpenDialog(legendDirField);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		legendDirField.setText(chooser.getSelectedFile().getAbsolutePath());
	    }
	}
    }

}