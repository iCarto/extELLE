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
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.XMLEntity;

import es.udc.cartolab.gvsig.elle.gui.EllePreferencesPage;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.utils.LoadLegend;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LoadLegendWizardComponent extends WizardComponent {

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

		if (legendDir != null) {
			fileCB.removeAllItems();
			File f = new File(legendDir);
			File[] files = f.listFiles();
			for (int i=0; i<files.length; i++) {
				if (files[i].isDirectory() && !files[i].isHidden()) {
					fileCB.addItem(files[i].getName());
				}
			}
			panel.add(new JLabel(PluginServices.getText(this, "legends_group_name")));
			panel.add(fileCB, "wrap");
		} else {
			fileRB.setEnabled(false);
			panel.add(new JLabel(PluginServices.getText(this, "no_dir_config")), "span 2");
		}

		return panel;

	}

	@Override
	public boolean canFinish() {
		return true;
	}

	@Override
	public boolean canNext() {
		return true;
	}

	@Override
	public String getWizardComponentName() {
		return "legend_wizard_component";
	}

	@Override
	public void showComponent() {
		dbCB.removeAllItems();

		DBSession dbs = DBSession.getCurrentSession();
		if (dbs!=null) {
			try {
				if (dbs.tableExists(dbs.getSchema(), "_map_style")) {
					String[] legends = dbs.getDistinctValues("_map_style", dbs.getSchema(), "nombre_estilo", true, false);
					for (String legend : legends) {
						dbCB.addItem(legend);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				try {
					dbs = DBSession.reconnect();
				} catch (DBException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}

	private void loadDBLegend(FLyrVect layer, boolean overview) throws SQLException, IOException {

		String table;
		if (overview) {
			table = "_map_overview_style";
		} else {
			table = "_map_style";
		}

		DBSession dbs = DBSession.getCurrentSession();
		String layerName = layer.getName();
		String styleName = dbCB.getSelectedItem().toString();
		String[][] style = dbs.getTable(table, "where nombre_capa='" + layerName + "' and nombre_estilo='" + styleName + "'");
		if (style.length == 1) {
			String type = style[0][2];
			String def = style[0][3];

			File tmpLegend = File.createTempFile("style", layerName + "." + type);
			FileWriter writer = new FileWriter(tmpLegend);
			writer.write(def);
			writer.close();
			LoadLegend.setLegend(layer, tmpLegend.getAbsolutePath(), true);
		}
	}

	private void loadFileLegend(FLyrVect layer, boolean overview) {
		String stylePath;
		if (legendDir.endsWith(File.separator)) {
			stylePath = legendDir + fileCB.getSelectedItem().toString();
		} else {
			stylePath = legendDir + File.separator + fileCB.getSelectedItem().toString();
		}
		LoadLegend.setLegendPath(stylePath);
		if (overview) {
			LoadLegend.setOverviewLegend(layer);
		} else {
			LoadLegend.setLegend(layer);
		}
	}

	@Override
	public void finish() throws WizardException {
		Object aux = properties.get(LoadMapWizardComponent.PROPERTY_VEW);
		if (aux!=null && aux instanceof View) {
			View view = (View) aux;

			if ((databaseRB.isSelected() && dbCB.getSelectedItem()!=null) || (fileRB.isSelected() && fileCB.getSelectedItem()!=null)) {
				try {
					FLayers layers = view.getMapControl().getMapContext().getLayers();
					loadLegends(layers, false);
					layers = view.getMapOverview().getMapContext().getLayers();
					loadLegends(layers, true);
				} catch (Exception e) {
					throw new WizardException(e);
				}
			}
		} else {
			throw new WizardException(PluginServices.getText(this, "no_view_error"));
		}
	}

	private void loadLegends(FLayers layers, boolean overview) throws SQLException, IOException {
		for (int i=0; i<layers.getLayersCount(); i++) {
			FLayer layer = layers.getLayer(i);
			if (layer instanceof FLyrVect) {
				if (databaseRB.isSelected()) {
					loadDBLegend((FLyrVect) layer, overview);
				} else {
					loadFileLegend((FLyrVect) layer, overview);
				}
			} else if (layer instanceof FLayers) {
				loadLegends((FLayers) layer, overview);
			}
		}
	}

	@Override
	public void setProperties() {
		// Nothing to do
	}



}
