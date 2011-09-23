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
import com.iver.andami.preferences.IPreference;
import com.iver.andami.preferences.IPreferenceExtension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.About;
import com.iver.cit.gvsig.AddLayer;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.gui.panels.FPanelAbout;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.ProjectFactory;
import com.iver.cit.gvsig.project.documents.view.ProjectView;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.udc.cartolab.gvsig.elle.gui.EllePreferencesPage;
import es.udc.cartolab.gvsig.elle.gui.ElleWizard;
import es.udc.cartolab.gvsig.elle.gui.wizard.load.LoadMapWizard;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LoadMapExtension extends Extension implements IPreferenceExtension {

    public static EllePreferencesPage ellePreferencesPage = new EllePreferencesPage();

    public void execute(String actionCommand) {
	View view = createViewIfNeeded();
	LoadMapWizard wizard = new LoadMapWizard(view);
	wizard.open();
    }

    /**
     * If the active window is a View returns it, if not creates a new one, adds
     * it to the project and returns it
     */
    protected View createViewIfNeeded() {

	// TODO: fpuga: Check what happens when exists a view in the project but
	// is not active
	IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
	View view = null;

	if (iWindow instanceof View) {
	    view = (View) iWindow;
	} else {
	    ProjectExtension pExt = (ProjectExtension) PluginServices
		    .getExtension(ProjectExtension.class);
	    Project project = pExt.getProject();
	    ProjectView doc = ProjectFactory.createView(null);
	    doc.setName("ELLE View");
	    doc.setProject(project, 0);
	    project.addDocument(doc);
	    // Opening view
	    view = new View();
	    view.initialize();
	    view.setModel(doc);
	    view.getWindowInfo().setMaximizable(true);
	    view.getWindowInfo().setMaximized(true);
	    PluginServices.getMDIManager().addWindow(view);
	}
	return view;
    }

    public void initialize() {
	About about = (About) PluginServices.getExtension(About.class);
	FPanelAbout panelAbout = about.getAboutPanel();
	java.net.URL aboutURL = this.getClass().getResource("/about.htm");
	panelAbout.addAboutUrl("ELLE", aboutURL);

	// carga la pestaña en añadir capa
	AddLayer.addWizard(ElleWizard.class);

	// icons
	registerIcons();
    }

    protected void registerIcons() {

	PluginServices.getIconTheme().registerDefault(
		"load-map",
		this.getClass().getClassLoader().getResource(
			"images/mapacargar.png"));
    }

    public boolean isEnabled() {
	return true;
    }

    public boolean isVisible() {
	DBSession dbs = DBSession.getCurrentSession();
	return dbs != null;
    }

    public IPreference[] getPreferencesPages() {
	IPreference[] preferences = new IPreference[1];
	preferences[0] = ellePreferencesPage;
	return preferences;
    }

}