/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coru?a
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gvsig.symbology.fmap.drivers.sld.FMapSLDDriver;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.gvl.FMapGVLDriver;
import com.iver.cit.gvsig.fmap.drivers.legend.IFMapLegendDriver;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;

import es.icarto.gvsig.elle.db.DBStructure;
import es.icarto.gvsig.elle.style.LayerLabeling;
import es.icarto.gvsig.elle.style.LayerSimbology;
import es.udc.cartolab.gvsig.elle.gui.EllePreferencesPage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

/**
 * This ELLE class can load legends (styles) on the layers. This styles are
 * 'gvl' files placed on a folder defined by the user on the config panel.
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

    private static boolean setLegend(FLyrVect layer, File legendFile){
        try {
            new LayerSimbology(layer).set(legendFile);
            return true;
        } catch (LegendDriverException e) {
            return false;
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
	String whereClause = "WHERE nombre_estilo='" + legendsName + "'";
	dbs.deleteRows(DBStructure.getSchema(), DBStructure.getMapStyleTable(), whereClause);
	dbs.deleteRows(DBStructure.getSchema(), DBStructure.getOverviewStyleTable(), whereClause);
    }

    public static String[] getLegends() throws SQLException {
	String[] legends = DBSession.getCurrentSession().getDistinctValues(
		DBStructure.getMapStyleTable(), DBStructure.getSchema(),
		"nombre_estilo");
	return legends;
    }

    public static boolean legendExistsDB(String legendName) throws SQLException {

	DBSession dbs = DBSession.getCurrentSession();
	String[] legends = dbs.getDistinctValues(
		DBStructure.getMapStyleTable(), DBStructure.getSchema(),
		"nombre_estilo");
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
		+ "  definicion text,"
		+ "  label text,"
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
		+ "  definicion text,"
		+ "  label text,"
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
	String[][] style = dbs.getTable(table, DBStructure.getSchema(),
		"where nombre_capa='" + layerName + "' and nombre_estilo='"
			+ styleName + "'");
	if (style.length == 1) {
	    String type = style[0][2];
	    String def = style[0][3];
	    // gvSIG uses an old postresql jar. This jar escapes \ characters 
	    // (converting it in \\) before commiting to the server, but in
	    // postgresql version > 9.1 the default behavior is not escape
	    // this characters.
	    // http://www.postgresql.org/docs/9.1/static/sql-syntax-lexical.html#SQL-SYNTAX-STRINGS-ESCAPE
	    // So when reading the jar expected only a \ and it 
	    // receives \\
	    def = def.replace("\\\\", "\\");
	    
	    File tmpLegend = File.createTempFile("style", layerName + "." + type);
	    FileWriter writer = new FileWriter(tmpLegend);
	    writer.write(def);
	    writer.close();
	    setLegend(layer, tmpLegend.getAbsolutePath(), true);

	    // fpuga. April 15, 2014
	    // This if is here only for compatible reasons with old version of
	    // ELLE without support for labelling. The if can be safely
	    // removed when all projects have added to the database a "label"
	    // column to _map_style and _map_overview_style
	    if (style[0].length == 5) {
	        new LayerLabeling(layer).set(style[0][4]);
	    }
	}
    }


    private static void loadFileLegend(FLyrVect layer, String styleName, boolean overview) {

	if (setLegendStyleName(styleName)) {
	    if (overview) {
		setOverviewLegend(layer, (String)null);
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
