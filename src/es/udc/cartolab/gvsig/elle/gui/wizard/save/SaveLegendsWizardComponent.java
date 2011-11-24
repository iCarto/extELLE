/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coruña
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
package es.udc.cartolab.gvsig.elle.gui.wizard.save;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.XMLEntity;

import es.udc.cartolab.gvsig.elle.gui.EllePreferencesPage;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.utils.AbstractLegendsManager;
import es.udc.cartolab.gvsig.elle.utils.DBLegendsManager;
import es.udc.cartolab.gvsig.elle.utils.FileLegendsManager;
import es.udc.cartolab.gvsig.elle.utils.LoadLegend;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class SaveLegendsWizardComponent extends WizardComponent {

	public final static String PROPERTY_SAVE_OVERVIEW = "save_overview";
	public final static String PROPERTY_CREATE_TABLES_QUESTION = "create_tables_q";


	private List<String> types;

	private JRadioButton noLegendRB, databaseRB, fileRB;
	private JPanel dbPanel;
	private JPanel filePanel;
	private JTextField dbStyles, fileStyles;
	private JTable table;
	private JCheckBox overviewCHB;
	private JComboBox overviewCB;
	private List<LayerProperties> layers;

	private String legendDir = null;

	public SaveLegendsWizardComponent(Map<String, Object> properties) {
		super(properties);


		//get config
		types = LoadLegend.getSortedPreferedLegendTypes();

		XMLEntity xml = PluginServices.getPluginServices("es.udc.cartolab.gvsig.elle").getPersistentXML();
		if (xml.contains(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME)) {
			legendDir = xml.getStringProperty(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME);
		}

		//init components
		noLegendRB = new JRadioButton(PluginServices.getText(this, "dont_save"));
		databaseRB = new JRadioButton(PluginServices.getText(this, "save_to_db"));
		fileRB = new JRadioButton(PluginServices.getText(this, "save_to_disk"));
		dbPanel = getDBPanel();
		filePanel = getFilePanel();
		setTable();
		JPanel optionsPanel = getOptionsPanel();

		//place components
		setLayout(new MigLayout("",
				"10[grow]",
		"[grow][]"));
		add(new JScrollPane(table), "shrink, growx, growy, wrap");
		add(optionsPanel, "shrink, growx, growy");

	}


	private JPanel getOptionsPanel() {

		JPanel panelOptions = new JPanel();
		panelOptions.setLayout(new MigLayout("",
				"[grow]80",
		"[][]15[][]15[][]"));


		panelOptions.add(getOverviewPanel(), "shrink, growx, growy, wrap");


		noLegendRB.addActionListener(new ActionListener() {


			public void actionPerformed(ActionEvent e) {
				dbSetEnabled(false);
				fileSetEnabled(false);
			}

		});
		panelOptions.add(noLegendRB, "wrap");


		databaseRB.setEnabled(DBSession.getCurrentSession() != null);
		databaseRB.addActionListener(new ActionListener() {


			public void actionPerformed(ActionEvent e) {
				dbSetEnabled(true);
				fileSetEnabled(false);
			}

		});
		panelOptions.add(databaseRB, "shrink, growx, growy, wrap");
		panelOptions.add(dbPanel, "shrink, growx, growy, wrap");

		fileRB.addActionListener(new ActionListener() {


			public void actionPerformed(ActionEvent e) {
				dbSetEnabled(false);
				fileSetEnabled(true);
			}

		});
		panelOptions.add(fileRB, "wrap");
		panelOptions.add(filePanel, "shrink, growx, growy, wrap");

		ButtonGroup group = new ButtonGroup();
		group.add(noLegendRB);
		group.add(databaseRB);
		group.add(fileRB);

		databaseRB.setSelected(true);

		return panelOptions;
	}

	private JPanel getOverviewPanel() {
		JPanel overviewPanel = new JPanel();
		overviewPanel.setLayout(new MigLayout("",
				"[grow, left][][shrink, right]",
		"[]"));

		overviewCHB = new JCheckBox(PluginServices.getText(this, "save_overview_legends"));
		overviewCHB.setSelected(true);
		overviewCHB.addActionListener(new ActionListener() {


			public void actionPerformed(ActionEvent e) {
				overviewCB.setEnabled(overviewCHB.isSelected());
			}

		});
		overviewPanel.add(overviewCHB);
		overviewPanel.add(new JLabel("Formato"));

		overviewCB = new JComboBox();
		for (String type : types) {
			overviewCB.addItem(type);
		}
		if (types.size() > 0) {
			overviewCB.setSelectedIndex(0);
		}
		overviewPanel.add(overviewCB, "shrink, wrap");

		return overviewPanel;

	}

	private void setTable() {
		String[] header = {"",
				PluginServices.getText(this, "name"),
				PluginServices.getText(this, "type")
		};
		DefaultTableModel model = new LegendTableModel();
		for (String h : header) {
			model.addColumn(h);
		}

		table = new JTable();
		table.setModel(model);

		TableColumn col = table.getColumnModel().getColumn(2);

		JComboBox cbox = new JComboBox();
		for (String type : types) {
			cbox.addItem(type);
		}
		col.setCellEditor(new DefaultCellEditor(cbox));

		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.getColumnModel().getColumn(2).setMaxWidth(60);

	}


	private void dbSetEnabled(boolean enabled) {
		if (dbStyles != null) {
			dbStyles.setEnabled(enabled);
		}
	}

	private void fileSetEnabled(boolean enabled) {
		if (fileStyles != null) {
			fileStyles.setEnabled(enabled);
		}
	}

	private JPanel getDBPanel() {

		JPanel panel = new JPanel();
		MigLayout layout = new MigLayout("inset 0, align center",
				"10[][grow, right]10",
		"5[grow]5");
		panel.setLayout(layout);

		if (DBSession.getCurrentSession() != null) {

			JLabel label = new JLabel(PluginServices.getText(this, "legends_group_name"));
			label.setEnabled(DBSession.getCurrentSession() != null);
			dbStyles = new JTextField("", 20);

			panel.add(label);
			panel.add(dbStyles, "shrink, right, wrap");

		} else {
			panel.add(new JLabel(PluginServices.getText(this, "notConnectedError")));
		}

		return panel;
	}

	private JPanel getFilePanel() {

		JPanel panel = new JPanel();
		MigLayout layout = new MigLayout("inset 0, align center",
				"10[][grow, right]10",
		"5[grow]5");
		panel.setLayout(layout);

		fileStyles = new JTextField("", 20);
		if (legendDir != null) {
			panel.add(new JLabel(PluginServices.getText(this, "legend")));
			panel.add(fileStyles, "shrink, right, wrap");
		} else {
			panel.add(new JLabel(PluginServices.getText(this, "no_dir_config")), "span 2");
			fileRB.setEnabled(false);
		}

		return panel;

	}

	public boolean canFinish() {
		return true;
	}

	public boolean canNext() {
		return true;
	}

	private boolean useNotGVL() {
		Object[] options = {PluginServices.getText(this, "ok"),
				PluginServices.getText(this, "cancel")};
		int n = JOptionPane.showOptionDialog(this,
				PluginServices.getText(this, "legend_format_question"),
				null,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options,
				options[1]);
		if (n!=0) {
			return false;
		} else {
			return true;
		}
	}

	public boolean prepare(AbstractLegendsManager legendsManager) {


		boolean question = true;
		Object aux = properties.get(PROPERTY_CREATE_TABLES_QUESTION);
		if (aux != null && aux instanceof Boolean) {
			question = (Boolean) aux;
		}

		boolean prepare = false;
		String message = legendsManager.getConfirmationMessage();
		if (message != null) {
			if (question) {
				int answer = JOptionPane.showConfirmDialog(
						this,
						message,
						"",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null);

				if (answer==0) {
					prepare = true;
				} else {
					prepare = false;
				}
			} else {
				prepare = true;
			}


			if (prepare) {
				try {
					legendsManager.prepare();
				} catch (WizardException e) {
					//TODO logger
					return false;
				}
				if (question) {
					JOptionPane.showMessageDialog(
							this,
							PluginServices.getText(this, "tables_created_correctly"),
							"",
							JOptionPane.INFORMATION_MESSAGE);
				}
				return true;
			} else {
				return false;
			}

		} else {
			return true;
		}
	}

	private boolean overwriteLegends(AbstractLegendsManager legendsManager) {
		if (legendsManager.exists()) {
			Object[] options = {PluginServices.getText(this, "ok"),
					PluginServices.getText(this, "cancel")};
			String message = PluginServices.getText(this, "overwrite_legend_question");
			int n = JOptionPane.showOptionDialog(this,
					String.format(message, dbStyles.getText()),
					PluginServices.getText(this, "overwrite_legend"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					options,
					options[1]);
			if (n!=0) {
				return false;
			} else {
				try {
					LoadLegend.deleteLegends(dbStyles.getText());
				} catch (SQLException e) {
					try {
						DBSession.reconnect();
					} catch (DBException e1) {
						//TODO logger
						return false;
					}
				}
				return true;
			}
		} else {
			return true;
		}
	}

	public void finish() throws WizardException {

		if (!noLegendRB.isSelected()) {

			AbstractLegendsManager legendsManager;
			if (databaseRB.isSelected()) {
				String name = dbStyles.getText().trim();
				if (!name.equals("")) {
					legendsManager = new DBLegendsManager(name);
				} else {
					throw new WizardException(PluginServices.getText(this,"empty_legend_field"), false);
				}
			} else {
				String name = fileStyles.getText().trim();
				if (!name.equals("")) {
					legendsManager = new FileLegendsManager(name);
				} else {
					throw new WizardException(PluginServices.getText(this, "empty_legend_field"), false);
				}
			}
			DefaultTableModel model = (DefaultTableModel) table.getModel();

			boolean useNotGvl = false;
			for (int i = 0; i<model.getRowCount(); i++) {
				String type = model.getValueAt(i, 2).toString().toLowerCase();
				boolean save = (Boolean) model.getValueAt(i, 0);
				if (save) {
					LayerProperties lp = layers.get(i);
					lp.setLegendType(type);
					legendsManager.addLayer(layers.get(i));
					if (!type.equals("gvl")) {
						useNotGvl = true;
						break;
					}
				}
			}

			if (overviewCHB.isSelected()) {
				Object aux = properties.get(SaveMapWizard.PROPERTY_VIEW);
				if (aux != null && aux instanceof View) {
					FLayers ovLayers = ((View) aux).getMapOverview().getMapContext().getLayers();
					legendsManager.addOverviewLayers(ovLayers);

					if (!overviewCB.getSelectedItem().toString().toLowerCase().equals("gvl")) {
						useNotGvl = true;
					}
				}

			}

			boolean cont = true;
			if (useNotGvl) {
				cont = useNotGVL();
			}

			if (cont) {
				cont = prepare(legendsManager);
			}

			if (cont) {
				cont = overwriteLegends(legendsManager);
			}

			if (cont) {
				legendsManager.saveLegends();
				if (overviewCHB.isSelected()) {
					legendsManager.saveOverviewLegends(overviewCB.getSelectedItem().toString());
				}
			} else {
				throw new WizardException("", false, false);
			}

		}

	}

	public String getWizardComponentName() {
		return "save_legends";
	}


	public void setProperties() {
		// Nothing to do

	}


	public void showComponent() throws WizardException {

		Object aux = properties.get(SaveMapWizardComponent.PROPERTY_LAYERS_MAP);
		if (aux != null && aux instanceof List<?>) {
			layers = (List<LayerProperties>) aux;
		} else {
			throw new WizardException(PluginServices.getText(this, "no_layer_list_error"));
		}

		//table
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);
		String type = "";
		if (types.size()>0) {
			type = types.get(0);
		}
		for (LayerProperties lp : layers) {
			Object[] row = {
					lp.save(),
					lp.getLayername(),
					type
			};
			model.addRow(row);
		}

		//checkbox
		aux = properties.get(PROPERTY_SAVE_OVERVIEW);
		if (aux != null && aux instanceof Boolean) {
			overviewCHB.setSelected((Boolean) aux);
		}
		overviewCB.setEnabled(overviewCHB.isSelected());
	}


	private class LegendTableModel extends DefaultTableModel {


		public Class<?> getColumnClass(int index) {
			if (index == 0) {
				return Boolean.class;
			} else {
				return super.getColumnClass(index);
			}
		}


		public boolean isCellEditable(int row, int column) {
			if (column == 1) {
				return false;
			}
			return true;
		}

	}


}
