package es.icarto.gvsig.elle.style;

import java.io.File;

import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.utils.FileNameUtils;

//Como llamas a la combinación de estilo y etiqueta
// Foo foo = new Foo(layer);
// foo.style('gvl', '/tmp/foo')
// foo.label('gvlabel', '/tmp/foo')
//
// styleToFile
// labelToFile
// styleToString
// labelToString

public class LayerStyle {

    private final LayerSimbology simbology;
    private final LayerLabeling labelling;

    public LayerStyle(FLyrVect layer) {
        simbology = new LayerSimbology(layer);
        labelling = new LayerLabeling(layer);
    }

    public void save(File file) throws LegendDriverException {
        String fileName = file.getName();
        simbology.save(new File(FileNameUtils.replaceExtension(fileName, ".gvl")));
        labelling.save(new File(FileNameUtils.replaceExtension(fileName, "gvlabel")));
    }

}
