package es.udc.cartolab.gvsig.elle.utils;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.testutils.FLyrVectStub;
import es.icarto.gvsig.commons.testutils.MapControlStub;
import es.icarto.gvsig.commons.testutils.ViewStub;

public class TOCGroupsHandlerTest {

    private MapContext mapContext = null;
    private Map<String, FLayer> map;

    @Before
    public void setUp() {
        ViewStub view = new ViewStub();
        MapControlStub mapControl = (MapControlStub) view.getMapControl();
        this.mapContext = mapControl.getMapContext();

        FLayers group = new FLayers();
        group.setName("test group");
        group.setMapContext(mapContext);
        FLyrVect vectLyrInGroup = new FLyrVectStub("inner vect layer");
        group.addLayer(vectLyrInGroup);
        mapControl.addLayer(group);

        FLayers sameNameGroup = new FLayers();
        sameNameGroup.setName("group and layer have same name");
        sameNameGroup.setMapContext(mapContext);
        FLyrVect sameNameLyr = new FLyrVectStub("group and layer have same name");
        sameNameGroup.addLayer(sameNameLyr);
        mapControl.addLayer(sameNameGroup);

        FLayers group3 = new FLayers();
        group3.setName("group3");
        group3.setMapContext(mapContext);
        FLyrVect group3_layer1 = new FLyrVectStub("group3_layer1");
        FLayers group3_group1 = new FLayers();
        group3_group1.setName("group3_group1");
        group3_group1.setMapContext(mapContext);
        FLyrVect group3_group1_layer1 = new FLyrVectStub("group3_group1_layer1");
        FLayers group3_group1_group1 = new FLayers();
        group3_group1_group1.setMapContext(mapContext);
        group3_group1_group1.setName("group3_group1_group1");
        FLyrVect group3_group1_group1_layer1 = new FLyrVectStub("group3_group1_group1_layer1");

        group3.addLayer(group3_layer1);
        group3_group1.addLayer(group3_group1_layer1);
        group3_group1_group1.addLayer(group3_group1_group1_layer1);
        group3_group1.addLayer(group3_group1_group1);
        group3.addLayer(group3_group1);
        mapControl.addLayer(group3);

        this.map = new HashMap<String, FLayer>();
        map.put("group3_group1_group1_layer1", group3_group1_group1_layer1);

    }

    @Test
    public void test() {
        FLayer layer = map.get("group3_group1_group1_layer1");
        String expected = "group3/group3_group1/group3_group1_group1";
        String actual = new TOCGroupsHandler(mapContext).getGroup(layer);
        assertEquals(expected, actual);
    }
}
