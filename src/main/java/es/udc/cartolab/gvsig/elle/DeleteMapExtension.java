package es.udc.cartolab.gvsig.elle;

import java.sql.SQLException;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.delete.DeleteMapWindow;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DeleteMapExtension extends Extension {

    public void initialize() {
	registerIcons();
    }

    protected void registerIcons() {
		final String id = this.getClass().getName();
		IconThemeHelper.registerIcon("action", id, this);
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
