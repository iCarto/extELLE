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
package es.udc.cartolab.gvsig.elle.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.cresques.cts.IProjection;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.tools.exception.BaseException;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.save.LayerProperties;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class MapDAO {

    private static MapDAO instance = null;
    private List<ELLEMap> loadedMaps;

    protected MapDAO() {
	loadedMaps = new ArrayList<ELLEMap>();
    }

    private synchronized static void createInstance() {
	if (instance == null) {
	    instance = new MapDAO();
	}
    }

    public static MapDAO getInstance() {
	if (instance == null) {
	    createInstance();
	}
	return instance;
    }

    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

    protected  FLayer getLayer(String layerName, String tableName,
	    String schema, String whereClause, IProjection proj,
	    boolean visible) throws BaseException {
	DBSession dbs = DBSession.getCurrentSession();
	FLayer layer = null;

	if (dbs != null) {
	    layer = dbs.getLayer(layerName, tableName, schema, whereClause,
		    proj);
	    layer.setVisible(visible);
	}
	return layer;
    }

    public FLayer getLayer(LayerProperties lp, IProjection proj) throws BaseException  {
	return getLayer(lp.getLayername(), lp.getTablename(), lp.getSchema(),
		lp.getWhere(), proj, lp.visible());
    }

    public ELLEMap getMap(IView view, String mapName) throws Exception {
	return getMap(view, mapName, LoadLegend.NO_LEGEND, "");
    }

    protected List<LayerProperties> getViewLayers(String mapName) throws SQLException {

	List<LayerProperties> viewLayers = new ArrayList<LayerProperties>();


	DBSession dbs = DBSession.getCurrentSession();
	if (dbs != null) {
	    String where = "WHERE mapa='" + mapName + "'";

	    String[][] layers = dbs.getTable(DBStructure.getMapTable(), DBStructure.getSchema(), where, new String[]{"posicion"}, false);

	    for (int i=0; i<layers.length; i++) {
		String schema=null;
		if (layers[i][8].length()>0) {
		    schema = layers[i][8];
		}
		LayerProperties lp = new LayerProperties(schema, layers[i][2], layers[i][1]);

		boolean visible = true;
		if (!(layers[i][4].equalsIgnoreCase("t") || layers[i][4]
			.equalsIgnoreCase("1") || layers[i][4]
			.equalsIgnoreCase("true"))) {
		    visible = false;
		}
		lp.setVisible(visible);

		double maxScale = -1;
		try {
		    maxScale = Double.parseDouble(layers[i][5]);
		} catch (NumberFormatException e) {
		    //do nothing
		}
		if (maxScale > -1) {
		    lp.setMaxScale(maxScale);
		}

		double minScale = -1;
		try {
		    minScale = Double.parseDouble(layers[i][6]);
		} catch (NumberFormatException e) {
		    //do nothing
		}
		if (minScale > -1) {
		    lp.setMinScale(minScale);
		}

		int position = 0;
		try {
		    position = Integer.parseInt(layers[i][3]);
		} catch (NumberFormatException e) {
		    //do nothing
		}

		lp.setPosition(position);

		lp.setGroup(layers[i][7]);

		viewLayers.add(lp);
	    }
	}

	return viewLayers;

    }

    protected List<LayerProperties> getOverviewLayers(String mapName) throws SQLException {

	List<LayerProperties> overviewLayers = new ArrayList<LayerProperties>();


	DBSession dbs = DBSession.getCurrentSession();
	if (dbs != null) {
	    String where = "WHERE mapa='" + mapName + "'";

	    String[][] layersOV = dbs.getTable(DBStructure.getOverviewTable(), DBStructure.getSchema(), where, new String[]{"posicion"}, false);

	    for (int i = 0; i < layersOV.length; i++) {
		String schema = null;
		if (layersOV[i][2].length() > 0) {
		    schema = layersOV[i][2];
		}

		LayerProperties lp = null;
		try {
		    lp = new LayerProperties(schema, layersOV[i][4], layersOV[i][1]);
		} catch (IndexOutOfBoundsException e) {
		    lp = new LayerProperties(schema, layersOV[i][1], layersOV[i][1]);
		}

		int position = 0;
		try {
		    position = Integer.parseInt(layersOV[i][3]);
		} catch (NumberFormatException e) {
		    //do nothing
		}

		lp.setPosition(position);

		overviewLayers.add(lp);

	    }
	}
	return overviewLayers;
    }

    /**
     * Get layers querying on '_map' table to the MapView.
     * Get layers querying on '_map_overview' table to the MapOverView.
     *
     * _MAP SCHEMA:
     * 0.- mapa character varying(255) NOT NULL,
     * 1.- nombre_capa character varying(255) NOT NULL,
     * 2.- nombre_tabla character varying(255),
     * 3.- posicion integer NOT NULL DEFAULT 0,
     * 4.- visible boolean,
     * 5.- max_escala character varying(50),
     * 6.- min_escala character varying(50),
     * 7.- grupo character varying,
     * 8.- "schema" character varying,
     * 9.- localizador boolean
     *
     *
     *
     * @param view
     * @param mapName
     * @param proj
     * @param stylesSource must fit with LoadLegend's NO_LEGEND, DB_LEGEND or FILE_LEGEND
     * @param stylesName
     * @throws Exception
     */
    public ELLEMap getMap(IView view, String mapName, int stylesSource,
	    String stylesName) throws Exception {

	ELLEMap map = new ELLEMap(mapName, view);

	List<LayerProperties> viewLayers = getViewLayers(mapName);
	for (LayerProperties lp : viewLayers) {
	    map.addLayer(lp);
	}

	/////////////// MapOverview
	List<LayerProperties> overviewLayers = getOverviewLayers(mapName);
	for (LayerProperties lp : overviewLayers) {
	    map.addOverviewLayer(lp);
	}

	map.setStyleSource(stylesSource);
	map.setStyleName(stylesName);

	return map;

    }

    public void addLoadedMap(ELLEMap map) {
	loadedMaps.add(map);
    }

    public void removeLoadedMap(ELLEMap map) {
	loadedMaps.remove(map);
    }


    /**
     * @param view
     * @param mapName
     * @return
     * @throws SQLException
     */
    public  boolean isMapLoaded(IView view, String mapName) throws SQLException {

	for (ELLEMap map : loadedMaps) {
	    if (map.getView() == view && map.getName().equals(mapName)) {
		return true;
	    }
	}
	return false;

    }

    public List<ELLEMap> getLoadedMaps() {
	return loadedMaps;
    }

    public  boolean mapExists(String mapName) throws SQLException {

	String[] maps = getMaps();
	for (String map : maps) {
	    if (mapName.equals(map)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Saves the map. If the maps already exists, it'll be overwritten.
     * @param rows
     * @param mapName
     * @throws SQLException
     */
    public  void saveMap(Object[][] rows, String mapName) throws SQLException {

	String auxMapName = "__aux__" + Double.toString(Math.random()*100000).trim();
	DBSession dbs = DBSession.getCurrentSession();
	for (Object[] row : rows) {
	    if (row.length == 8 || row.length == 9) {

		Object[] rowToSave = new Object[10];
		rowToSave[0] = auxMapName;
		for (int i=0; i<row.length; i++) {
		    rowToSave[i+1] = row[i];
		}
		rowToSave[9] = null;

		try {
		    dbs.insertRow(DBStructure.getSchema(), DBStructure.getMapTable(), rowToSave);
		} catch (SQLException e) {
		    // undo insertions
		    try {
			dbs = DBSession.reconnect();
			dbs.deleteRows(DBStructure.getSchema(), DBStructure.getMapTable(), "where mapa='" + auxMapName + "'");
			throw new SQLException(e);
		    } catch (DataException e1) {
			e1.printStackTrace();
		    }
		}
	    }
	}
	//remove previous entries and rename aux table
	dbs.deleteRows(DBStructure.getSchema(), DBStructure.getMapTable(), "where mapa='" + mapName + "'");
	dbs.updateRows(DBStructure.getSchema(), DBStructure.getMapTable(), new String[]{"mapa"}, new String[]{mapName}, "where mapa='" + auxMapName + "'");
    }

    public  void saveMapOverview(Object[][] rows, String mapName) throws SQLException {

	String auxMapname = "__aux__" + Double.toString(Math.random()*100000).trim();
	DBSession dbs = DBSession.getCurrentSession();
	for (int j=0; j<rows.length; j++) {
	    if (rows[j].length == 3 || rows[j].length == 4) {


		/* fpuga: This is hack. Previously _map_overview doesn't have a nombre_tabla column, so we must ensure
		 * compatibility with previous versions.
		 * Also, a more accurate approach will be have a hashmap 'columnName = valueName' because using this code
		 * if the order of the columns changes it will not work.
		 */
		String[] columns = dbs.getColumns(DBStructure.getSchema(), DBStructure.getOverviewTable());

		//_map_overview structure: mapName, tablename, schema, position, [layername]
		Object[] rowToSave = new Object[columns.length];
		rowToSave[0] = auxMapname;
		rowToSave[2] = rows[j][1]; // schema
		rowToSave[3] = rows[j][3]; // position

		if (columns.length == 5) {
		    rowToSave[4] = rows[j][0]; // tableName
		    rowToSave[1] = rows[j][2]; // layerName
		} else {
		    rowToSave[1] =  rows[j][0]; // tablename
		}

		
		dbs.insertRow(DBStructure.getSchema(), DBStructure.getOverviewTable(), columns, rowToSave);
		
	    }
	}
	//remove previous entries and rename aux table
	dbs.deleteRows(DBStructure.getSchema(), DBStructure.getOverviewTable(), "where mapa='" + mapName + "'");
	dbs.updateRows(DBStructure.getSchema(), DBStructure.getOverviewTable(), new String[]{"mapa"}, new String[]{mapName}, "where mapa='" + auxMapname + "'");

    }

    /**
     * 
     * @return false if a SQLException is thrown, true otherwise (schema dropped
     *         or nothing to do if not exists)
     */
    public boolean dropSchema() {
	if(hasSchema()) {
	    DBSession dbs = DBSession.getCurrentSession();
	    Connection con = dbs.getJavaConnection();
	    String sqlCreateSchema = "DROP SCHEMA " + DBStructure.getSchema()
		    + " CASCADE";
	    try {
		Statement stat = con.createStatement();
		stat.execute(sqlCreateSchema);
		con.commit();
	    } catch (SQLException e) {
		e.printStackTrace();
		return false;
	    }
	}
	return true;
    }

    public void createSchema() {
	if (!hasSchema()) {
	    DBSession dbs = DBSession.getCurrentSession();
	    dbs.createSchema(DBStructure.getSchema());
	}
    }

    /**
     * 
     * @return true if schema exists, false otherwise (if not exists or
     *         SQLException is thrown)
     */
    public boolean hasSchema() {
	DBSession dbs = DBSession.getCurrentSession();
	return dbs.schemaExists(DBStructure.getSchema());
    }

    public  void createMapTables() throws SQLException {

	boolean commit = false;

	DBSession dbs = DBSession.getCurrentSession();

	String sqlCreateMap = "CREATE TABLE " + DBStructure.getSchema() +"."+DBStructure.getMapTable()+" "
		+ "("
		+ "   mapa character varying(255) NOT NULL,"
		+ "   nombre_capa character varying(255) NOT NULL,"
		+ "   nombre_tabla character varying(255),"
		+ "   posicion integer NOT NULL DEFAULT 0,"
		+ "   visible boolean,"
		+ "   max_escala character varying(50),"
		+ "   min_escala character varying(50),"
		+ "   grupo character varying,"
		+ "   \"schema\" character varying,"
		+ "   localizador boolean,"
		+ "   PRIMARY KEY (mapa, nombre_capa)"
		+ ")";

	String sqlCreateMapOverview =  "CREATE TABLE " + DBStructure.getSchema() + "."+DBStructure.getOverviewTable()
		+ "("
		+ "  mapa character varying NOT NULL,"
		+ "  nombre_capa character varying NOT NULL,"
		+ "  \"schema\" character varying,"
		+ "  posicion integer,"
		+ "  nombre_tabla character varying,"
		+ "  PRIMARY KEY (mapa, nombre_capa)"
		+ ")";

	String sqlGrant = "GRANT SELECT ON TABLE " + DBStructure.getSchema() + ".%s TO public";

	Connection con = dbs.getJavaConnection();
	Statement stat = con.createStatement();


	if (!dbs.tableExists(DBStructure.getSchema(), DBStructure.getMapTable())) {
	    createSchema();
	    stat.execute(sqlCreateMap);
	    stat.execute(String.format(sqlGrant, DBStructure.getMapTable()));
	}

	if (!dbs.tableExists(DBStructure.getSchema(), DBStructure.getOverviewTable())) {
	    createSchema();
	    stat.execute(sqlCreateMapOverview);
	    stat.execute(String.format(sqlGrant, DBStructure.getOverviewTable()));

	}

	con.close();
    }

    public  void deleteMap(String mapName) throws SQLException {
	DBSession dbs = DBSession.getCurrentSession();
	dbs.deleteRows(DBStructure.getSchema(), DBStructure.getMapTable(),
		"WHERE mapa='" + mapName + "'");
	dbs.deleteRows(DBStructure.getSchema(), DBStructure.getOverviewTable(),
		"WHERE mapa='" + mapName + "'");
    }

    public String[] getMaps() throws SQLException {
	String[] maps = DBSession.getCurrentSession().getDistinctValues(
		DBStructure.getMapTable(), DBStructure.getSchema(), "mapa");
	return maps;
    }

}

