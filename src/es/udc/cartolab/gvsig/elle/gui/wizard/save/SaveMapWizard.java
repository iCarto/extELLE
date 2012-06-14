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
package es.udc.cartolab.gvsig.elle.gui.wizard.save;

import javax.swing.JOptionPane;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

public class SaveMapWizard extends WizardWindow {

    public final static String PROPERTY_VIEW = "view";

    private WindowInfo viewInfo;
    private final int width = 750;
    private final int height = 500;


    public SaveMapWizard(View view) {
	super();

	properties.put(PROPERTY_VIEW, view);

    }


    public WindowInfo getWindowInfo() {
	if (viewInfo == null) {
	    viewInfo = new WindowInfo(WindowInfo.MODALDIALOG | WindowInfo.RESIZABLE);
	    viewInfo.setTitle(PluginServices.getText(this, "save_map"));
	    viewInfo.setWidth(width);
	    viewInfo.setHeight(height);
	}
	return viewInfo;
    }


    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }


    protected void addWizardComponents() {
	views.add(new SaveMapWizardComponent(properties));
	views.add(new SaveLegendsWizardComponent(properties));
    }

    protected void finish() {
	boolean close = true;
	boolean success = true;
	try {
	    for (WizardComponent wc : views) {
		wc.finish();
	    }
	} catch (WizardException e) {
	    close = e.closeWizard();
	    success = false;
	    if (e.showMessage()) {
		JOptionPane.showMessageDialog(
			this,
			e.getMessage(),
			"",
			JOptionPane.ERROR_MESSAGE);
	    }
	    e.printStackTrace();
	}
	if (close) {
	    close();
	}
	if (success) {
	    JOptionPane.showMessageDialog(
		    this,
		    PluginServices.getText(this, "map_saved_correctly"),
		    "",
		    JOptionPane.INFORMATION_MESSAGE);
	}
    }

}
