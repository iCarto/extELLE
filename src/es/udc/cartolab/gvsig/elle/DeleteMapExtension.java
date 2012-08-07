package es.udc.cartolab.gvsig.elle;

import java.sql.SQLException;

import com.iver.andami.plugins.Extension;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.delete.DeleteMapWizard;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DeleteMapExtension extends Extension {

    public void initialize() {
    }

    public void execute(String actionCommand) {
	DeleteMapWizard wizard = new DeleteMapWizard();
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
