package es.icarto.gvsig.elle.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iver.cit.gvsig.fmap.featureiterators.FeatureIteratorTest;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.utils.MapDAO;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class ELLETests {

    static final String fwAndamiDriverPath = "../_fwAndami/gvSIG/extensiones/com.iver.cit.gvsig/drivers";
    private static File baseDataPath;
    private static File baseDriversPath;

    @Before
    public void connectToDatabase() throws Exception {
	doSetup();
	DBSession.createConnection("localhost", 5432, "elle",
		"public", "postgres", "postgres");
	MapDAO.getInstance().dropSchema();
    }

    @Test
    public void testCreateSchema() {
	assertEquals(true, MapDAO.getInstance().createSchema());
    }

    @Test
    public void testCreateMap() throws SQLException {
	createMap();
	assertEquals(true, MapDAO.getInstance().mapExists("test_map"));
    }

    private boolean createMap() {
	List<Object[]> rows = new ArrayList<Object[]>();
	Object[] row = { "Carreteras",
		"rede_carreteras",
		"1",
		true,
		null,
		null,
		"",
	"inventario"};
	rows.add(row);
	try {
	    DBSession dbs = DBSession.getCurrentSession();
	    if (!dbs.tableExists(DBStructure.getSchema(),
		    DBStructure.getMapTable())) {
		MapDAO.getInstance().createMapTables();
	    }
	    MapDAO.getInstance().saveMap(rows.toArray(new Object[0][0]),
		    "test_map");
	    return true;
	} catch (SQLException e) {
	    e.printStackTrace();
	    return false;
	}
    }

    private void doSetup() throws Exception {
	URL url = FeatureIteratorTest.class.getResource("testdata");
	if (url == null) {
	    throw new Exception(
		    "No se encuentra el directorio con datos de prueba");
	}

	baseDataPath = new File(url.getFile());
	if (!baseDataPath.exists()) {
	    throw new Exception(
		    "No se encuentra el directorio con datos de prueba");
	}

	baseDriversPath = new File(fwAndamiDriverPath);
	if (!baseDriversPath.exists()) {
	    throw new Exception("Can't find drivers path: "
		    + fwAndamiDriverPath);
	}

	LayerFactory.setDriversPath(baseDriversPath.getAbsolutePath());
	if (LayerFactory.getDM().getDriverNames().length < 1) {
	    throw new Exception("Can't find drivers in path: "
		    + fwAndamiDriverPath);
	}
    }

}
