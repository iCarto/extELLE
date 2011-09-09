package es.udc.cartolab.gvsig.elle;

/**
 * @author Nacho Varela <nachouve@gmail.com>
 * @author Francisco Puga <fpuga@cartolab.es>
 */
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.drivers.wms.FMapWMSDriver;
import com.iver.cit.gvsig.fmap.drivers.wms.FMapWMSDriverFactory;
import com.iver.cit.gvsig.fmap.layers.FLyrWMS;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.udc.cartolab.gvsig.users.utils.DBSession;

public class EasyWMSExtension extends Extension {

    private static final String WMS_TABLENAME = "_wms";
    private static final String WMS_COLUMN_ID = "id_wms";
    private static final String WMS_ID = "1";

    public void execute(String actionCommand) {

	DBSession dbs = DBSession.getCurrentSession();
	String whereC = WMS_COLUMN_ID + "=" + "'" + WMS_ID + "'";
	// columns in order from wms table: id_wms[0], layer_name[1], layer[2],
	// srs[3], format[4], host[5]
	String[][] wmsValues = null;
	try {
	    wmsValues = dbs.getTable(WMS_TABLENAME, dbs.getSchema(), whereC);
	} catch (SQLException e2) {
	    // TODO Auto-generated catch block
	    e2.printStackTrace();
	}

	// / TODO Read from a file a request and parse it
	String host = wmsValues[0][5];
	URL url = null;
	try {
	    url = new URL(host);
	} catch (MalformedURLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	    return;
	}
	String sLayer = wmsValues[0][2];
	String srs = wmsValues[0][3];
	String format = wmsValues[0][4];
	FLyrWMS layer = new FLyrWMS();

	try {
	    layer.setHost(url);
	    layer.setFullExtent(new Rectangle2D.Float(430819, 4623297, 286459,
		    232149));

	    layer.setFormat(format);
	    layer.setLayerQuery(sLayer);
	    layer.setInfoLayerQuery(sLayer);
	    layer.setSRS(srs);
	    layer.setName(wmsValues[0][1]);
	    layer.setWmsTransparency(true);
	    Vector styles = new Vector();
	    String[] sLayers = sLayer.split(",");
	    for (int i = 0; i < sLayers.length; i++) {
		styles.add("planos_tif_bn");
	    }
	    layer.setStyles(styles);
	    // layer.setDimensions(getDimensions());
	    // "gvSIG Raster Driver";
	    FMapWMSDriver driver;
	    driver = FMapWMSDriverFactory.getFMapDriverForURL(url);
	    layer.setDriver(driver);

	    Hashtable online_resources = new Hashtable();
	    online_resources.put("GetFeatureInfo", wmsValues[0][5]);
	    online_resources.put("GetMap", wmsValues[0][5]);
	    layer.setOnlineResources(online_resources);
	    layer.setFixedSize(new Dimension(-1, -1));
	    layer.setQueryable(false);

	    layer.setVisible(true);

	    // //////////////////
	    // TODO Fix bug when try to edit WMS Layer
	    // try {
	    // WMSWizardData dataSource = new WMSWizardData();
	    // dataSource.setHost(url, true);
	    // getTreeLayers().setModel(new
	    // LayerTreeModel(dataSource.getLayer()));
	    // } catch (DriverException e) {
	    // // TODO Auto-generated catch block
	    // e.printStackTrace();
	    // }
	    //
	    // ///////////////
	    View v = (View) PluginServices.getMDIManager().getActiveWindow();
	    MapContext mapContext = v.getModel().getMapContext();

	    mapContext.getLayers().addLayer(0, layer);

	} catch (ConnectException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (MalformedURLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void initialize() {
	PluginServices.getIconTheme().registerDefault(
		"planos_wms",
		this.getClass().getClassLoader().getResource(
			"images/planos_wms.png"));

    }

    public boolean isEnabled() {
	return PluginServices.getMDIManager().getActiveWindow() instanceof View;
    }

    public boolean isVisible() {
	return PluginServices.getMDIManager().getActiveWindow() instanceof View;
    }

}
