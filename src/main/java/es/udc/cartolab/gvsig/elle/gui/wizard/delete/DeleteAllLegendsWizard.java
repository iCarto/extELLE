package es.udc.cartolab.gvsig.elle.gui.wizard.delete;

import static es.icarto.gvsig.commons.i18n.I18n._;

import org.gvsig.andami.ui.mdiManager.WindowInfo;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardWindow;

public class DeleteAllLegendsWizard extends WizardWindow {

	private WindowInfo viewInfo;

	public DeleteAllLegendsWizard() {
		super();
	}

	public WindowInfo getWindowInfo() {
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
			viewInfo.setTitle(_("delete_legends"));
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
		views.add(new DeleteAllLegendsWizardComponent(properties));
	}

}
