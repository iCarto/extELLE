/*
 * Copyright (c) 2010. Cartolab (Universidade da Coruña)
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

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.elle.db.DBStructure;
import es.icarto.gvsig.elle.style.LayerLabeling;
import es.icarto.gvsig.elle.style.LayerSimbology;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.gui.wizard.save.LayerProperties;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DBLegendsManager extends AbstractLegendsManager {

    private static final Logger logger = Logger
	    .getLogger(DBLegendsManager.class);

    private boolean tableStylesExists, tableOvStylesExists;
    private String notConnected = PluginServices.getText(this, "notConnectedError");
    private String schema, styleTable = DBStructure.getMapStyleTable(), styleOvTable = DBStructure.getOverviewStyleTable();


    public DBLegendsManager(String leyendGroupName) throws WizardException {
	super(leyendGroupName);

	DBSession dbs = DBSession.getCurrentSession();
	if (dbs!=null) {
	    try {
		tableStylesExists = dbs.tableExists(DBStructure.getSchema(), styleTable);
		tableOvStylesExists = dbs.tableExists(DBStructure.getSchema(), styleOvTable);
		schema = DBStructure.getSchema();
	    } catch (SQLException e) {
		throw new WizardException(e);
	    }
	} else {
	    throw new WizardException(notConnected);
	}

    }


    public void prepare() throws WizardException {
	DBSession dbs = DBSession.getCurrentSession();
	if (dbs != null) {
	    try {
		LoadLegend.createLegendtables();
	    } catch (SQLException e) {
		try {
		    DBSession.reconnect();
		} catch (DBException e1) {
		    logger.error(e1.getStackTrace(), e1);
		}
		throw new WizardException(e);
	    }
	} else {
	    throw new WizardException(notConnected);
	}

    }

    private void saveLeyend(FLyrVect layer, String layerName, String table, String type) throws WizardException {

	DBSession dbs = DBSession.getCurrentSession();
	try {
	    String symbology = new LayerSimbology(layer).getAs(type);
	    String label = new LayerLabeling(layer).get();

	    // fpuga. April 15, 2014
	    // This if is here only for compatible reasons with old version of
	    // ELLE without support for labelling. The if, and the else can be
	    // safely removed when all projects have added to the database a
	    // "label" column to _map_style and _map_overview_style
	    if (dbs.getColumns(DBStructure.getSchema(), table).length == 5) {
		String[] row = {
		    layer.getName(),
		    getLegendGroupName(),
		    type,
		    symbology,
		    label
		};
		dbs.insertRow(DBStructure.getSchema(), table, row);
	    } else {
		String[] row = { 
			layer.getName(),
			getLegendGroupName(),
			type,
			symbology
		};
		dbs.insertRow(DBStructure.getSchema(), table, row);
	    }
	} catch (SQLException e) {
	    throw new WizardException(e);
	} catch (LegendDriverException e) {
	    throw new WizardException(e);
	}
    }
    

    public void saveLegends() throws WizardException {

	DBSession dbs = DBSession.getCurrentSession();
	if (dbs != null) {

	    for (LayerProperties lp : layers) {
		saveLeyend(lp.getLayer(), lp.getLayername(), styleTable, lp.getLegendType());
	    }
	} else {
	    throw new WizardException(notConnected);
	}
    }

    public boolean exists() {
	try {
	    return tableStylesExists && LoadLegend.legendExistsDB(getLegendGroupName());
	} catch (SQLException e) {
	    try {
		DBSession.reconnect();
	    } catch (DBException e1) {
		logger.error(e1.getStackTrace(), e1);
	    }
	    return false;
	}
    }

    public boolean canRead() {
	return true;
    }

    public boolean canWrite() {
	return true;
    }

    public void saveOverviewLegends(String type) throws WizardException {

	DBSession dbs = DBSession.getCurrentSession();
	if (dbs != null) {

	    for (FLyrVect layer : overviewLayers) {
		saveLeyend(layer, layer.getName(), styleOvTable, type);
	    }
	} else {
	    throw new WizardException(notConnected);
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
