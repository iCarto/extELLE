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

import static es.icarto.gvsig.commons.i18n.I18n._;

import javax.swing.JOptionPane;

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.ui.mdiManager.MDIManagerFactory;
import org.gvsig.andami.ui.mdiManager.WindowInfo;
import org.gvsig.app.project.documents.view.gui.IView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

@SuppressWarnings("serial")
public class SaveMapWizard extends WizardWindow {

	private static final Logger logger = LoggerFactory
			.getLogger(SaveMapWizard.class);

    public final static String PROPERTY_VIEW = "view";

    private WindowInfo viewInfo;
    private final int width = 750;
    private final int height = 500;


    public SaveMapWizard(IView view) {
	super();
	properties.put(PROPERTY_VIEW, view);
    }

    public WindowInfo getWindowInfo() {
	if (viewInfo == null) {
	    viewInfo = new WindowInfo(WindowInfo.MODALDIALOG | WindowInfo.RESIZABLE);
	    viewInfo.setTitle(_("save_map"));
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
	    logger.error(e.getMessage(), e);
	} catch (RuntimeException e) {
		success = false;
		JOptionPane.showMessageDialog(this, _("error_saving"), "", JOptionPane.ERROR_MESSAGE);
		logger.error(e.getMessage(), e);
		MDIManagerFactory.getManager().restoreCursor();
	}

	if (success) {
	    JOptionPane.showMessageDialog(
		    this,
		    _("map_saved_correctly"),
		    "",
		    JOptionPane.INFORMATION_MESSAGE);
	}
	
	if (close) {
	    close();
	}
    }

}
