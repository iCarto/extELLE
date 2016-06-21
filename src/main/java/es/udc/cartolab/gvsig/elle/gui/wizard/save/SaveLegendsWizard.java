/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coru�a
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
package es.udc.cartolab.gvsig.elle.gui.wizard.save;

import java.util.ArrayList;
import java.util.List;

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.ui.mdiManager.WindowInfo;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

public class SaveLegendsWizard extends WizardWindow {

    public final static String PROPERTY_VIEW = "view";

    public final static int ACTIVES = 0;
    public final static int VISIBLES = 1;
    public final static int ALL = 2;

    private WindowInfo viewInfo;
    private int layersOption;
    private final int width = 750;
    private final int height = 500;

    public SaveLegendsWizard(IView view, int layersOption) {
	properties.put(SaveMapWizard.PROPERTY_VIEW, view);

	this.layersOption = layersOption;
	setLayersProperties();

    }

    protected void addWizardComponents() {
	views.add(new SaveLegendsWizardComponent(properties));
    }

    private void setLayersProperties() {
	Object aux = properties.get(SaveMapWizard.PROPERTY_VIEW);
	if (aux != null && aux instanceof IView) {
	    FLayers layers = ((IView) aux).getMapControl().getMapContext().getLayers();
	    List<LayerProperties> list = getList(layers);
	    properties.put(SaveMapWizardComponent.PROPERTY_LAYERS_MAP, list);
	}
    }

    private List<LayerProperties> getList(FLayers layers) {
	List<LayerProperties> list = new ArrayList<LayerProperties>();

	for (int i=layers.getLayersCount()-1; i>=0; i--) {
	    FLayer layer = layers.getLayer(i);
	    if (layer instanceof FLayers) {
		list.addAll(getList((FLayers) layer));
	    } else {
		if (layer instanceof FLyrVect) {
		    LayerProperties lp;
		    try {
			lp = new LayerProperties((FLyrVect) layer);

			lp.setVisible(layer.isVisible());

			switch (layersOption) {
			case ACTIVES :
			    lp.setSave(layer.isActive());
			    break;
			case VISIBLES :
			    lp.setSave(layer.isVisible());
			    break;
			case ALL:
			default:
			    lp.setSave(true);
			}

			lp.setGroup(layer.getParentLayer().getName());
			lp.setMaxScale(layer.getMaxScale());
			lp.setMinScale(layer.getMinScale());
			lp.setPosition(layers.getLayersCount()-i);

			list.add(lp);
		    } catch (WizardException e) {
			// layer is not postgis, nothing to do
		    }
		}
	    }
	}

	return list;

    }


    public WindowInfo getWindowInfo() {
	if (viewInfo == null) {
	    viewInfo = new WindowInfo(WindowInfo.MODALDIALOG | WindowInfo.RESIZABLE);
	    viewInfo.setTitle(PluginServices.getText(this, "Save_legends"));
	    viewInfo.setWidth(width);
	    viewInfo.setHeight(height);
	}
	return viewInfo;
    }


    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }

}
