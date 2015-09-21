package es.udc.cartolab.gvsig.elle;

import java.sql.SQLException;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.delete.DeleteMapWindow;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DeleteMapExtension extends Extension {

    public void initialize() {
	registerIcons();
    }

    private void registerIcons() {
	PluginServices.getIconTheme().registerDefault(
		"delete-map",
		this.getClass().getClassLoader()
		.getResource("images/mapaborrar.png"));
    }

    public void execute(String actionCommand) {
	DeleteMapWindow window = new DeleteMapWindow();
	window.openDialog();
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
