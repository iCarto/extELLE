package es.udc.cartolab.gvsig.elle.gui.wizard.delete;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

public class DeleteMapWizard extends WizardWindow {

    private WindowInfo viewInfo;

    public DeleteMapWizard() {
	super();
    }

    public WindowInfo getWindowInfo() {
	if (viewInfo == null) {
	    viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG
		    | WindowInfo.PALETTE);
	    viewInfo.setTitle(PluginServices.getText(this, "delete_map"));
	    viewInfo.setWidth(240);
	    viewInfo.setHeight(460);
	}
	return viewInfo;
    }

    public Object getWindowProfile() {
	return WindowInfo.DIALOG_PROFILE;
    }

    @Override
    protected void addWizardComponents() {
	views.add(new DeleteMapWizardComponent(properties));
    }

}
