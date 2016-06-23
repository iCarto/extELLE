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

import org.gvsig.andami.PluginServices;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.store.jdbc.JDBCServerExplorerParameters;
import org.gvsig.fmap.dal.store.jdbc.JDBCStoreParameters;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.utils.MapDAO;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class SaveMapWizardComponent extends WizardComponent implements ActionListener {

	
	private static final Logger logger = LoggerFactory
			.getLogger(SaveMapWizardComponent.class);
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

	mapLayers = new ArrayList<LayerProperties>();

	//layout
	MigLayout layout = new MigLayout("inset 0, align center",
		"10[grow]10",
		"10[grow]");

	setLayout(layout);

	add(getMainPanel(), "shrink, growx, growy, wrap");
    }

    private JPanel getMainPanel() {
	JPanel panel = new JPanel();
	MigLayout layout = new MigLayout("inset 0, align center",
		"[grow][grow]",
		"[grow][]");
	panel.setLayout(layout);

	//map, up & down buttons
	setMapTable();
	JPanel tablePanel = new JPanel();
	MigLayout tableLayout = new MigLayout("inset 0, align center", "[grow][]", "[grow]");
	tablePanel.setLayout(tableLayout);
	tablePanel.add(new JScrollPane(mapTable), "growx, growy");
	tablePanel.add(getUpDownPanel(), "shrink, align right, wrap");

	//map overview
	overviewChb = new JCheckBox(PluginServices.getText(this, "save_overview"));
	overviewChb.setSelected(true);

	//map name
	JPanel namePanel = new JPanel();
	namePanel.add(new JLabel(PluginServices.getText(this, "map_name")));
	mapNameField = new JTextField("", 20);

	mapNameField.addKeyListener(new KeyListener() {

	    public void keyPressed(KeyEvent e) {
	    }

	    public void keyReleased(KeyEvent e) {
		callStateChanged();
	    }


	    public void keyTyped(KeyEvent e) {
	    }

	});
	namePanel.add(mapNameField);

	//add to panel
	panel.add(tablePanel, "span 2 1, grow, wrap");
	panel.add(overviewChb, "grow 0, align left");
	panel.add(namePanel, "grow 0, align right, wrap");

	return panel;
    }

    private JPanel getUpDownPanel() {

	JPanel upDownPanel = new JPanel();
	MigLayout layout = new MigLayout("inset 0, align center",
		"[]",
		"[grow]");

	upDownPanel.setLayout(layout);
	java.net.URL imgURL = getClass().getResource("/go-up.png");
	upButton = new JButton(new ImageIcon(imgURL));
	upButton.addActionListener(this);

	imgURL = getClass().getResource("/go-down.png");
	downButton = new JButton(new ImageIcon(imgURL));
	downButton.addActionListener(this);

	upDownPanel.add(upButton, "shrink, wrap");
	upDownPanel.add(downButton, "shrink");

	return upDownPanel;
    }

    private void setMapTable() {

	String[] header = {"",
		PluginServices.getText(this, "layer"),
		PluginServices.getText(this, "visible"),
		PluginServices.getText(this, "max_scale"),
		PluginServices.getText(this, "min_scale")};
	DefaultTableModel model = new MapTableModel();
	for (String columnName : header) {
	    model.addColumn(columnName);
	}
	model.setRowCount(0);
	mapTable =  new JTable();
	mapTable.setModel(model);

	mapTable.getColumnModel().getColumn(0).setMaxWidth(30);
	mapTable.getColumnModel().getColumn(1).setMinWidth(120);
	mapTable.getColumnModel().getColumn(2).setMinWidth(40);
	mapTable.getColumnModel().getColumn(3).setMinWidth(60);
	mapTable.getColumnModel().getColumn(4).setMinWidth(60);
    }

    public boolean canFinish() {
	return canNext();
    }

    public boolean canNext() {
	String mapname = mapNameField.getText();
	return mapname != null && !mapname.equals("");
    }

    public String getWizardComponentName() {
	return "save_map";
    }

    private List<String> parse() {

	if (mapTable.isEditing()) {
	    if (mapTable.getCellEditor() != null) {
		mapTable.getCellEditor().stopCellEditing();
	    }
	}

	String emptyNameError = PluginServices.getText(this, "error_empty_layer_name");
	String parseError = PluginServices.getText(this, "error_numeric_scale");
	String minGreaterError = PluginServices.getText(this, "error_min_greater_than_max");
	String repeatedLayerNameError = PluginServices.getText(this, "error_repeated_layer_name");

	List<String> errors = new ArrayList<String>();
	int position = 1;
	MapTableModel model = (MapTableModel) mapTable.getModel();
	List<String> layerNames = new ArrayList<String>();

	for (int i=model.getRowCount()-1; i>=0; i--) {

	    String name = model.getValueAt(i,1).toString();
	    if (name.equals("")) {
		if (!errors.contains(emptyNameError))  {
		    errors.add(emptyNameError);
		}
	    }

	    if (!layerNames.contains(name)) {

		layerNames.add(name);

		boolean save = (Boolean) model.getValueAt(i, 0);

		boolean visible = (Boolean) model.getValueAt(i, 2);
		Double maxScale = null, minScale = null;
		Object aux;
		try {
		    aux = model.getValueAt(i, 3);
		    if (aux!=null) {
			String str = aux.toString();
			if (!str.equals("")) {
			    maxScale = NumberFormat.getInstance().parse(str).doubleValue();
			}
		    }
		} catch (ParseException e) {
		    if (!errors.contains(parseError)) {
			errors.add(parseError);
		    }
		}
		try {
		    aux = model.getValueAt(i, 4);
		    if (aux != null) {
			String str = aux.toString();
			if (!str.equals("")) {
			    minScale = NumberFormat.getInstance().parse(str).doubleValue();
			}
		    }
		} catch (ParseException e) {
		    if (!errors.contains(parseError)) {
			errors.add(parseError);
		    }
		}
		if (minScale!=null && maxScale!=null && minScale > maxScale) {
		    if (!errors.contains(minGreaterError)) {
			errors.add(minGreaterError);
		    }
		}

		LayerProperties lp = mapLayers.get(i);
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
		errors.add(String.format(repeatedLayerNameError, name));
	    }
	}

	return errors;
    }

    public void setProperties() throws WizardException {
	List<String> errors = parse();
	if (errors.size()>0) {
	    String msg = PluginServices.getText(this, "errors_list");
	    for (String error : errors) {
		msg = msg + "\n" + error;
	    }
	    throw new WizardException(msg);
	}
	properties.put(SaveMapWizardComponent.PROPERTY_MAP_NAME, mapNameField.getText().trim());
	properties.put(SaveMapWizardComponent.PROPERTY_LAYERS_MAP, mapLayers);
	properties.put(SaveLegendsWizardComponent.PROPERTY_SAVE_OVERVIEW, overviewChb.isSelected());
    }

    @SuppressWarnings("unchecked")
    public void showComponent() throws WizardException {
	DefaultTableModel model = (DefaultTableModel) mapTable.getModel();
	model.setRowCount(0);
	Object aux = properties.get(PROPERTY_LAYERS_MAP);
	if (aux == null) {
	    aux = properties.get(SaveMapWizard.PROPERTY_VIEW);
	    if (aux != null && aux instanceof IView) {
		view = (IView) aux;
		createMapLayerList(view.getMapControl().getMapContext().getLayers());
	    } else {
		throw new WizardException(PluginServices.getText(this, "no_view_error"));
	    }
	} else if (aux instanceof List<?>) {
	    mapLayers = (List<LayerProperties>) aux;
	} else {
	    throw new WizardException(PluginServices.getText(this, "no_layer_list_error"));
	}

	for (LayerProperties lp : mapLayers) {
	    double maxScale = lp.getMaxScale();
	    String maxScaleStr = "";
	    if (maxScale >= 0) {
		maxScaleStr = NumberFormat.getInstance().format(maxScale);
	    }
	    double minScale = lp.getMinScale();
	    String minScaleStr = "";
	    if (minScale >= 0) {
		minScaleStr = NumberFormat.getInstance().format(minScale);
	    }
	    Object[] row = {lp.save(),
		    lp.getLayername(),
		    lp.visible(),
		    maxScaleStr,
		    minScaleStr	};
	    model.addRow(row);

	}

	//popup menu
	final LayerListPopupMenu popupmenu = new LayerListPopupMenu(this, mapTable, mapLayers);
	mapTable.addMouseListener(new MouseListener() {


	    public void mouseClicked(MouseEvent e) {
		if (e.isMetaDown()) {
		    popupmenu.show(e.getComponent(), e.getX(), e.getY());
		}
	    }


	    public void mouseEntered(MouseEvent e) {
		System.out.println("mouse entered");
	    }


	    public void mouseExited(MouseEvent e) {
	    }


	    public void mousePressed(MouseEvent e) {
	    }


	    public void mouseReleased(MouseEvent e) {
	    }

	});


    }


    /*
     * This method is used in order to retrieve the name of all the nested
     * groups as a string, each of them separated by '/'. Therefore, we have to
     * escape that character ('\/'), which also means duplicating the
     * backslashes.
     */
    private String getGroupCompositeName(FLayers group) {
	// We check whether the layer has a parent group or it doesn't.
	if ((group.getName() == null) || (group.getName().equals("root layer") && (group.getParentLayer() == null))) {
	    return "";
	}
	// We duplicate previously existing backslashes and escape the slashes.
	String groupName = group.getName().replace("\\", "\\\\").replace("/", "\\/");
	if (group.getParentLayer() != null) {
	    String parentName = getGroupCompositeName(group.getParentLayer());
	    if (parentName.length() > 0) {
		groupName =  parentName + "/" + groupName;
	    }
	}
	return groupName;
    }

    private void createMapLayerList(FLayers layers) {

	for (int i=layers.getLayersCount()-1; i>=0; i--) {
	    FLayer layer = layers.getLayer(i);
	    if (layer instanceof FLayers) {
		createMapLayerList((FLayers) layer);
	    } else if (layer instanceof FLyrVect) {
		try {
		    LayerProperties lp = new LayerProperties((FLyrVect) layer);
			//layer data to fill the table
			String group = "";
			if (layer.getParentLayer() != null) {
			    group = getGroupCompositeName(layer.getParentLayer());
			}
			double maxScale = layer.getMaxScale();
			if (maxScale >= 0) {
			    lp.setMaxScale(maxScale);
			}
			double minScale = layer.getMinScale();
			if (minScale >= 0) {
			    lp.setMinScale(minScale);
			}
			boolean visible = layer.isVisible();

			lp.setVisible(visible);
			lp.setGroup(group);
			lp.setPosition(mapLayers.size());
			lp.setSave(true);

			mapLayers.add(lp);

		} catch (WizardException e) {
		    // layer is not postgis, nothing to do
		}
	    }
	}
    }

    public void finish() throws WizardException {
	DBSession dbs = DBSession.getCurrentSession();
	try {
	    boolean tableMapExists = dbs.tableExists(DBStructure.getSchema(), DBStructure.getMapTable());
	    boolean tableMapOvExists = dbs.tableExists(DBStructure.getSchema(), DBStructure.getOverviewTable());

	    if (!tableMapExists || !tableMapOvExists) {

		boolean canCreate = false;
		if(MapDAO.getInstance().hasSchema()) {
		    canCreate = dbs.getDBUser().canCreateTable(
			    DBStructure.getSchema());
		} else {
		    canCreate = dbs.getDBUser().canCreateSchema();
		    // if the user has permissions to create schema also have
		    // them to create table inside it
		}

		if (!canCreate) {
		    //[jestevez] I think this code is never reached due to the limitations of SaveMapExtension
		    throw new WizardException(PluginServices.getText(this, "table_map_contact_admin"));
		} else {
		    String message = String.format(PluginServices.getText(this, "tables_will_be_created"), DBStructure.getSchema());
		    int answer = JOptionPane.showConfirmDialog(
			    this,
			    message,
			    "",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.QUESTION_MESSAGE,
			    null);
		    if (answer == 0) {
			MapDAO.getInstance().createMapTables();
			properties.put(SaveLegendsWizardComponent.PROPERTY_CREATE_TABLES_QUESTION, false);
			JOptionPane.showMessageDialog(
				this,
				PluginServices.getText(this, "tables_created_correctly"),
				"",
				JOptionPane.INFORMATION_MESSAGE);
			tableMapExists = true;
		    }
		}
	    }

	    if (tableMapExists) {

		String mapName = mapNameField.getText();

		boolean mapExists = MapDAO.getInstance().mapExists(mapName);
		if (mapExists) {
		    String question = PluginServices.getText(this, "overwrite_map_question");
		    question = String.format(question, mapName);
		    int answer = JOptionPane.showOptionDialog(
			    this,
			    question,
			    PluginServices.getText(this, "overwrite_map"),
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.QUESTION_MESSAGE,
			    null,
			    null,
			    null);

		    if (answer!=0) {
			throw new WizardException("", false, false);
		    } else {
			MapDAO.getInstance().deleteMap(mapName);
		    }
		}

		PluginServices.getMDIManager().setWaitCursor();

		String[] errors = saveMap(mapName);
		PluginServices.getMDIManager().restoreCursor();
		if (errors.length>0) {
		    String msg = PluginServices.getText(this, "errors_list");
		    for (String error : errors) {
			msg = msg + "\n" + error;
		    }
		    throw new WizardException(msg);
		}
	    }

	} catch (SQLException e1) {
	    try {
		dbs = DBSession.reconnect();
	    } catch (DataException e) {
		logger.error(e.getMessage(), e);
		
	    }
	    PluginServices.getMDIManager().restoreCursor();
	    throw new WizardException(PluginServices.getText(this, "error_saving_map"), e1);
	}
    }

    private String[] saveMap(String mapName) throws SQLException {

	List<Object[]> rows = new ArrayList<Object[]>();

	List<String> errors = parse();

	if (errors.size()>0) {
	    return errors.toArray(new String[0]);
	} else {
	    for (LayerProperties lp : mapLayers) {
		Double maxScale = null, minScale = null;
		if (lp.getMaxScale() > -1) {
		    maxScale = lp.getMaxScale();
		}
		if (lp.getMinScale() > -1) {
		    minScale = lp.getMinScale();
		}
		if (lp.save()) {
		    Object[] row = {lp.getLayername(),
			    lp.getTablename(),
			    lp.getPosition(),
			    lp.visible(),
			    maxScale,
			    minScale,
			    lp.getGroup(),
			    lp.getSchema()};
		    rows.add(row);
		}
	    }

	    if (overviewChb.isSelected()) {
		try {
		    saveOverview(mapName);
		} catch (SQLException e) {
		    try {
			DBSession.reconnect();
		    } catch (DataException e1) {
			logger.error(e1.getMessage(), e1);
		    }
		    return new String[] { PluginServices.getText(this,
			    "error_overview") };
		}
	    }
	    MapDAO.getInstance().saveMap(rows.toArray(new Object[0][0]), mapName);
	    return new String[0];
	}
    }

    private void saveOverview(String mapName) throws SQLException {
	FLayers layers = view.getMapOverview().getMapContext().getLayers();
	List<Object[]> rows = new ArrayList<Object[]>();
	List<String> knownTables = new ArrayList<String>();
	for (int i=layers.getLayersCount()-1; i>=0; i--) {
	    FLayer layer = layers.getLayer(i);
	    if (layer instanceof FLyrVect) {
	    	FLyrVect lyr = (FLyrVect) layer;
	    	DataStoreParameters parameters = lyr.getDataStore().getParameters();
	    	if (parameters instanceof JDBCStoreParameters) {
	    		JDBCStoreParameters params = (JDBCStoreParameters) parameters;
	    		String schema = params.getSchema();
	    		String tablename = params.getTable();
	    		if (!knownTables.contains(schema + tablename)) {
	    			String[] row = { tablename, schema, layer.getName(), Integer.toString(i) };
	    			rows.add(row);
	    			knownTables.add(schema + tablename);
	    		}
	    	}
	    }
	}
	MapDAO.getInstance().saveMapOverview(rows.toArray(new Object[0][0]), mapName);
    }

    private void moveRowsDown() {
	int[] selectedRows = mapTable.getSelectedRows();
	DefaultTableModel model = (DefaultTableModel) mapTable.getModel();
	ListSelectionModel selectionModel = mapTable.getSelectionModel();
	mapTable.clearSelection();
	int beginPos = 0;
	int endPos = 0;
	for(int i=0; i<selectedRows.length; i++) {
	    //determine the beginning and ending of the selected rows group
	    beginPos = selectedRows[i];
	    endPos = selectedRows[i];
	    for (int j=i+1; j<selectedRows.length; j++) {
		if (selectedRows[j]-endPos == 1) {
		    endPos++;
		} else {
		    break;
		}
		i = j;
	    }
	    if (mapTable.getRowCount()>endPos+1) {
		//reorder the table
		model.moveRow(beginPos, endPos, beginPos+1);
		selectionModel.addSelectionInterval(beginPos+1, endPos+1);
		//reorder lists - move last unselected value to next position of the last selected one
		LayerProperties elementToMove = mapLayers.get(beginPos+1);
		mapLayers.remove(beginPos+1);
		mapLayers.add(endPos, elementToMove);
	    } else {
		//the selection group is at the top of the table, don't move anything
		selectionModel.addSelectionInterval(beginPos, endPos);
	    }
	}
    }

    private void moveRowsUp() {
	int[] selectedRows = mapTable.getSelectedRows();
	DefaultTableModel model = (DefaultTableModel) mapTable.getModel();
	ListSelectionModel selectionModel = mapTable.getSelectionModel();
	mapTable.clearSelection();
	int beginPos = 0;
	int endPos = 0;
	for(int i=0; i<selectedRows.length; i++) {
	    //determine the beginning and ending of the selected rows group
	    beginPos = selectedRows[i];
	    endPos = selectedRows[i];
	    for (int j=i+1; j<selectedRows.length; j++) {
		if (selectedRows[j]-endPos == 1) {
		    endPos++;
		} else {
		    break;
		}
		i = j;
	    }
	    if (beginPos-1>=0) {
		//reorder the table
		model.moveRow(beginPos, endPos, beginPos-1);
		selectionModel.addSelectionInterval(beginPos-1, endPos-1);
		//reorder lists - move last unselected value to next position of the last selected one
		LayerProperties elementToMove = mapLayers.get(beginPos-1);
		mapLayers.remove(beginPos-1);
		mapLayers.add(endPos, elementToMove);
	    } else {
		//the selection group is at the top of the table, don't move anything
		selectionModel.addSelectionInterval(beginPos, endPos);
	    }
	}
    }

    public void actionPerformed(ActionEvent e) {

	if (e.getSource() == upButton) {
	    moveRowsUp();
	}
	if (e.getSource() == downButton) {
	    moveRowsDown();
	}
    }

    private class MapTableModel extends DefaultTableModel {

	public Class<?> getColumnClass(int index) {
	    if (index == 0 || index == 2) {
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