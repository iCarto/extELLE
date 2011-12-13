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
package es.udc.cartolab.gvsig.elle.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.cresques.cts.IProjection;

import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.project.documents.view.gui.View;

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
			boolean visible) throws SQLException, DBException {
		DBSession dbs = DBSession.getCurrentSession();
		FLayer layer = null;

		if (dbs != null) {
			if (schema!=null) {
				layer = dbs.getLayer(layerName, tableName, schema, whereClause, proj);
			} else {
				layer = dbs.getLayer(layerName, tableName, whereClause, proj);
			}
			layer.setVisible(visible);
		}
		return layer;
	}

	public FLayer getLayer(LayerProperties lp, String whereClause, IProjection proj) throws SQLException, DBException {
		return getLayer(lp.getLayername(), lp.getTablename(), lp.getSchema(),
		lp.getSQLRestriction(), proj, lp.visible());
	}

	public ELLEMap getMap(View view, String mapName, String whereClause) throws Exception {
		return getMap(view, mapName, whereClause, LoadLegend.NO_LEGEND, "");
	}

	protected List<LayerProperties> getViewLayers(String mapName) throws SQLException {

		List<LayerProperties> viewLayers = new ArrayList<LayerProperties>();


		DBSession dbs = DBSession.getCurrentSession();
		if (dbs != null) {
			String where = "WHERE map_name='" + mapName + "'";


			/////////////// MapControl
	    String[][] layers = dbs.getTable("_map", dbs.getSchema(),
		    new String[] { "group_toc_name", // 0
			    "layer_toc_name", // 1
			    "toc_position", // 2
			    "visible", // 3
			    "max_scale", // 4
			    "min_scale", // 5
			    "schemaname", // 6
			    "layerdbname", // 7
			    "sql_restriction" // 8
		    }, where, new String[] { "toc_position" }, false);

			for (int i=0; i<layers.length; i++) {
				String schema=null;
		if (layers[i][6].length() > 0) {
		    schema = layers[i][6];
				}
		LayerProperties lp = new LayerProperties(schema, layers[i][7],
			layers[i][1]);

				boolean visible = true;
		if (!layers[i][3].equalsIgnoreCase("t")) {
					visible = false;
				}
				lp.setVisible(visible);

				double maxScale = -1;
				try {
		    maxScale = Double.parseDouble(layers[i][4]);
				} catch (NumberFormatException e) {
					//do nothing
				}
				if (maxScale > -1) {
					lp.setMaxScale(maxScale);
				}

				double minScale = -1;
				try {
		    minScale = Double.parseDouble(layers[i][5]);
				} catch (NumberFormatException e) {
					//do nothing
				}
				if (minScale > -1) {
					lp.setMinScale(minScale);
				}

				int position = 0;
				try {
		    position = Integer.parseInt(layers[i][2]);
				} catch (NumberFormatException e) {
					//do nothing
				}

				lp.setPosition(position);

		lp.setGroup(layers[i][0]);
		lp.setSQLRestriction(layers[i][8]);

				viewLayers.add(lp);
			}
		}

		return viewLayers;

	}

	protected List<LayerProperties> getOverviewLayers(String mapName) throws SQLException {

		List<LayerProperties> overviewLayers = new ArrayList<LayerProperties>();


		DBSession dbs = DBSession.getCurrentSession();
		if (dbs != null) {
			String where = "WHERE map_name='" + mapName + "'";

	    String[][] layersOV = dbs.getTable("_map_overview",
		    dbs.getSchema(), new String[] { "layer_toc_name", // 0
			    "toc_position", // 1
			    "schemaname", // 2
			    "layerdbname" // 3
		    }, where, new String[] { "toc_position" }, false);

			for (int i = 0; i < layersOV.length; i++) {
				String schema = null;
				if (layersOV[i][2].length() > 0) {
					schema = layersOV[i][2];
				}

		LayerProperties lp = new LayerProperties(schema,
			layersOV[i][3], layersOV[i][0]);

				int position = 0;
				try {
		    position = Integer.parseInt(layersOV[i][1]);
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
     *
     * @param lp
     *            The LayerProperties of the the layer we are processing
     * @param whereClause
     *            The general where clause get from the constants set to the map
     * @return a composed where clause to apply to the layer
     */
    private String getComposedWhereClauseForThisLayer(LayerProperties lp,
	    String whereClause) {
	String layerWhereClause = null;
	if (whereClause.length() != 0) {
	    if (lp.getSQLRestriction().length() != 0) {
		layerWhereClause = whereClause + " AND "
			+ lp.getSQLRestriction();
	    }
	} else if (lp.getSQLRestriction().length() != 0) {
	    layerWhereClause = " WHERE " + lp.getSQLRestriction();
	}
	return layerWhereClause;
    }

    /**
     * Get layers querying on '_map' table to the MapView. Get layers querying
     * on '_map_overview' table to the MapOverView.
     *
     * @param view
     * @param mapName
     * @param proj
     * @param whereClause
     * @param stylesSource
     *            must fit with LoadLegend's NO_LEGEND, DB_LEGEND or FILE_LEGEND
     * @param stylesName
     * @throws Exception
     */
	public ELLEMap getMap(View view, String mapName,
			String whereClause, int stylesSource, String stylesName) throws Exception {



		ELLEMap map = new ELLEMap(mapName, view);


		List<LayerProperties> viewLayers = getViewLayers(mapName);
		for (LayerProperties lp : viewLayers) {
	    lp.setSQLRestriction(getComposedWhereClauseForThisLayer(lp,
		    whereClause));
			map.addLayer(lp);
		}

		/////////////// MapOverview
		List<LayerProperties> overviewLayers = getOverviewLayers(mapName);
		for (LayerProperties lp : overviewLayers) {
	    lp.setSQLRestriction(getComposedWhereClauseForThisLayer(lp,
		    whereClause));
			map.addOverviewLayer(lp);
		}

		map.setStyleSource(stylesSource);
		map.setStyleName(stylesName);


		map.setWhereClause(whereClause);
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
	public  boolean isMapLoaded(View view, String mapName) throws SQLException {

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

				try {
					dbs.insertRow(dbs.getSchema(), "_map", rowToSave);
				} catch (SQLException e) {
					// undo insertions
					try {
						dbs = DBSession.reconnect();
			dbs.deleteRows(dbs.getSchema(), "_map",
				"where map_name='" + auxMapName + "'");
						throw new SQLException(e);
					} catch (DBException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		//remove previous entries and rename aux table
		dbs.deleteRows(dbs.getSchema(), "_map", "where map_name='" + mapName
				+ "'");
		dbs.updateRows(dbs.getSchema(), "_map", new String[] { "map_name" },
				new String[] { mapName }, "where map_name='" + auxMapName + "'");
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
				String[] columns = dbs.getColumns(dbs.getSchema(), "_map_overview");

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

				try {
					dbs.insertRow(dbs.getSchema(), "_map_overview", columns, rowToSave);
				} catch (SQLException e) {
					//undo insertions
					try {
						dbs = DBSession.reconnect();
						dbs.deleteRows(dbs.getSchema(), "_map_overview",
								"where map_name='" + auxMapname + "'");
						throw new SQLException(e);
					} catch (DBException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
		//remove previous entries and rename aux table
		dbs.deleteRows(dbs.getSchema(), "_map_overview", "where map_name='"
				+ mapName + "'");
		dbs.updateRows(dbs.getSchema(), "_map_overview",
				new String[] { "map_name" }, new String[] { mapName },
				"where map_name='" + auxMapname + "'");

	}

	public  void createMapTables() throws SQLException {

		boolean commit = false;

		DBSession dbs = DBSession.getCurrentSession();

		String sqlCreateMap = "CREATE TABLE " + dbs.getSchema() +"._map "
		+ "("
 + "   map_name character varying(255) NOT NULL,"
				+ "   group_toc_name character varying(255),"
				+ "   layer_toc_name character varying(255) NOT NULL,"
				+ "   toc_position integer NOT NULL DEFAULT 0,"
				+ "   visible boolean DEFAULT TRUE,"
				+ "   max_scale character varying(50),"
				+ "   min_scale character varying(50),"
				+ "   schemaName character varying(255) NOT NULL,"
				+ "   layerDBName character varying(255) NOT NULL,"
				+ "   sql_restriction character varying,"
				+ "   PRIMARY KEY (map_name, group_toc_name, layer_toc_name)"
		+ ")";

		String sqlCreateMapOverview =  "CREATE TABLE " + dbs.getSchema() + "._map_overview"
		+ "("
				+ "  map_name character varying(255) NOT NULL,"
				+ "  layer_toc_name character varying(255) NOT NULL,"
				+ "  toc_position integer NOT NULL DEFAULT 0,"
				+ "  schemaName character varying(255) NOT NULL,"
				+ "  layerDBName character varying(255) NOT NULL,"
				+ "  PRIMARY KEY (map_name, layer_toc_name)"
		+ ")";

		String sqlGrant = "GRANT SELECT ON TABLE " + dbs.getSchema() + ".%s TO public";

		Connection con = dbs.getJavaConnection();
		Statement stat = con.createStatement();

		if (!dbs.tableExists(dbs.getSchema(), "_map")) {
			stat.execute(sqlCreateMap);
			stat.execute(String.format(sqlGrant, "_map"));
			commit = true;
		}

		if (!dbs.tableExists(dbs.getSchema(), "_map_overview")) {
			stat.execute(sqlCreateMapOverview);
			stat.execute(String.format(sqlGrant, "_map_overview"));
			commit = true;
		}

		if (commit) {
			con.commit();
		}
	}

	public  void deleteMap(String mapName) throws SQLException {
		DBSession dbs = DBSession.getCurrentSession();
		String removeMap = "DELETE FROM " + dbs.getSchema()
				+ "._map WHERE map_name=?";
		String removeMapOverview = "DELETE FROM " + dbs.getSchema()
				+ "._map_overview WHERE map_name=?";

		PreparedStatement ps = dbs.getJavaConnection().prepareStatement(removeMap);
		ps.setString(1, mapName);
		ps.executeUpdate();
		ps.close();

		ps = dbs.getJavaConnection().prepareStatement(removeMapOverview);
		ps.setString(1, mapName);
		ps.executeUpdate();
		ps.close();

		dbs.getJavaConnection().commit();
	}

    public String[] getMaps() throws SQLException {
	String[] maps = DBSession.getCurrentSession().getDistinctValues("_map",
		"map_name");
	return maps;
    }


}

