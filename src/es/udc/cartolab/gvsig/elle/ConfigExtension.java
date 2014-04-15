package es.udc.cartolab.gvsig.elle;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.preferences.IPreference;
import com.iver.andami.preferences.IPreferenceExtension;
import com.iver.cit.gvsig.About;
import com.iver.cit.gvsig.AddLayer;
import com.iver.cit.gvsig.gui.panels.FPanelAbout;

import es.udc.cartolab.gvsig.elle.gui.EllePreferencesPage;
import es.udc.cartolab.gvsig.elle.gui.ElleWizard;
import es.udc.cartolab.gvsig.elle.utils.MapFilter;
import es.udc.cartolab.gvsig.elle.utils.NoFilter;

public class ConfigExtension extends Extension implements IPreferenceExtension {

    public static EllePreferencesPage ellePreferencesPage = new EllePreferencesPage();
    
    private String wizardTitle = "ELLE";
    private MapFilter mapFilter = new NoFilter();

    @Override
    public void initialize() {
	About about = (About) PluginServices.getExtension(About.class);
	FPanelAbout panelAbout = about.getAboutPanel();
	java.net.URL aboutURL = this.getClass().getResource("/about.htm");
	panelAbout.addAboutUrl("ELLE", aboutURL);

	// carga la pestaña en añadir capa
	AddLayer.addWizard(ElleWizard.class);
    }

    @Override
    public void execute(String actionCommand) {
	throw new AssertionError("This extension should not be 'executed'");
    }

    @Override
    public boolean isEnabled() {
	return false;
    }

    @Override
    public boolean isVisible() {
	return false;
    }
    
    public IPreference[] getPreferencesPages() {
	IPreference[] preferences = new IPreference[1];
	preferences[0] = ellePreferencesPage;
	return preferences;
    }

    public String getWizardTitle() {
	return this.wizardTitle;
    }
    
    public void setWizardTitle (String wizardTitle) {
	this.wizardTitle = wizardTitle;
    }
    
    public MapFilter getMapFilter() {
	return this.mapFilter;
    }
    
    public void setMapFilter(MapFilter mapFilter) {
	this.mapFilter = mapFilter;
    }
    

}
