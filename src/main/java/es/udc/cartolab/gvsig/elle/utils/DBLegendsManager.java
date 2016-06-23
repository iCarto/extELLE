/*
 * Copyright (c) 2010. Cartolab (Universidade da Coruï¿½a)
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

package es.udc.cartolab.gvsig.elle.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.gvsig.andami.PluginServices;
import org.gvsig.andami.PluginsManager;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.ILegend;
import org.gvsig.fmap.mapcontext.rendering.legend.styling.ILabelingStrategy;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.persistence.PersistenceManager;
import org.gvsig.tools.persistence.PersistentState;
import org.gvsig.tools.persistence.exception.PersistenceException;
import org.gvsig.tools.persistence.xml.XMLPersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.gui.wizard.save.LayerProperties;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DBLegendsManager extends AbstractLegendsManager {

    
	private static final Logger logger = LoggerFactory
			.getLogger(DBLegendsManager.class);

    private boolean tableStylesExists, tableOvStylesExists;
    private String schema, styleTable = DBStructure.getMapStyleTable(), styleOvTable = DBStructure.getOverviewStyleTable();
	private final XMLPersistenceManager persistenceManager;


    public DBLegendsManager(String leyendGroupName) throws WizardException {
	super(leyendGroupName);
	this.persistenceManager = initXMLPersistenceManager();
	
	try {
		DBSession dbs = DBSession.getCurrentSession();
		tableStylesExists = dbs.tableExists(DBStructure.getSchema(), styleTable);
		tableOvStylesExists = dbs.tableExists(DBStructure.getSchema(), styleOvTable);
		schema = DBStructure.getSchema();
	} catch (RuntimeException e) {
		throw new WizardException(e);
	}

    }

    public void loadLegends() {
	// TODO Auto-generated method stub

    }

	public void prepare() throws WizardException {
		DBSession dbs = DBSession.getCurrentSession();

		try {
			LoadLegend.createLegendtables();
		} catch (SQLException e) {
			try {
				DBSession.reconnect();
			} catch (DataException e1) {
				logger.error(e1.getMessage(), e1);
			}
			throw new WizardException(e);
		}

	}

    private void saveLeyend(FLyrVect layer, String layerName, String table, String type) throws WizardException {

	DBSession dbs = DBSession.getCurrentSession();
	try {
	    String symbology = getSymbologyAsXML(layer, type, persistenceManager);
	    String label = getLabelAsXML(layer, persistenceManager);
	    
		String[] row = {
		    layer.getName(),
		    getLegendGroupName(),
		    type,
		    symbology,
		    label
		};
		dbs.insertRow(DBStructure.getSchema(), table, row);
	    
	} catch (SQLException e) {
	    throw new WizardException(e);
	} 
    }

    private String getLabelAsXML(FLyrVect layer, PersistenceManager manager) {
	String label = null;
	if (layer.isLabeled()) {
	    final ILabelingStrategy labelingStrategy = layer.getLabelingStrategy();
	    if (labelingStrategy != null) {
	    	label = serializeObject(labelingStrategy, manager);
	    }
	}
	return label;
    }
    
    private XMLPersistenceManager initXMLPersistenceManager() {
    	PersistenceManager defaultManager = ToolsLocator.getPersistenceManager();
    	XMLPersistenceManager manager = new XMLPersistenceManager();
    	// Al crear el XMLPersistenceManager de esta forma no están instanciadas
    	// en el manager las factorias que hacen falta para crear el state así
    	// que las cogemos del ZIPXMLPersistenceManager
		Iterator it = defaultManager.getFactories().iterator();
		while (it.hasNext()) {
			manager.registerFactory((org.gvsig.tools.persistence.PersistenceFactory)it.next()); 
		}
		return manager;
    }
    private String serializeObject(Object o, PersistenceManager manager) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	manager.putObject(baos, o);
    	try {
			return baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
    	return "";
    }

    // TODO. Only default gvsig simbology is supported
    private String getSymbologyAsXML(FLyrVect layer, String type, PersistenceManager manager) {
    	ILegend legend = layer.getLegend();
    	return serializeObject(legend, manager);
    }

	public void saveLegends() throws WizardException {
		for (LayerProperties lp : layers) {
			saveLeyend(lp.getLayer(), lp.getLayername(), styleTable, lp.getLegendType());
		}
	}

    public boolean exists() {
	try {
	    return tableStylesExists && LoadLegend.legendExistsDB(getLegendGroupName());
	} catch (SQLException e) {
	    try {
		DBSession.reconnect();
	    } catch (DataException e1) {
	    	logger.error(e1.getMessage(), e1);
	    }
	    return false;
	}
    }

    public boolean canRead() {
	// TODO canRead
	return true;
    }

    public boolean canWrite() {
	// TODO canWrite
	return true;
    }

    public void loadOverviewLegends() {
    }

	public void saveOverviewLegends(String type) throws WizardException {
		for (FLyrVect layer : overviewLayers) {
			saveLeyend(layer, layer.getName(), styleOvTable, type);
		}
	}

    public String getConfirmationMessage() {
	if (!tableStylesExists || !tableOvStylesExists) {
	    return String.format(PluginServices.getText(this, "tables_will_be_created"), schema);
	} else {
	    return null;
	}
    }

}
