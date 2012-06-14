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
package es.udc.cartolab.gvsig.elle.gui.wizard.load;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.project.documents.view.gui.View;

public class LoadLegendWizard extends LoadMapWizard {

    public LoadLegendWizard(View view) {
	super(view);

    }

    public WindowInfo getWindowInfo() {
	WindowInfo wi = super.getWindowInfo();
	wi.setHeight(300);
	wi.setTitle(PluginServices.getText(this, "load_legends"));
	return wi;
    }

    protected void addWizardComponents() {
	views.add(new LoadLegendWizardComponent(properties));
    }

}
