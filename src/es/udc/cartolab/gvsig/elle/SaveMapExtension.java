/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coruña
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
package es.udc.cartolab.gvsig.elle;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.udc.cartolab.gvsig.elle.gui.wizard.save.SaveMapWizard;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class SaveMapExtension extends Extension {


    public void execute(String actionCommand) {
	View view = (View) PluginServices.getMDIManager().getActiveWindow();
	SaveMapWizard wizard = new SaveMapWizard(view);
	wizard.open();
    }


    public void initialize() {
	registerIcons();
    }

    protected void registerIcons() {
	PluginServices.getIconTheme().registerDefault(
		"save-map",
		this.getClass().getClassLoader().getResource("images/mapaguardar.png")
		);
    }


    public boolean isEnabled() {
	if (PluginServices.getMDIManager().getActiveWindow() instanceof View) {
	    FLayers layers = ((View) PluginServices.getMDIManager().getActiveWindow()).getMapControl().getMapContext().getLayers();
	    return layers.getLayersCount() > 0;
	}
	return false;
    }


    public boolean isVisible() {
	if (PluginServices.getMDIManager().getActiveWindow() instanceof View) {
	    DBSession session = DBSession.getCurrentSession();
	    if (session != null) {
		return session.getDBUser().isAdmin();
	    }
	}
	return false;
    }

}
