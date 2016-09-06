/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coruï¿½a
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gvsig.andami.PluginServices;
import org.gvsig.fmap.mapcontext.exceptions.LegendLayerException;
import org.gvsig.fmap.mapcontext.exceptions.WriteLegendException;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.ILegend;
import org.gvsig.fmap.mapcontext.rendering.legend.IVectorLegend;
import org.gvsig.fmap.mapcontext.rendering.legend.driver.ILegendReader;
import org.gvsig.fmap.mapcontext.rendering.legend.driver.ILegendWriter;
import org.gvsig.fmap.mapcontext.rendering.legend.styling.ILabelingStrategy;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.driver.impl.PersistenceBasedLegendReader;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.driver.impl.PersistenceBasedLegendWriter;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.styling.LabelingFactory;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.exception.BaseException;
import org.gvsig.tools.persistence.PersistenceManager;
import org.gvsig.tools.persistence.xml.XMLPersistenceManager;
import org.gvsig.utils.XMLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.EllePreferencesPage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

/**
 * This ELLE class can load legends (styles) on the layers. This styles are
 * 'gvl' files placed on a tmpFolder defined by the user on the config panel.
 */
public class LoadLegend {

	
	private static final Logger logger = LoggerFactory
			.getLogger(LoadLegend.class);
	
    public static final int NO_LEGEND = 0;
    public static final int DB_LEGEND = 1;
    public static final int FILE_LEGEND = 2;

    private static String legendPath;
    private static HashMap<String, Class<? extends ILegendReader>> readDrivers = new HashMap<String, Class<? extends ILegendReader>>();
    private static HashMap<String, Class<? extends ILegendWriter>> writeDrivers = new HashMap<String, Class<? extends ILegendWriter>>();
    private static String configLegendDir;

    
    static {
    	readDrivers.put("gvl", PersistenceBasedLegendReader.class);
    	readDrivers.put("sld", PersistenceBasedLegendReader.class); // FIXME
    	writeDrivers.put("gvl", PersistenceBasedLegendWriter.class);
    	writeDrivers.put("sld", PersistenceBasedLegendWriter.class); // FIXME

	//get config
	XMLEntity xml = PluginServices.getPluginServices("es.udc.cartolab.gvsig.elle").getPersistentXML();
	if (xml.contains(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME)) {
	    configLegendDir = xml.getStringProperty(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME);
	    if (!configLegendDir.endsWith(File.separator)) {
		configLegendDir = configLegendDir + File.separator;
	    }
	}
    }

	private final XMLPersistenceManager persistenceManager;

    public LoadLegend() {
		this.persistenceManager = initXMLPersistenceManager();
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
		if (readDrivers.containsKey(ext.toLowerCase())) {
		    ILegendReader driver = readDrivers.get(ext.toLowerCase()).newInstance();
		    ILegend legend = driver.read(legendFile, lyr.getShapeType());
		    
		    
		    if (legend != null && legend instanceof IVectorLegend) {
			lyr.setLegend((IVectorLegend)legend);
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

    public static void saveLegend(FLyrVect layer, File legendFile) {
	String ext = legendFile.getName().substring(legendFile.getName().lastIndexOf('.') +1);
	if (readDrivers.containsKey(ext.toLowerCase())) {
	    try {
		ILegendWriter driver = writeDrivers.get(ext.toLowerCase()).newInstance();
		
		driver.write(layer.getLegend(), legendFile, "gvsleg");
		
	    } catch (InstantiationException e) {
	    	logger.error(e.getMessage(), e);
	    } catch (IllegalAccessException e) {
	    	logger.error(e.getMessage(), e);
	    } catch (WriteLegendException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
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
	for (String ext : readDrivers.keySet()) {
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
		+ "  definicion xml,"
		+ "  label xml"
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
		+ "  label xml"
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
	Set<String> set = readDrivers.keySet();
	Iterator<String> it = set.iterator();
	while (it.hasNext()) {
	    String aux = it.next().toLowerCase();
	    if (!type.equals(aux)) {
		result.add(aux);
	    }
	}

	return result;

    }

    public void loadDBLegend(FLyrVect layer, String styleName, boolean overview) {
    	String table;
    	if (overview) {
    	    table = DBStructure.getOverviewStyleTable();
    	} else {
    	    table = DBStructure.getMapStyleTable();
    	}

    	DBSession dbs = DBSession.getCurrentSession();
    	String layerName = layer.getName();
    	String[][] style = new String[0][0];
		try {
			style = dbs.getTable(table, DBStructure.getSchema(),
				"where nombre_capa='" + layerName + "' and nombre_estilo='"
					+ styleName + "'");
		} catch (SQLException e1) {
			logger.error(e1.getMessage(), e1);
		}
    	if (style.length != 1) {
    		return;
    	}
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
    	    try {
    	    	InputStream is = new ByteArrayInputStream(def.getBytes("UTF-8"));
        	    IVectorLegend legend = (IVectorLegend) persistenceManager.getObject(is);
            	is.close();
        		layer.setLegend(legend);
    	    } catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (BaseException e) {
				logger.error(e.getMessage(), e);
			}
    	    
    	
    	setLabel(layer, style[0][4]);
    }
    
    @Deprecated
    public static void loadstaticDBLegend(FLyrVect layer, String styleName, boolean overview) throws SQLException, IOException {

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

//		setLabel(layer, style[0][4]);

	}
    }

    public boolean setLabel(FLyrVect layer, String label) {
    	
    	if ((label != null) && (label.length() > 0)) {
			try {
				InputStream isLb = new ByteArrayInputStream(label.getBytes("UTF-8"));
				ILabelingStrategy labelStrategy = (ILabelingStrategy) persistenceManager.getObject(isLb);
				layer.setLabelingStrategy(labelStrategy);
				layer.setIsLabeled(true);
				return true;
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e);
			}
		}
    	return false;
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

//    public static void loadLegend(FLyrVect layer, String styleName, boolean overview, int source) throws SQLException, IOException {
//
//	switch (source) {
//	case DB_LEGEND : loadDBLegend(layer, styleName, overview);
//	break;
//	case FILE_LEGEND : loadFileLegend(layer, styleName, overview);
//	break;
//
//	}
//
//	for (ELLEMap map : MapDAO.getInstance().getLoadedMaps()) {
//	    if (map.layerInMap(layer.getName())) {
//		map.setStyleSource(source);
//		map.setStyleName(styleName);
//	    }
//	}
//
//    }

}
