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
package es.udc.cartolab.gvsig.elle.gui.wizard.load;


import org.gvsig.andami.PluginServices;
import org.gvsig.andami.ui.mdiManager.WindowInfo;
import org.gvsig.app.project.documents.view.gui.IView;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

@SuppressWarnings("serial")
public class LoadMapWizard extends WizardWindow {

    private WindowInfo viewInfo;

    public LoadMapWizard(IView view) {
	super();
	properties.put(LoadMapWizardComponent.PROPERTY_VEW, view);
    }

    @Override
    public WindowInfo getWindowInfo() {
	if (viewInfo == null) {
	    viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG
		    | WindowInfo.PALETTE);
	    viewInfo.setTitle(PluginServices.getText(this, "Load_map"));
	    viewInfo.setWidth(600);
	    viewInfo.setHeight(800);
	}
	return viewInfo;
    }

    @Override
    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }

    @Override
    protected void addWizardComponents() {
	views.add(new LoadMapWizardComponent(properties));
	views.add(new LoadLegendWizardComponent(properties));
    }

}
