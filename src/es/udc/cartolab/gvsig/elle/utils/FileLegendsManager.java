/*
 * Copyright (c) 2010. Cartolab (Universidade da Coruña)
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

import java.io.File;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.utiles.XMLEntity;

import es.udc.cartolab.gvsig.elle.gui.EllePreferencesPage;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.gui.wizard.save.LayerProperties;

public class FileLegendsManager extends AbstractLegendsManager {

    private File dir;

    public FileLegendsManager(String legendGroupName) {
	super(legendGroupName);

	setDir(legendGroupName);
    }

    public void setLegendGroupname(String leyendGroupName) {
	super.setLeyendGroupName(leyendGroupName);
    }

    public void loadLegends() {


    }

    public void saveLegends() throws WizardException {
	if (dir != null) {
	    if (!dir.exists()) {
		if (!dir.mkdir()) {
		    String message = PluginServices.getText(this, "legend_write_dir_error");
		    throw new WizardException(String.format(message, dir.getAbsolutePath()));
		}
	    }
	    String path = dir.getAbsolutePath();
	    if (!path.endsWith(File.separator)) {
		path = path + File.separator;
	    }
	    for (LayerProperties lp : layers) {
		File legendFile = new File(path + lp.getLayername() + "." + lp.getLegendType());
		try {
		    LoadLegend.saveLegend(lp.getLayer(), legendFile);
		} catch (LegendDriverException e) {
		    throw new WizardException(e);
		}
	    }
	} else {
	    throw new WizardException(PluginServices.getText(this, "no_config_error"));
	}
    }

    public boolean exists() {
	return (dir!=null) && dir.exists();
    }

    public boolean canRead() {
	return (dir!=null) && dir.isDirectory() && dir.canRead();
    }

    public boolean canWrite() {
	return (dir!=null) && dir.canWrite() && dir.isDirectory();
    }

    private void setDir(String dir) {
	this.dir = null;
	PluginServices ps = PluginServices.getPluginServices("es.udc.cartolab.gvsig.elle");
	XMLEntity xml = ps.getPersistentXML();
	if (xml.contains(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME)) {
	    String path = xml.getStringProperty(EllePreferencesPage.DEFAULT_LEGEND_DIR_KEY_NAME);
	    if (path.endsWith(File.separator)) {
		path = path + dir + File.separator;
	    } else {
		path = path + File.separator + dir + File.separator;
	    }
	    this.dir = new File(path);
	}
    }

    public void loadOverviewLegends() {

    }

    public void saveOverviewLegends(String type) throws WizardException {

	String path = dir.getAbsolutePath();
	if (path.endsWith(File.separator)) {
	    path = path + "overview" + File.separator;
	} else {
	    path = path + File.separator + "overview" + File.separator;
	}
	File overviewDir = new File(path);
	if (!overviewDir.exists()) {
	    if (!overviewDir.mkdir()) {
		String message = PluginServices.getText(this, "legend_write_dir_error");
		throw new WizardException(String.format(message, path));
	    }
	}
	if (!overviewDir.isDirectory()) {
	    String msg = PluginServices.getText(this, "legend_overview_error");
	    throw new WizardException(String.format(msg, path));
	}
	for (FLyrVect layer : overviewLayers) {
	    File legendFile = new File(path + layer.getName() + "." + type);
	    try {
		LoadLegend.saveLegend(layer, legendFile);
	    } catch (LegendDriverException e) {
		throw new WizardException(e);
	    }
	}

    }

    public String getConfirmationMessage() {
	//nothing to do
	return null;
    }

    public void prepare() {
	//nothing to do
    }


}
