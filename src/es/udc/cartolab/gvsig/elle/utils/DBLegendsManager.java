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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.gui.wizard.save.LayerProperties;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DBLegendsManager extends AbstractLegendsManager {

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

	public void loadLegends() {
		// TODO Auto-generated method stub

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
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
			File legendFile = File.createTempFile("style", "." + type);
			LoadLegend.saveLegend(layer, legendFile);
			BufferedReader reader = new BufferedReader(new FileReader(legendFile.getAbsolutePath()));
			StringBuffer buffer = new StringBuffer();
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line).append("\n");
				line = reader.readLine();
			}
			String xml = buffer.toString();
			String[] row = {
					layer.getName(),
					getLegendGroupName(),
					type,
					xml
			};
			dbs.insertRow(DBStructure.getSchema(), table, row);
		} catch (SQLException e) {

		} catch (Exception e) {
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
				//TODO logger
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
