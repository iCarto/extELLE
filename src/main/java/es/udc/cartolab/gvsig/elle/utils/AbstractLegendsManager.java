/*
 * Copyright (c) 2010. Cartolab (Universidade da Coru�a)
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

import java.util.ArrayList;
import java.util.List;

import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.gui.wizard.save.LayerProperties;



public abstract class AbstractLegendsManager {

    protected ArrayList<LayerProperties> layers;
    protected ArrayList<FLyrVect> overviewLayers;
    private String legendGroupName;

    public AbstractLegendsManager(String legendGroupName) {
	layers = new ArrayList<LayerProperties>();
	overviewLayers = new ArrayList<FLyrVect>();
	this.legendGroupName = legendGroupName;
    }

    public void setLeyendGroupName(String legendGroupName) {
	this.legendGroupName = legendGroupName;
    }

    public String getLegendGroupName() {
	return this.legendGroupName;
    }

    public void addLayer(LayerProperties layer) {
	layers.add(layer);
    }

    public void addLayers(List<LayerProperties> layers) {
	layers.addAll(layers);
    }

    public void addOverviewLayer(FLyrVect layer) {
	overviewLayers.add(layer);
    }

    public void addOverviewLayers(FLayers layers) {
	for (int i=0; i<layers.getLayersCount(); i++) {
	    if (layers.getLayer(i) instanceof FLyrVect) {
		overviewLayers.add((FLyrVect) layers.getLayer(i));
	    }
	}
    }

    public abstract void loadLegends();

    public abstract void loadOverviewLegends();

    public abstract void saveOverviewLegends(String type) throws WizardException;

    public abstract void saveLegends() throws WizardException;

    public abstract boolean exists();

    public abstract boolean canRead();

    public abstract boolean canWrite();

    /**
     * Returns a question to ask to the user before saving. Null if none.
     */
    public abstract String getConfirmationMessage();

    /**
     * Prepares the infrastructure before saving.
     */
    public abstract void prepare() throws WizardException;

}
