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
package es.udc.cartolab.gvsig.elle.gui.wizard.load;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.gvsig.andami.PluginServices;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.utils.XMLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.EllePreferencesPage;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.utils.LoadLegend;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LoadLegendWizardComponent extends WizardComponent {
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(LoadLegendWizardComponent.class);

    private JRadioButton noLegendRB, databaseRB, fileRB;
    private JPanel dbPanel;
    private JPanel filePanel;
    private JComboBox dbCB, fileCB;

    private String legendDir = null;

    public LoadLegendWizardComponent(Map<String, Object> properties) {
	super(properties);

	//get config
	XMLEntity xml = PluginServices.getPluginServices("es.udc.cartolab.gvsig.elle").getPersistentXML();
	if (xml.contains(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME)) {
	    legendDir = xml.getStringProperty(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME);
	}


	//init components
	noLegendRB = new JRadioButton(PluginServices.getText(this, "dont_load"));
	databaseRB = new JRadioButton(PluginServices.getText(this, "load_from_db"));
	fileRB = new JRadioButton(PluginServices.getText(this, "load_from_disk"));
	dbCB = new JComboBox();
	fileCB = new JComboBox();
	dbPanel = getDBPanel();
	filePanel = getFilePanel();
	ButtonGroup group = new ButtonGroup();
	group.add(noLegendRB);
	group.add(databaseRB);
	group.add(fileRB);


	//components placement
	setLayout(new MigLayout("inset 0, align center",
		"20[grow]",
		"[]15[][]15[][]"));
	add(noLegendRB, "wrap");
	add(databaseRB, "wrap");
	add(dbPanel, "shrink, growx, growy, wrap");
	add(fileRB, "wrap");
	add(filePanel, "shrink, growx, growy, wrap");



	//listeners
	noLegendRB.addActionListener(new ActionListener() {


	    public void actionPerformed(ActionEvent e) {
		dbSetEnabled(false);
		fileSetEnabled(false);
	    }

	});

	databaseRB.addActionListener(new ActionListener() {


	    public void actionPerformed(ActionEvent e) {
		dbSetEnabled(true);
		fileSetEnabled(false);
	    }

	});

	fileRB.addActionListener(new ActionListener() {


	    public void actionPerformed(ActionEvent e) {
		dbSetEnabled(false);
		fileSetEnabled(true);
	    }

	});

	//initial values
	noLegendRB.setSelected(true);
	dbSetEnabled(false);
	fileSetEnabled(false);


    }

    private void dbSetEnabled(boolean enabled) {
	dbCB.setEnabled(enabled);
    }

    private void fileSetEnabled(boolean enabled) {
	fileCB.setEnabled(enabled);
    }

    private JPanel getDBPanel() {

	JPanel panel = new JPanel();
	MigLayout layout = new MigLayout("inset 0, align center",
		"10[grow][]50",
		"5[grow]5");
	panel.setLayout(layout);

	if (DBSession.getCurrentSession()!=null) {
	    dbCB.removeAllItems();
	    JLabel label = new JLabel(PluginServices.getText(this, "legends_group_name"));
	    label.setEnabled(DBSession.getCurrentSession() != null);
	    panel.add(label);
	    panel.add(dbCB, "wrap");
	} else {
	    panel.add(new JLabel(PluginServices.getText(this, "notConnectedError")));
	    databaseRB.setEnabled(false);
	}


	return panel;
    }

    private JPanel getFilePanel() {

	JPanel panel = new JPanel();
	MigLayout layout = new MigLayout("inset 0, align center",
		"10[grow][]50",
		"5[grow]5");
	panel.setLayout(layout);

	boolean panelAdded = false;
	if (legendDir != null) {
	    File f = new File(legendDir);
	    if (f.isDirectory()) {
		fileCB.removeAllItems();
		File[] files = f.listFiles();
		for (int i=0; i<files.length; i++) {
		    if (files[i].isDirectory() && !files[i].isHidden()) {
			fileCB.addItem(files[i].getName());
		    }
		}
		panel.add(new JLabel(PluginServices.getText(this, "legends_group_name")));
		panel.add(fileCB, "wrap");
		panelAdded = true;
	    }
	}

	if (!panelAdded) {
	    fileRB.setEnabled(false);
	    panel.add(new JLabel(PluginServices.getText(this, "no_dir_config")), "span 2");
	}

	return panel;
    }

    public boolean canFinish() {
	return true;
    }

    public boolean canNext() {
	return true;
    }

    public String getWizardComponentName() {
	return "legend_wizard_component";
    }

    public void showComponent() {
	dbCB.removeAllItems();

	DBSession dbs = DBSession.getCurrentSession();
	if (dbs!=null) {
	    try {
		if (dbs.tableExists(DBStructure.getSchema(), DBStructure.getMapStyleTable())) {
		    String[] legends = dbs.getDistinctValues(DBStructure.getMapStyleTable(), DBStructure.getSchema(), "nombre_estilo", true, false);
		    Object tmp = properties
			    .get(LoadMapWizardComponent.PROPERTY_MAP_NAME);
		    boolean exists = false;
		    String legendName = (tmp == null ? "" : tmp.toString());
		    for (String legend : legends) {
			dbCB.addItem(legend);
			if (legendName.equals(legend)) {
			    exists = true;
			}
		    }
		    if (exists) {
			dbCB.setSelectedItem(legendName);
			databaseRB.setSelected(true);
			dbSetEnabled(true);
		    }

		}
	    } catch (SQLException e) {
		try {
		    dbs = DBSession.reconnect();
		} catch (DataException e1) {
			logger.error(e1.getMessage(), e1);
		}
		logger.error(e.getMessage(), e);
	    }
	}
    }





    public void finish() throws WizardException {
	Object aux = properties.get(LoadMapWizardComponent.PROPERTY_VEW);
	if (aux!=null && aux instanceof IView) {
	    IView view = (IView) aux;

	    if ((databaseRB.isSelected() && dbCB.getSelectedItem()!=null) || (fileRB.isSelected() && fileCB.getSelectedItem()!=null)) {
		
		    FLayers layers = view.getMapControl().getMapContext().getLayers();
		    try {
			loadLegends(layers, false);
			layers = view.getMapOverview().getMapContext().getLayers();
			loadLegends(layers, true);
		    } catch (SQLException e) {
			throw new WizardException(e);
		    } catch (IOException e) {
			throw new WizardException(e);
		    }
		
	    }
	} else {
	    throw new WizardException(PluginServices.getText(this, "no_view_error"));
	}
    }

    private void loadLegends(FLayers layers, boolean overview) throws SQLException, IOException {
    	LoadLegend legendLoader = new LoadLegend();
	for (int i=0; i<layers.getLayersCount(); i++) {
	    FLayer layer = layers.getLayer(i);
	    if (layer instanceof FLyrVect) {
		int source;
		String styles;
		if (databaseRB.isSelected()) {
		    source = LoadLegend.DB_LEGEND;
		    styles = dbCB.getSelectedItem().toString();
		    legendLoader.loadDBLegend((FLyrVect) layer, styles, overview);
		} else {
		    source = LoadLegend.FILE_LEGEND;
		    styles = fileCB.getSelectedItem().toString();
//		    LoadLegend.loadLegend((FLyrVect) layer, styles, overview, source);
		}
		
	    } else if (layer instanceof FLayers) {
		loadLegends((FLayers) layer, overview);
	    }
	}
    }

    public void setProperties() {
	// Nothing to do
    }



}
