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
package es.udc.cartolab.gvsig.elle;

import java.sql.SQLException;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.MDIManagerFactory;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.mapcontext.layers.FLayers;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;
import es.udc.cartolab.gvsig.elle.gui.wizard.load.LoadLegendWizard;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class LoadAllLegendsExtension extends Extension {

	public void execute(String actionCommand) {
		IView view = (IView) MDIManagerFactory.getManager().getActiveWindow();
		WizardWindow wizard = new LoadLegendWizard(view);
		wizard.open();
	}

	public void initialize() {
		registerIcons();
	}

	protected void registerIcons() {
		final String id = this.getClass().getName();
		IconThemeHelper.registerIcon("action", id, this);
	}

	public boolean isEnabled() {
		if (DBSession.isActive() && canUseELLE() && activeWindowIsIWindow() && areLayersInTOC()) {
			return true;
		}
		return false;
	}

	private boolean canUseELLE() {
		DBSession dbs = DBSession.getCurrentSession();
		try {
			return dbs.getDBUser().canUseSchema(DBStructure.SCHEMA_NAME);
		} catch (SQLException e) {
			return false;
		}
	}

	private boolean activeWindowIsIWindow() {
		return MDIManagerFactory.getManager().getActiveWindow() instanceof IView;
	}

	private boolean areLayersInTOC() {
		IView view = ((IView) MDIManagerFactory.getManager().getActiveWindow());
		FLayers layers = view.getMapControl().getMapContext().getLayers();
		return layers.getLayersCount() > 0;
	}

	public boolean isVisible() {
		return true;
	}

}
