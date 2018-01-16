package es.udc.cartolab.gvsig.elle.utils;

import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;

import es.udc.cartolab.gvsig.elle.gui.wizard.save.LayerProperties;

public class TOCGroupsHandler {

    private final MapContext mapContext;

    public TOCGroupsHandler(MapContext mapContext) {
        this.mapContext = mapContext;
    }

    public FLayers getGroup(LayerProperties lp) {
        FLayers group;
        if (!lp.getGroup().equals("")) {
            List<String> groupNames = getGroupNames(lp.getGroup());
            FLayers currentGroup = mapContext.getLayers();

            for (String name : groupNames) {
                group = getGroup(currentGroup, name);
                if (group == null) {
                    group = new FLayers();
                    group.setName(name);
                    group.setMapContext(mapContext);
                    currentGroup.addLayer(group);
                }
                currentGroup = group;
            }
            group = currentGroup;
        } else {
            group = mapContext.getLayers();
        }
        return group;
    }

    private FLayers getGroup(FLayers layers, String group) {
        for (int i = 0; i < layers.getLayersCount(); i++) {
            FLayer l = layers.getLayer(i);
            if (l instanceof FLayers) {
                if (l.getName().equalsIgnoreCase(group)) {
                    return (FLayers) l;
                } else {
                    FLayers g = getGroup((FLayers) l, group);
                    if (g != null) {
                        return g;
                    }
                }
            }
        }
        return null;
    }

    /*
     * jlopez
     * 
     * This method is used in order to retrieve all nested groups
     * as FLayers with the string representation stored in DB.
     */
    private List<String> getGroupNames(String allGroups) {
        List<String> groupNames = new ArrayList<String>();
        char previousChar = '/';
        int startName = 0;
        for (int i = 0; i < allGroups.length(); i++) {
            if (allGroups.charAt(i) == '/') {
                // We check whether the slash is being escaped.
                if (previousChar != '\\') {
                    if ((i - startName) > 0) {
                        // We undo previously existing backslashes duplication and slashes escapes.
                        groupNames.add(allGroups.substring(startName, i).replace("\\/", "/").replace("\\\\", "\\"));
                    } else {
                        // Starting index == ending index --> empty string.
                        groupNames.add("");
                    }
                    startName = i + 1;
                }
            }
            if (allGroups.charAt(i) == '\\') {
                if (previousChar == '\\') {
                    // The backslash is duplicated, so it's not escaping a slash.
                    previousChar = '/';
                } else {
                    previousChar = allGroups.charAt(i);
                }
            } else {
                previousChar = allGroups.charAt(i);
            }

        }

        // We undo previously existing backslashes duplication and slashes escapes.
        groupNames.add(allGroups.substring(startName).replace("\\/", "/").replace("\\\\", "\\"));

        return groupNames;
    }

    public String getGroup(FLayer layer) {
        String group = "";
        if (layer.getParentLayer() != null) {
            group = getGroupCompositeName(layer.getParentLayer());
        }
        return group;
    }

    /*
     * Devuelve la jerarquía de capas incluída la raíz
     */
    public List<FLayer> getHierarchy(FLayer layer) {
        List<FLayer> hierarchy = new ArrayList<FLayer>();
        while (hasParentGroup(layer)) {
            layer = layer.getParentLayer();
            hierarchy.add(0, layer);
        }
        return hierarchy;
    }

    private boolean hasParentGroup(FLayer layer) {
        if ((layer.getName() == null) || (layer.getName().equals("root layer") && (layer.getParentLayer() == null))) {
            return false;
        }
        return true;
    }

    /*
     * This method is used in order to retrieve the name of all the nested
     * groups as a string, each of them separated by '/'. Therefore, we have to
     * escape that character ('\/'), which also means duplicating the
     * backslashes.
     */
    private String getGroupCompositeName(FLayers group) {

        if (!hasParentGroup(group)) {
            return "";
        }
        // We duplicate previously existing backslashes and escape the slashes.
        String groupName = group.getName().replace("\\", "\\\\").replace("/", "\\/");
        if (group.getParentLayer() != null) {
            String parentName = getGroupCompositeName(group.getParentLayer());
            if (parentName.length() > 0) {
                groupName = parentName + "/" + groupName;
            }
        }
        return groupName;
    }

}
