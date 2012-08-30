package es.udc.cartolab.gvsig.elle;

import java.sql.SQLException;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.delete.DeleteAllLegendsWizard;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DeleteAllLegendsExtension extends Extension {

    public void initialize() {
	registerIcons();
    }

    private void registerIcons() {
	PluginServices.getIconTheme().registerDefault(
		"delete-all-legends",
		this.getClass().getClassLoader()
		.getResource("images/leyborrar.png"));
    }

    public void execute(String actionCommand) {
	DeleteAllLegendsWizard wizard = new DeleteAllLegendsWizard();
	wizard.open();
    }

    public boolean isEnabled() {
	if (DBSession.isActive() && canUseELLE()) {
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

    public boolean isVisible() {
	return true;
    }

}
