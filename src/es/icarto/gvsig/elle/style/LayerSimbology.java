package es.icarto.gvsig.elle.style;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.gvsig.symbology.fmap.drivers.sld.FMapSLDDriver;
import org.gvsig.symbology.fmap.rendering.VectorFilterExpressionLegend;

import com.iver.cit.gvsig.exceptions.layers.LegendLayerException;
import com.iver.cit.gvsig.fmap.drivers.gvl.FMapGVLDriver;
import com.iver.cit.gvsig.fmap.drivers.legend.IFMapLegendDriver;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.VectorialFileAdapter;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.rendering.IClassifiedVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;

import es.icarto.gvsig.commons.utils.FileNameUtils;

public class LayerSimbology {

    private static final Logger logger = Logger.getLogger(LayerSimbology.class);

    private static HashMap<String, Class<? extends IFMapLegendDriver>> drivers = new HashMap<String, Class<? extends IFMapLegendDriver>>();
    static {
        drivers.put("gvl", FMapGVLDriver.class);
        drivers.put("sld", FMapSLDDriver.class);
    }

    private final FLyrVect layer;

    public LayerSimbology(FLyrVect layer) {
        this.layer = layer;
    }

    public void save() throws LegendDriverException {
        if (layer.getSource() instanceof VectorialFileAdapter) {
            File file = ((VectorialFileAdapter) layer.getSource()).getFile();
            String path = FileNameUtils.replaceExtension(file.getAbsolutePath(), "gvl");
            save(new File(path));
        } else {
            logger.debug("Sólo implementado para ficheros");
        }
    }

    public void save(File file) throws LegendDriverException {
        String ext = FileNameUtils.getExtension(file.getName());

        IFMapLegendDriver driver = getDriver(ext);

        // workaround for driver version... we hope that when supportedVersions array grows (it has one element
        // for gvl and sld), gvsIG people will put the newer versions at the last position
        ArrayList<String> supportedVersions = driver.getSupportedVersions();
        String version = supportedVersions.get(supportedVersions.size() - 1);
        driver.write(layer.getMapContext().getLayers(), layer, layer.getLegend(), file, version);
    }

    public String get() throws LegendDriverException {
        return getAs("gvl");
    }

    public String getAs(String type) throws LegendDriverException {
        File legendFile = null;
        BufferedReader reader = null;
        try {
            legendFile = File.createTempFile("style", "." + type);
            save(legendFile);
            reader = new BufferedReader(new FileReader(legendFile.getAbsolutePath()));
            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line).append("\n");
                line = reader.readLine();
            }
            String xml = buffer.toString().trim();

            return xml;
        } catch (IOException e) {
            logger.error(e.getStackTrace(), e);
            throw new LegendDriverException(LegendDriverException.SYSTEM_ERROR);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error(e.getStackTrace(), e);
                }
            }
            if (legendFile != null) {
                legendFile.delete();
            }
        }

    }

    public ILegend cloneSimbology() throws LegendDriverException {
        try {
            return layer.getLegend().cloneLegend();
        } catch (XMLException e) {
            logger.error(e.getStackTrace(), e);
            throw new LegendDriverException(LegendDriverException.PARSE_LEGEND_FILE_ERROR);
        }
    }

    public void set(File file) throws LegendDriverException {
        if (!file.canRead()) {
            throw new LegendDriverException(LegendDriverException.SYSTEM_ERROR);
        }

        String ext = FileNameUtils.getExtension(file.getName());
        IFMapLegendDriver driver = getDriver(ext);

        Hashtable<FLayer, ILegend> table = driver.read(layer.getMapContext().getLayers(), layer, file);
        ILegend legend = table.get(layer);
        if (legend == null) {
            throw new LegendDriverException(0);
        }
        if (!(legend instanceof IVectorLegend)) {
            throw new LegendDriverException(0);
        }

        try {
            layer.setLegend((IVectorLegend) table.get(layer));
        } catch (LegendLayerException e) {
            logger.error(e.getStackTrace(), e);
            throw new LegendDriverException(0);
        }

    }

    private static IFMapLegendDriver getDriver(String id) throws LegendDriverException {
        if (!drivers.containsKey(id)) {
            throw new LegendDriverException(LegendDriverException.LEGEND_TYPE_NOT_YET_SUPPORTED);
        }
        try {
            return drivers.get(id).newInstance();
        } catch (InstantiationException e) {
            logger.error(e.getStackTrace(), e);
            throw new LegendDriverException(0);
        } catch (IllegalAccessException e) {
            logger.error(e.getStackTrace(), e);
            throw new LegendDriverException(0);
        }
    }

    public ILegend transformForSHP() throws LegendDriverException {
        ILegend simbology = cloneSimbology();

        if (simbology instanceof IClassifiedVectorLegend) {
            IClassifiedVectorLegend classifiedLegend = (IClassifiedVectorLegend) simbology;
            String[] classifyingFieldNames = classifiedLegend.getClassifyingFieldNames();
            String[] fieldNames = new String[classifyingFieldNames.length];
            for (int i = 0; i < classifyingFieldNames.length; i++) {
                int l = Math.min(classifyingFieldNames[i].length(), 10);
                fieldNames[i] = classifyingFieldNames[i].substring(0, l);
            }
            classifiedLegend.setClassifyingFieldNames(fieldNames);
            if (simbology instanceof VectorFilterExpressionLegend) {
                logger.warn("Seguramente no funcione");
            }
        }
        return simbology;
    }

    // De FMapGVLDriver
    public static void writeGVL(ILegend legend, File file) throws LegendDriverException {
        IFMapLegendDriver driver = getDriver("gvl");
        driver.write(null, null, legend, file, null);
    }

    public void set(ILegend simbology) throws LegendDriverException {
        if (!(simbology instanceof IVectorLegend)) {
            throw new LegendDriverException(0);
        }

        try {
            layer.setLegend((IVectorLegend) simbology);
        } catch (LegendLayerException e) {
            logger.error(e.getStackTrace(), e);
            throw new LegendDriverException(0);
        }

    }

}
