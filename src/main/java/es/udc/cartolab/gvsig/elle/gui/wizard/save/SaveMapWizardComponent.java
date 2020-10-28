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
package es.udc.cartolab.gvsig.elle.gui.wizard.save;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.gvsig.andami.ui.mdiManager.MDIManagerFactory;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.store.jdbc.JDBCStoreParameters;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.utils.MapDAO;
import es.udc.cartolab.gvsig.users.utils.DBSession;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class SaveMapWizardComponent extends WizardComponent implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(SaveMapWizardComponent.class);
	public static final String PROPERTY_LAYERS_MAP = "table_layers";
	public static final String PROPERTY_MAP_NAME = "property_map_name";

	private JButton upButton;
	private JButton downButton;
	private JTextField mapNameField;
	private JCheckBox overviewChb;
	private JTable mapTable;

	private List<LayerProperties> mapLayers;

	private IView view;

	public SaveMapWizardComponent(Map<String, Object> properties) {
		super(properties);

		this.mapLayers = new ArrayList<>();

		// layout
		final MigLayout layout = new MigLayout("inset 0, align center", "10[grow]10", "10[grow]");
		setLayout(layout);

		add(getMainPanel(), "shrink, growx, growy, wrap");
	}

	private JPanel getMainPanel() {
		final JPanel panel = new JPanel();
		final MigLayout layout = new MigLayout("inset 0, align center", "[grow][grow]", "[grow][]");
		panel.setLayout(layout);

		// map, up & down buttons
		setMapTable();
		final JPanel tablePanel = new JPanel();
		final MigLayout tableLayout = new MigLayout("inset 0, align center", "[grow][]", "[grow]");
		tablePanel.setLayout(tableLayout);
		tablePanel.add(new JScrollPane(this.mapTable), "growx, growy");
		tablePanel.add(getUpDownPanel(), "shrink, align right, wrap");

		// map overview
		this.overviewChb = new JCheckBox(_("save_overview"));
		this.overviewChb.setSelected(true);

		// map name
		final JPanel namePanel = new JPanel();
		namePanel.add(new JLabel(_("map_name")));
		this.mapNameField = new JTextField("", 20);

		this.mapNameField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				callStateChanged();
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

		});
		namePanel.add(this.mapNameField);

		// add to panel
		panel.add(tablePanel, "span 2 1, grow, wrap");
		panel.add(this.overviewChb, "grow 0, align left");
		panel.add(namePanel, "grow 0, align right, wrap");

		return panel;
	}

	private JPanel getUpDownPanel() {

		final JPanel upDownPanel = new JPanel();
		final MigLayout layout = new MigLayout("inset 0, align center", "[]", "[grow]");

		upDownPanel.setLayout(layout);
		java.net.URL imgURL = getClass().getClassLoader().getResource("images/go-up.png");
		this.upButton = new JButton(new ImageIcon(imgURL));
		this.upButton.addActionListener(this);

		imgURL = getClass().getClassLoader().getResource("images/go-down.png");
		this.downButton = new JButton(new ImageIcon(imgURL));
		this.downButton.addActionListener(this);

		upDownPanel.add(this.upButton, "shrink, wrap");
		upDownPanel.add(this.downButton, "shrink");

		return upDownPanel;
	}

	private void setMapTable() {

		final String[] header = { "", _("layer"), _("visible"), _("max_scale"), _("min_scale") };
		final DefaultTableModel model = new MapTableModel();
		for (final String columnName : header) {
			model.addColumn(columnName);
		}
		model.setRowCount(0);
		this.mapTable = new JTable();
		this.mapTable.setModel(model);

		this.mapTable.getColumnModel().getColumn(0).setMaxWidth(30);
		this.mapTable.getColumnModel().getColumn(1).setMinWidth(120);
		this.mapTable.getColumnModel().getColumn(2).setMinWidth(40);
		this.mapTable.getColumnModel().getColumn(3).setMinWidth(60);
		this.mapTable.getColumnModel().getColumn(4).setMinWidth(60);
	}

	@Override
	public boolean canFinish() {
		return canNext();
	}

	@Override
	public boolean canNext() {
		final String mapname = this.mapNameField.getText();
		return mapname != null && !mapname.equals("");
	}

	@Override
	public String getWizardComponentName() {
		return "save_map";
	}

	private List<String> parse() {

		if (this.mapTable.isEditing()) {
			if (this.mapTable.getCellEditor() != null) {
				this.mapTable.getCellEditor().stopCellEditing();
			}
		}

		final String emptyNameError = _("error_empty_layer_name");
		final String parseError = _("error_numeric_scale");
		final String minGreaterError = _("error_min_greater_than_max");

		final List<String> errors = new ArrayList<>();
		int position = 1;
		final MapTableModel model = (MapTableModel) this.mapTable.getModel();
		final List<String> layerNames = new ArrayList<>();

		for (int i = model.getRowCount() - 1; i >= 0; i--) {

			final String name = model.getValueAt(i, 1).toString();
			if (name.equals("")) {
				if (!errors.contains(emptyNameError)) {
					errors.add(emptyNameError);
				}
			}

			if (!layerNames.contains(name)) {

				layerNames.add(name);

				final boolean save = (Boolean) model.getValueAt(i, 0);

				final boolean visible = (Boolean) model.getValueAt(i, 2);
				Double maxScale = null, minScale = null;
				Object aux;
				try {
					aux = model.getValueAt(i, 3);
					if (aux != null) {
						final String str = aux.toString();
						if (!str.equals("")) {
							maxScale = NumberFormat.getInstance().parse(str).doubleValue();
						}
					}
				} catch (final ParseException e) {
					if (!errors.contains(parseError)) {
						errors.add(parseError);
					}
				}
				try {
					aux = model.getValueAt(i, 4);
					if (aux != null) {
						final String str = aux.toString();
						if (!str.equals("")) {
							minScale = NumberFormat.getInstance().parse(str).doubleValue();
						}
					}
				} catch (final ParseException e) {
					if (!errors.contains(parseError)) {
						errors.add(parseError);
					}
				}
				if (minScale != null && maxScale != null && minScale > maxScale) {
					if (!errors.contains(minGreaterError)) {
						errors.add(minGreaterError);
					}
				}

				final LayerProperties lp = this.mapLayers.get(i);
				lp.setPosition(position);
				lp.setVisible(visible);
				if (maxScale != null) {
					lp.setMaxScale(maxScale);
				}
				if (minScale != null) {
					lp.setMinScale(minScale);
				}
				lp.setSave(save);

				position++;
			} else {
				errors.add(_("error_repeated_layer_name", name));
			}
		}

		return errors;
	}

	@Override
	public void setProperties() throws WizardException {
		final List<String> errors = parse();
		if (errors.size() > 0) {
			String msg = _("errors_list");
			for (final String error : errors) {
				msg = msg + "\n" + error;
			}
			throw new WizardException(msg);
		}
		this.properties.put(SaveMapWizardComponent.PROPERTY_MAP_NAME, this.mapNameField.getText().trim());
		this.properties.put(SaveMapWizardComponent.PROPERTY_LAYERS_MAP, this.mapLayers);
		this.properties.put(SaveLegendsWizardComponent.PROPERTY_SAVE_OVERVIEW, this.overviewChb.isSelected());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void showComponent() throws WizardException {
		final DefaultTableModel model = (DefaultTableModel) this.mapTable.getModel();
		model.setRowCount(0);
		Object aux = this.properties.get(PROPERTY_LAYERS_MAP);
		if (aux == null) {
			aux = this.properties.get(SaveMapWizard.PROPERTY_VIEW);
			if (aux != null && aux instanceof IView) {
				this.view = (IView) aux;
				createMapLayerList(this.view.getMapControl().getMapContext().getLayers());
			} else {
				throw new WizardException(_("no_view_error"));
			}
		} else if (aux instanceof List<?>) {
			this.mapLayers = (List<LayerProperties>) aux;
		} else {
			throw new WizardException(_("no_layer_list_error"));
		}

		for (final LayerProperties lp : this.mapLayers) {
			final double maxScale = lp.getMaxScale();
			String maxScaleStr = "";
			if (maxScale >= 0) {
				maxScaleStr = NumberFormat.getInstance().format(maxScale);
			}
			final double minScale = lp.getMinScale();
			String minScaleStr = "";
			if (minScale >= 0) {
				minScaleStr = NumberFormat.getInstance().format(minScale);
			}
			final Object[] row = { lp.save(), lp.getLayername(), lp.visible(), maxScaleStr, minScaleStr };
			model.addRow(row);

		}

		// popup menu
		final LayerListPopupMenu popupmenu = new LayerListPopupMenu(this, this.mapTable, this.mapLayers);
		this.mapTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.isMetaDown()) {
					popupmenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

		});

	}

	/*
	 * This method is used in order to retrieve the name of all the nested groups as
	 * a string, each of them separated by '/'. Therefore, we have to escape that
	 * character ('\/'), which also means duplicating the backslashes.
	 */
	private String getGroupCompositeName(FLayers group) {
		// We check whether the layer has a parent group or it doesn't.
		if (group.getName() == null || group.getName().equals("root layer") && group.getParentLayer() == null) {
			return "";
		}
		// We duplicate previously existing backslashes and escape the slashes.
		String groupName = group.getName().replace("\\", "\\\\").replace("/", "\\/");
		if (group.getParentLayer() != null) {
			final String parentName = getGroupCompositeName(group.getParentLayer());
			if (parentName.length() > 0) {
				groupName = parentName + "/" + groupName;
			}
		}
		return groupName;
	}

	private void createMapLayerList(FLayers layers) {

		for (int i = layers.getLayersCount() - 1; i >= 0; i--) {
			final FLayer layer = layers.getLayer(i);
			if (layer instanceof FLayers) {
				createMapLayerList((FLayers) layer);
			} else if (layer instanceof FLyrVect) {
				try {
					final LayerProperties lp = new LayerProperties((FLyrVect) layer);
					// layer data to fill the table
					String group = "";
					if (layer.getParentLayer() != null) {
						group = getGroupCompositeName(layer.getParentLayer());
					}
					final double maxScale = layer.getMaxScale();
					if (maxScale >= 0) {
						lp.setMaxScale(maxScale);
					}
					final double minScale = layer.getMinScale();
					if (minScale >= 0) {
						lp.setMinScale(minScale);
					}
					final boolean visible = layer.isVisible();

					lp.setVisible(visible);
					lp.setGroup(group);
					lp.setPosition(this.mapLayers.size());
					lp.setSave(true);

					this.mapLayers.add(lp);

				} catch (final WizardException e) {
					// layer is not postgis, nothing to do
				}
			}
		}
	}

	@Override
	public void finish() throws WizardException {
		DBSession dbs = DBSession.getCurrentSession();
		try {
			boolean tableMapExists = dbs.tableExists(DBStructure.getSchema(), DBStructure.getMapTable());
			final boolean tableMapOvExists = dbs.tableExists(DBStructure.getSchema(), DBStructure.getOverviewTable());

			if (!tableMapExists || !tableMapOvExists) {

				boolean canCreate = false;
				if (MapDAO.getInstance().hasSchema()) {
					canCreate = dbs.getDBUser().canCreateTable(DBStructure.getSchema());
				} else {
					canCreate = dbs.getDBUser().canCreateSchema();
					// if the user has permissions to create schema also have
					// them to create table inside it
				}

				if (!canCreate) {
					// [jestevez] I think this code is never reached due to the limitations of
					// SaveMapExtension
					throw new WizardException(_("table_map_contact_admin"));
				} else {
					final int answer = JOptionPane.showConfirmDialog(this,
							_("tables_will_be_created", DBStructure.getSchema()), "", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null);
					if (answer == 0) {
						MapDAO.getInstance().createMapTables();
						this.properties.put(SaveLegendsWizardComponent.PROPERTY_CREATE_TABLES_QUESTION, false);
						JOptionPane.showMessageDialog(this, _("tables_created_correctly"), "",
								JOptionPane.INFORMATION_MESSAGE);
						tableMapExists = true;
					}
				}
			}

			if (tableMapExists) {

				final String mapName = this.mapNameField.getText();

				final boolean mapExists = MapDAO.getInstance().mapExists(mapName);
				if (mapExists) {
					final int answer = JOptionPane.showOptionDialog(this, _("overwrite_map_question", mapName),
							_("overwrite_map"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
							null);

					if (answer != 0) {
						throw new WizardException("", false, false);
					} else {
						MapDAO.getInstance().deleteMap(mapName);
					}
				}

				MDIManagerFactory.getManager().setWaitCursor();

				final String[] errors = saveMap(mapName);
				MDIManagerFactory.getManager().restoreCursor();
				if (errors.length > 0) {
					String msg = _("errors_list");
					for (final String error : errors) {
						msg = msg + "\n" + error;
					}
					throw new WizardException(msg);
				}
			}

		} catch (final SQLException e1) {
			try {
				dbs = DBSession.reconnect();
			} catch (final DataException e) {
				logger.error(e.getMessage(), e);

			}
			MDIManagerFactory.getManager().restoreCursor();
			throw new WizardException(_("error_saving"), e1);
		}
	}

	private String[] saveMap(String mapName) throws SQLException {

		final List<Object[]> rows = new ArrayList<>();

		final List<String> errors = parse();

		if (errors.size() > 0) {
			return errors.toArray(new String[0]);
		} else {
			for (final LayerProperties lp : this.mapLayers) {
				Double maxScale = null, minScale = null;
				if (lp.getMaxScale() > -1) {
					maxScale = lp.getMaxScale();
				}
				if (lp.getMinScale() > -1) {
					minScale = lp.getMinScale();
				}
				if (lp.save()) {
					final Object[] row = { lp.getLayername(), lp.getTablename(), lp.getPosition(), lp.visible(),
							maxScale, minScale, lp.getGroup(), lp.getSchema() };
					rows.add(row);
				}
			}

			if (this.overviewChb.isSelected()) {
				try {
					saveOverview(mapName);
				} catch (final SQLException e) {
					logger.error(e.getMessage(), e);
					try {
						DBSession.reconnect();
					} catch (final DataException e1) {
						logger.error(e1.getMessage(), e1);
					}
					return new String[] { _("error_overview") };
				}
			}
			MapDAO.getInstance().saveMap(rows.toArray(new Object[0][0]), mapName);
			return new String[0];
		}
	}

	private void saveOverview(String mapName) throws SQLException {
		final FLayers layers = this.view.getMapOverview().getMapContext().getLayers();
		final List<Object[]> rows = new ArrayList<>();
		final List<String> knownTables = new ArrayList<>();
		for (int i = layers.getLayersCount() - 1; i >= 0; i--) {
			final FLayer layer = layers.getLayer(i);
			if (layer instanceof FLyrVect) {
				final FLyrVect lyr = (FLyrVect) layer;
				final DataStoreParameters parameters = lyr.getDataStore().getParameters();
				if (parameters instanceof JDBCStoreParameters) {
					final JDBCStoreParameters params = (JDBCStoreParameters) parameters;
					final String schema = params.getSchema();
					final String tablename = params.getTable();
					if (!knownTables.contains(schema + tablename)) {
						final String[] row = { tablename, schema, layer.getName(), Integer.toString(i) };
						rows.add(row);
						knownTables.add(schema + tablename);
					}
				}
			}
		}
		MapDAO.getInstance().saveMapOverview(rows.toArray(new Object[0][0]), mapName);
	}

	private void moveRowsDown() {
		final int[] selectedRows = this.mapTable.getSelectedRows();
		final DefaultTableModel model = (DefaultTableModel) this.mapTable.getModel();
		final ListSelectionModel selectionModel = this.mapTable.getSelectionModel();
		this.mapTable.clearSelection();
		int beginPos = 0;
		int endPos = 0;
		for (int i = 0; i < selectedRows.length; i++) {
			// determine the beginning and ending of the selected rows group
			beginPos = selectedRows[i];
			endPos = selectedRows[i];
			for (int j = i + 1; j < selectedRows.length; j++) {
				if (selectedRows[j] - endPos == 1) {
					endPos++;
				} else {
					break;
				}
				i = j;
			}
			if (this.mapTable.getRowCount() > endPos + 1) {
				// reorder the table
				model.moveRow(beginPos, endPos, beginPos + 1);
				selectionModel.addSelectionInterval(beginPos + 1, endPos + 1);
				// reorder lists - move last unselected value to next position of the last
				// selected one
				final LayerProperties elementToMove = this.mapLayers.get(beginPos + 1);
				this.mapLayers.remove(beginPos + 1);
				this.mapLayers.add(endPos, elementToMove);
			} else {
				// the selection group is at the top of the table, don't move anything
				selectionModel.addSelectionInterval(beginPos, endPos);
			}
		}
	}

	private void moveRowsUp() {
		final int[] selectedRows = this.mapTable.getSelectedRows();
		final DefaultTableModel model = (DefaultTableModel) this.mapTable.getModel();
		final ListSelectionModel selectionModel = this.mapTable.getSelectionModel();
		this.mapTable.clearSelection();
		int beginPos = 0;
		int endPos = 0;
		for (int i = 0; i < selectedRows.length; i++) {
			// determine the beginning and ending of the selected rows group
			beginPos = selectedRows[i];
			endPos = selectedRows[i];
			for (int j = i + 1; j < selectedRows.length; j++) {
				if (selectedRows[j] - endPos == 1) {
					endPos++;
				} else {
					break;
				}
				i = j;
			}
			if (beginPos - 1 >= 0) {
				// reorder the table
				model.moveRow(beginPos, endPos, beginPos - 1);
				selectionModel.addSelectionInterval(beginPos - 1, endPos - 1);
				// reorder lists - move last unselected value to next position of the last
				// selected one
				final LayerProperties elementToMove = this.mapLayers.get(beginPos - 1);
				this.mapLayers.remove(beginPos - 1);
				this.mapLayers.add(endPos, elementToMove);
			} else {
				// the selection group is at the top of the table, don't move anything
				selectionModel.addSelectionInterval(beginPos, endPos);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == this.upButton) {
			moveRowsUp();
		}
		if (e.getSource() == this.downButton) {
			moveRowsDown();
		}
	}

	private class MapTableModel extends DefaultTableModel {

		@Override
		public Class<?> getColumnClass(int index) {
			if (index == 0 || index == 2) {
				return Boolean.class;
			} else {
				return super.getColumnClass(index);
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 1) {
				return false;
			}
			return true;
		}
	}
}