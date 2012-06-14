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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gvsig.symbology.fmap.drivers.sld.FMapSLDDriver;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.gvl.FMapGVLDriver;
import com.iver.cit.gvsig.fmap.drivers.legend.IFMapLegendDriver;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.utiles.XMLEntity;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.EllePreferencesPage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

/**
 * This ELLE class can load legends (styles) on the layers. This styles are  'gvl' files placed on a folder defined by the user
 * on the config panel.
 */
public abstract class LoadLegend {

	public static final int NO_LEGEND = 0;
	public static final int DB_LEGEND = 1;
	public static final int FILE_LEGEND = 2;

	private static String legendPath;
	private static HashMap<String, Class<? extends IFMapLegendDriver>> drivers = new HashMap<String, Class<? extends IFMapLegendDriver>>();
	private static String configLegendDir;

	static {
		drivers.put("gvl", FMapGVLDriver.class);
		drivers.put("sld", FMapSLDDriver.class);

		//get config
		XMLEntity xml = PluginServices.getPluginServices("es.udc.cartolab.gvsig.elle").getPersistentXML();
		if (xml.contains(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME)) {
			configLegendDir = xml.getStringProperty(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME);
			if (!configLegendDir.endsWith(File.separator)) {
				configLegendDir = configLegendDir + File.separator;
			}
		}

	}

	public static boolean setLegendStyleName(String stylesName) {
		if (configLegendDir!=null) {
			File f = new File(configLegendDir + stylesName);
			if (f.exists() && f.isDirectory()) {
				legendPath = configLegendDir + stylesName + File.separator;
				return true;
			}
		}
		return false;
	}

	public static String getLegendPath(){
		return legendPath;
	}

	public static String getOverviewLegendPath(){
		return legendPath + "overview" + File.separator;
	}

	private static boolean setLegend(FLyrVect lyr, File legendFile){

		if (lyr == null) {
			System.out.println("[LoadLegend] La capa es null: " + lyr + " legend: " + legendFile);
			return false;
		}

		if (legendFile.exists()){

			String ext = legendFile.getName().substring(legendFile.getName().lastIndexOf('.') +1);
			try {
				if (drivers.containsKey(ext.toLowerCase())) {
					IFMapLegendDriver driver = drivers.get(ext.toLowerCase()).newInstance();
					Hashtable<FLayer, ILegend> table = driver.read(lyr.getMapContext().getLayers(),lyr, legendFile);
					ILegend legend = table.get(lyr);
					if (legend != null && legend instanceof IVectorLegend) {
						lyr.setLegend((IVectorLegend)table.get(lyr));
						System.out.println("Cargado el style: "+ legendFile.getAbsolutePath());
						return true;
					}
				} else {
					System.out.println("Tipo de leyenda no soportado");

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("No existe el style: "+ legendFile.getAbsolutePath());

		}
		return false;

	}

	public static void saveLegend(FLyrVect layer, File legendFile) throws LegendDriverException {
		String ext = legendFile.getName().substring(legendFile.getName().lastIndexOf('.') +1);
		if (drivers.containsKey(ext.toLowerCase())) {
			try {
				IFMapLegendDriver driver = drivers.get(ext.toLowerCase()).newInstance();
				//workaround for driver version... we hope that when supportedVersions array grows (it has one element
				//for gvl and sld), gvsIG people will put the newer versions at the last position
				ArrayList<String> supportedVersions = driver.getSupportedVersions();
				String version = supportedVersions.get(supportedVersions.size()-1);
				driver.write(layer.getMapContext().getLayers(),layer, layer.getLegend(), legendFile, version);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void setOverviewLegend(FLyrVect lyr, String legendFilename){

		if (legendFilename == null) {
			legendFilename = lyr.getName();
		}

		setLegend(lyr, getOverviewLegendPath() + legendFilename, true);


	}

	public static boolean setLegend(FLyrVect lyr, String legendFilename, boolean absolutePath){

		if (legendFilename == null) {
			legendFilename = lyr.getName();
		}
		if (!absolutePath) {
			legendFilename = getLegendPath() + legendFilename;
		}
		File legendFile;
		if (!hasExtension(legendFilename)) {
			legendFile = new File(legendFilename + ".gvl");
			if (!setLegend(lyr, legendFile)) {
				legendFile = new File(legendFilename + ".sld");
				return setLegend(lyr, legendFile);
			} else {
				return true;
			}
		} else {
			legendFile = new File(legendFilename);
			return setLegend(lyr, legendFile);
		}

	}

	public static void setLegend(FLyrVect lyr){
		//prioridad gvl
		if (!setLegend(lyr, lyr.getName() + ".gvl", false)) {
			setLegend(lyr, lyr.getName() + ".sld", false);
		}
	}

	public static void setOverviewLegend(FLyrVect lyr){
		setOverviewLegend(lyr, (String)null);
	}

	private static boolean hasExtension(String fileName) {
		for (String ext : drivers.keySet()) {
			if (fileName.toLowerCase().endsWith("." + ext.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static void deleteLegends(String legendsName) throws SQLException {
		DBSession dbs = DBSession.getCurrentSession();
		String removeMap = "DELETE FROM " + DBStructure.getSchema() + "."+DBStructure.getMapStyleTable()+" WHERE nombre_estilo=?";
		String removeMapOverview = "DELETE FROM " + DBStructure.getSchema() + "."+DBStructure.getOverviewStyleTable()+" WHERE nombre_estilo=?";

		PreparedStatement ps = dbs.getJavaConnection().prepareStatement(removeMap);
		ps.setString(1, legendsName);
		ps.executeUpdate();
		ps.close();

		ps = dbs.getJavaConnection().prepareStatement(removeMapOverview);
		ps.setString(1, legendsName);
		ps.executeUpdate();
		ps.close();

		dbs.getJavaConnection().commit();
	}

	public static boolean legendExistsDB(String legendName) throws SQLException {

		DBSession dbs = DBSession.getCurrentSession();
		String[] legends = dbs.getDistinctValues(DBStructure.getMapStyleTable(), "nombre_estilo");
		boolean found = false;
		for (int i=0; i<legends.length; i++) {
			if (legendName.equals(legends[i])) {
				found = true;
				break;
			}
		}
		return found;

	}

	public static void createLegendtables() throws SQLException {

		boolean commit = false;

		DBSession dbs = DBSession.getCurrentSession();

		String sqlCreateMapStyle =  "CREATE TABLE " + DBStructure.getSchema() + "."+DBStructure.getMapStyleTable()
		+ "("
		+ "  nombre_capa character varying NOT NULL,"
		+ "  nombre_estilo character varying NOT NULL,"
		+ "  type character varying(3),"
		+ "  definicion xml,"
		+ "  PRIMARY KEY (nombre_capa, nombre_estilo)"
		+ ")"
		+ "WITH ("
		+ "  OIDS=FALSE"
		+ ")";

		String sqlCreateMapOverviewStyle = "CREATE TABLE " + DBStructure.getSchema() + "."+DBStructure.getOverviewStyleTable()
		+ "("
		+ "  nombre_capa character varying NOT NULL,"
		+ "  nombre_estilo character varying NOT NULL,"
		+ "  tipo character varying(3),"
		+ "  definicion xml,"
		+ "  PRIMARY KEY (nombre_capa, nombre_estilo)"
		+ ")";

		String sqlGrant = "GRANT SELECT ON TABLE " + DBStructure.getSchema() + ".%s TO public";

		Connection con = dbs.getJavaConnection();
		Statement stat = con.createStatement();

		if (!dbs.tableExists(DBStructure.getSchema(), DBStructure.getMapStyleTable())) {
			stat.execute(sqlCreateMapStyle);
			stat.execute(String.format(sqlGrant, DBStructure.getMapStyleTable()));
			commit = true;
		}

		if (!dbs.tableExists(DBStructure.getSchema(), DBStructure.getOverviewStyleTable())) {
			stat.execute(sqlCreateMapOverviewStyle);
			stat.execute(String.format(sqlGrant, DBStructure.getMapStyleTable()));
			commit = true;
		}

		if (commit) {
			con.commit();
		}
	}


	public static List<String> getSortedPreferedLegendTypes() {

		ArrayList<String> result = new ArrayList<String>();

		PluginServices ps = PluginServices.getPluginServices("es.udc.cartolab.gvsig.elle");
		XMLEntity xml = ps.getPersistentXML();

		String type = EllePreferencesPage.DEFAULT_LEGEND_FILE_TYPE;
		if (xml.contains(EllePreferencesPage.DEFAULT_LEGEND_FILE_TYPE_KEY_NAME)) {
			type = xml.getStringProperty(EllePreferencesPage.DEFAULT_LEGEND_FILE_TYPE_KEY_NAME).toLowerCase();
		}

		result.add(type);
		Set<String> set = drivers.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String aux = it.next().toLowerCase();
			if (!type.equals(aux)) {
				result.add(aux);
			}
		}

		return result;

	}

	private static void loadDBLegend(FLyrVect layer, String styleName, boolean overview) throws SQLException, IOException {

		String table;
		if (overview) {
			table = DBStructure.getOverviewStyleTable();
		} else {
			table = DBStructure.getMapStyleTable();
		}

		DBSession dbs = DBSession.getCurrentSession();
		String layerName = layer.getName();
//		String styleName = dbCB.getSelectedItem().toString();
		String[][] style = dbs.getTable(table, "where nombre_capa='" + layerName + "' and nombre_estilo='" + styleName + "'");
		if (style.length == 1) {
			String type = style[0][2];
			String def = style[0][3];

			File tmpLegend = File.createTempFile("style", layerName + "." + type);
			FileWriter writer = new FileWriter(tmpLegend);
			writer.write(def);
			writer.close();
			setLegend(layer, tmpLegend.getAbsolutePath(), true);
		}
	}

	private static void loadFileLegend(FLyrVect layer, String styleName, boolean overview) {

		if (setLegendStyleName(styleName)) {
			if (overview) {
				setOverviewLegend(layer);
			} else {
				setLegend(layer);
			}
		}
	}

	public static void loadLegend(FLyrVect layer, String styleName, boolean overview, int source) throws SQLException, IOException {

		switch (source) {
		case DB_LEGEND : loadDBLegend(layer, styleName, overview);
		break;
		case FILE_LEGEND : loadFileLegend(layer, styleName, overview);
		break;

		}

		for (ELLEMap map : MapDAO.getInstance().getLoadedMaps()) {
			if (map.layerInMap(layer.getName())) {
				map.setStyleSource(source);
				map.setStyleName(styleName);
			}
		}

	}

}
