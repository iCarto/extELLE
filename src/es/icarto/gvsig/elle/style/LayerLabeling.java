package es.icarto.gvsig.elle.style;

import java.io.File;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.gvsig.symbology.fmap.labeling.GeneralLabelingStrategy;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.VectorialFileAdapter;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.AttrInTableLabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingMethod;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelClass;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelingFactory;
import com.iver.utiles.XMLEntity;

import es.icarto.gvsig.commons.utils.FileNameUtils;
import es.icarto.gvsig.commons.utils.FileUtils;

public class LayerLabeling {

    private static final Logger logger = Logger.getLogger(LayerLabeling.class);

    public static final String GVLABEL = "gvlabel";
    private final FLyrVect layer;

    public LayerLabeling(FLyrVect layer) {
        this.layer = layer;
    }

    public String get() {
        return getAs(GVLABEL);
    }

    public String getAs(String type) {
        String label = null;
        if (layer.isLabeled()) {
            final ILabelingStrategy labelingStrategy = layer.getLabelingStrategy();
            if (labelingStrategy != null) {
                label = labelingStrategy.getXMLEntity().toString().trim();
            }
        }
        return label;
    }

    public void save() throws LegendDriverException {
        if (layer.getSource() instanceof VectorialFileAdapter) {
            File file = ((VectorialFileAdapter) layer.getSource()).getFile();
            String path = FileNameUtils.replaceExtension(file.getAbsolutePath(), "gvlabel");
            save(new File(path));
        } else {
            logger.debug("Sólo implementado para ficheros");
        }
    }

    public void save(File file) throws LegendDriverException {
        String ext = FileNameUtils.getExtension(file.getName());
        if (!ext.equals(GVLABEL)) {
            throw new LegendDriverException(LegendDriverException.SAVE_LEGEND_ERROR);

        }
        String label = get();

        if (label != null) {
            FileUtils.write(label, file);
        }
    }

    public ILabelingStrategy cloneLabeling() {
        XMLEntity label = null;
        if (layer.isLabeled()) {
            final ILabelingStrategy labelingStrategy = layer.getLabelingStrategy();
            if (labelingStrategy != null) {
                label = labelingStrategy.getXMLEntity();
                ILabelingStrategy strategy = null;
                try {
                    strategy = LabelingFactory.createStrategyFromXML(label, layer);
                } catch (ReadDriverException e) {
                    logger.error(e.getStackTrace(), e);
                }
                return strategy;
            }
        }
        return null;
    }

    public void set(String label) {
        if ((label != null) && (label.length() > 0)) {
            try {
                XMLEntity parse = XMLEntity.parse(label);
                ILabelingStrategy strategy = LabelingFactory.createStrategyFromXML(parse, layer);
                layer.setLabelingStrategy(strategy);
                layer.setIsLabeled(true);

            } catch (MarshalException e) {
                logger.error(e.getStackTrace(), e);
            } catch (ValidationException e) {
                logger.error(e.getStackTrace(), e);
            } catch (ReadDriverException e) {
                logger.error(e.getStackTrace(), e);
            }
        }
    }

    private String t(String s) {
        if (s == null) {
            return null;
        }
        int l = Math.min(s.length(), 10);
        return s.substring(0, l);
    }

    public ILabelingStrategy transformForSHP() throws LegendDriverException {

        ILabelingStrategy cloneLabel = cloneLabeling();
        if (cloneLabel == null) {
            return null;
        }

        if (cloneLabel instanceof AttrInTableLabelingStrategy) {
            AttrInTableLabelingStrategy tableLabel = (AttrInTableLabelingStrategy) cloneLabel;

            try {
                tableLabel.setColorField(t(tableLabel.getColorField()));
                tableLabel.setHeightField(t(tableLabel.getHeightField()));
                tableLabel.setRotationField(t(tableLabel.getRotationField()));
                tableLabel.setTextField(t(tableLabel.getTextField()));
            } catch (ReadDriverException e) {
                logger.error(e.getStackTrace(), e);
                throw new LegendDriverException(0);
            }

            handleLabelingMethod(tableLabel);

        } else if (cloneLabel instanceof GeneralLabelingStrategy) {
            handleLabelingMethod(cloneLabel);
        } else {
            throw new RuntimeException("Not implemented");
        }

        return cloneLabel;

    }

    private void handleLabelingMethod(ILabelingStrategy labelStrategy) {
        FieldLabelRE re = new FieldLabelRE();

        if (labelStrategy.getLabelingMethod() != null) {
            ILabelingMethod labelingMethod = labelStrategy.getLabelingMethod();

            for (LabelClass lc : labelingMethod.getLabelClasses()) {
                String[] labelExpressions = lc.getLabelExpressions();
                if ((labelExpressions != null) && (labelExpressions.length > 0)) {
                    for (int i = 0; i < labelExpressions.length; i++) {
                        labelExpressions[i] = re.limitTo10(labelExpressions[i]);
                    }
                }
                if (lc.isUseSqlQuery()) {
                    // lc.getSQLQuery();
                    throw new RuntimeException("Not implemented jet");
                }

                // getLabelStyle(), getTexts() and getTextSymbol() do not need transformation
            }
        }
    }

    public static void writeGVL(ILabelingStrategy labelling, File file) {
        String label = labelling.getXMLEntity().toString().trim();
        FileUtils.write(label, file);
    }

    public void set(ILabelingStrategy labelling) throws LegendDriverException {
        if (labelling == null) {
            return;
        }
        try {
            labelling.setLayer(layer);
        } catch (ReadDriverException e) {
            logger.error(e.getStackTrace(), e);
            throw new LegendDriverException(0);
        }
        layer.setLabelingStrategy(labelling);
        layer.setIsLabeled(true);
    }

}
