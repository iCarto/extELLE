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
import java.util.Hashtable;
import java.util.Vector;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.drivers.wms.FMapWMSDriver;
import com.iver.cit.gvsig.fmap.drivers.wms.FMapWMSDriverFactory;
import com.iver.cit.gvsig.fmap.layers.FLyrWMS;
import com.iver.cit.gvsig.project.documents.view.gui.View;

public class EasyWMSExtension extends Extension {

	public void execute(String actionCommand) {

		/// TODO Read from a  file a request and parse it
		String host = "http://ideg.xunta.es/WMS/Request.aspx";
		URL url = null;
		try {
			url = new URL(host);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		String sLayer = "NOMECONCELLO,CONCELLO,PROVINCIA,ortofotos2003";
		String srs = "EPSG:23029";
		String format =  "image/jpeg";
		FLyrWMS layer = new FLyrWMS();

		try {
			layer.setHost(url);
			layer.setFullExtent(new Rectangle2D.Float(430819,  4623297,   286459,  232149));

			layer.setFormat(format);
			layer.setLayerQuery(sLayer);
			layer.setInfoLayerQuery(sLayer);
			layer.setSRS(srs);
			layer.setName("SITGA_WMS");
			layer.setWmsTransparency(true);
			Vector styles = new Vector();
			String[] sLayers = sLayer.split(",");
			for (int i = 0; i < sLayers.length; i++){
				styles.add("Default");
			}
			layer.setStyles(styles);
			//layer.setDimensions(getDimensions());
			//"gvSIG Raster Driver";
			FMapWMSDriver driver;
			driver = FMapWMSDriverFactory.getFMapDriverForURL(url);
			layer.setDriver(driver);

			Hashtable online_resources = new Hashtable();
			online_resources.put("GetFeatureInfo", "http://ideg.xunta.es/WMS/Request.aspx?");
			online_resources.put("GetMap", "http://ideg.xunta.es/WMS/Request.aspx?");
			layer.setOnlineResources(online_resources);
			layer.setFixedSize(new Dimension(-1, -1));
			layer.setQueryable(false);

			layer.setVisible(true);

			//			//////////////////
			// TODO Fix bug when try to edit WMS Layer
			//			try {
			//				WMSWizardData dataSource = new WMSWizardData();
			//				dataSource.setHost(url, true);
			//				getTreeLayers().setModel(new LayerTreeModel(dataSource.getLayer()));
			//			} catch (DriverException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}
			//
			//			///////////////
			View v = (View)PluginServices.getMDIManager().getActiveWindow();
			MapContext mapContext = v.getModel().getMapContext();

			mapContext.getLayers().addLayer(layer);

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
				"wms_sitga",
				this.getClass().getClassLoader().getResource("images/wms_sitga.png")
			);

	}

	public boolean isEnabled() {
		return PluginServices.getMDIManager().getActiveWindow() instanceof View;
	}

	public boolean isVisible() {
		return true;
	}

}
