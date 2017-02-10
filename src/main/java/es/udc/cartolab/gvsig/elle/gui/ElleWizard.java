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

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.BorderLayout;
import java.io.InputStream;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cresques.cts.IProjection;
import org.gvsig.andami.PluginServices;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.addlayer.AddLayerDialog;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.store.jdbc.JDBCStoreParameters;
import org.gvsig.app.gui.WizardPanel;
import org.gvsig.app.gui.panels.CRSSelectPanel;
import org.gvsig.app.project.documents.view.gui.DefaultViewPanel;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.tools.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.ConfigExtension;
import es.udc.cartolab.gvsig.elle.utils.LoadLegend;
import es.udc.cartolab.gvsig.elle.utils.MapDAO;
import es.udc.cartolab.gvsig.elle.utils.MapFilter;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class ElleWizard extends WizardPanel {

	private static final Logger logger = LoggerFactory.getLogger(ElleWizard.class);

    private JPanel listPanel = null;
    private JList layerList = null;
    private JList groupList = null;
    private CRSSelectPanel crsPanel = null;
    private DBSession dbs;
    private String[][] layers;
    private final String wizardTitle;
    private MapFilter mapFilter;

    public ElleWizard() {
	ConfigExtension configExt = (ConfigExtension) PluginServices.getExtension(ConfigExtension.class);
	this.wizardTitle = configExt.getWizardTitle();
	this.mapFilter = configExt.getMapFilter();
    }
    
    @Override
    public void execute() {
    	executeWizard();
    }
    
    @Override
    public Object executeWizard() {
    	JDBCStoreParameters[] params = (JDBCStoreParameters[]) getParameters();
    	DataManager manager = DALLocator.getDataManager();
    	ApplicationManager application = ApplicationLocator.getManager();
    	
    	MapControl mapControl = getMapCtrl();
    	FLayers root = mapControl.getMapContext().getLayers();
    	MapContext mc = mapControl.getMapContext();
    	
    	FLayers layersToAdd = new FLayers();
    	
    	LoadLegend loadLegend = new LoadLegend();
    	String styleName = groupList.getSelectedValue() == null ? "" : groupList.getSelectedValue().toString();
    	try {
    	layersToAdd.setMapContext(mc);
    	layersToAdd.setParentLayer(root);
    	layersToAdd.setName(getWizarTitle());
    	int[] selectedPos = layerList.getSelectedIndices();
    	for (int i=0; i<params.length; i++) {
    		JDBCStoreParameters p = params[i];
    		FeatureStore fs = (FeatureStore) manager.openStore(p.getDataStoreName(), p);
    		int pos = selectedPos[i];
    		FLayer layer = application.getMapContextManager().createLayer(layers[pos][1], fs);
    		loadLegend.loadDBLegend((FLyrVect) layer, styleName, false);
    		fs.dispose();
    		layersToAdd.addLayer(layer);
    	}
    	} catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    	mc.getLayers().addLayer(layersToAdd);
    	return layersToAdd;
    }

    
    

    protected String getWhereClause() {
	return "";
    }

    @Override
    public void initWizard() {
    	DefaultViewPanel view = (DefaultViewPanel) PluginServices.getMDIManager().getActiveWindow();
    	setMapCtrl(view.getMapControl());
    	
    	dbs = DBSession.getCurrentSession();
    	setTabName(getWizarTitle());

    	setLayout(new BorderLayout());
    	JPanel mainPanel = new JPanel();
    	mainPanel.setLayout(new BorderLayout());

    	if (dbs != null) {
    	    mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
    		    null, _("Choose_Layer"),
    		    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
    		    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));


    	    mainPanel.add(getListPanel(), BorderLayout.CENTER);
    	    mainPanel.add(getCRSPanel(), BorderLayout.SOUTH);

    	} else {
    	    JLabel label = new JLabel(_("notConnectedError"));
    	    mainPanel.add(label, BorderLayout.NORTH);
    	}
    	add(mainPanel, BorderLayout.CENTER);
	

    }


    private String getWizarTitle() {
	return wizardTitle;
    }

    private JPanel getListPanel() {
	if (listPanel == null) {

	    listPanel = new JPanel();

	    try {
		 InputStream stream = getClass().getClassLoader()
			    .getResourceAsStream("forms/loadLayer.jfrm");
		FormPanel form = new FormPanel(stream);
		form.setFocusTraversalPolicyProvider(true);

		listPanel.add(form);

		dbs = DBSession.getCurrentSession();

		if (dbs.tableExists(DBStructure.getSchema(), DBStructure.getMapTable())) {

		    String[] maps = MapDAO.getInstance().getMaps();

		    layerList = form.getList("layerList");
		    groupList = form.getList("groupList");
		    groupList.setListData(filterMaps(maps));

		    groupList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
			    int[] selected = groupList.getSelectedIndices();
			    if (selected.length == 1) {
				String where = String.format("where mapa ='%s'", groupList.getSelectedValues()[0]);
				try {
					    layers = dbs.getTable(
						    DBStructure.getMapTable(),
						    DBStructure.getSchema(),
						    where,
						    new String[] {"posicion", "nombre_campa",
						    true);
				} catch (SQLException e) {
				    JOptionPane.showMessageDialog(null,
					    "Error SQL: " + e.getMessage(),
					    "SQL Exception",
					    JOptionPane.ERROR_MESSAGE);
				    try {
					dbs = DBSession.reconnect();
				    } catch (DataException e1) {
					logger.error(e1.getMessage(), e1);
				    }
				}
				if (layers != null) {
				    String[] layerNames = new String[layers.length];
				    for (int i=0; i<layers.length; i++) {
					layerNames[i] = layers[i][1];
				    }
				    layerList.setListData(layerNames);
				}
			    } else {
				layerList.setListData(new Object[0]);
			    }
			    callStateChanged(false);
			}

		    });

		    layerList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
			    int[] selected = groupList.getSelectedIndices();
			    if (selected.length > 0) {
				callStateChanged(true);
			    } else {
				callStateChanged(false);
			    }
			}

		    });
		} else {
		    listPanel = new JPanel();
		    JLabel label = new JLabel(_("no_map_table_on_schema"));
		    listPanel.add(label);
		}

	    } catch (SQLException e) {
		listPanel = new JPanel();
		JLabel label = new JLabel("SQL Exception: " + e.getMessage());
		listPanel.add(label);
		try {
		    dbs = DBSession.reconnect();
		} catch (DataException e1) {
		    logger.error(e1.getMessage(), e1);
		}
	    } catch (FormException e) {
		logger.error(e.getMessage(), e);
	    }

	}

	return listPanel;

    }

    private String[] filterMaps(String[] maps) {
	return mapFilter.filter(maps);
    }

    private JPanel getCRSPanel() {
	if (crsPanel == null) {
	    crsPanel = CRSSelectPanel.getPanel(AddLayerDialog.getLastProjection());
	    crsPanel.addActionListener(new java.awt.event.ActionListener() {
		@Override
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    if (crsPanel.isOkPressed()) {
			AddLayerDialog.setLastProjection(crsPanel.getCurProj());
		    }
		}
	    });
	}
	return crsPanel;
    }

	@Override
	public void close() {
	}

	public DataStoreParameters[] getParameters() {
		IProjection proj = crsPanel.getCurProj();
		int[] selectedPos = layerList.getSelectedIndices();
		JDBCStoreParameters defaultParams = null;
		JDBCStoreParameters[] params = new JDBCStoreParameters[selectedPos.length];
		try {
			defaultParams = DBSession.getCurrentSession().getConnectionWithParams().getStoreParams();
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
			return new DataStoreParameters[0];
		} 
		if (selectedPos.length<1) {
			return new DataStoreParameters[0];
		}
		for (int i=0; i< selectedPos.length; i++ ) {
			int pos = selectedPos[i];
			String layerName = layers[pos][1];
			String tableName = layers[pos][2];

			String schema = null;
			if (layers[pos].length > 8) {
			    if (layers[pos][8].length()>0) {
				schema = layers[pos][8];
			    }
			}

			String whereClause = getWhereClause();
			params[i] = (JDBCStoreParameters) defaultParams.getCopy();
			params[i].setTable(tableName);
			params[i].setSchema(schema);
			// params[i].setBaseFilter(whereClause);
			params[i].setCRS(proj);
		}
		return params;
	}
}
