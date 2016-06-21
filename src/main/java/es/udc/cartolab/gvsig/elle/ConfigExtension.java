package es.udc.cartolab.gvsig.elle;

import org.gvsig.about.AboutManager;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.preferences.IPreference;
import org.gvsig.andami.preferences.IPreferenceExtension;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.extension.AddLayer;

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
    	ApplicationManager application = ApplicationLocator.getManager();
    	AboutManager about = application.getAbout();
    	about.addDeveloper("ELLE", getClass().getClassLoader().getResource("/about.htm"), 1);
   

	// carga la pesta�a en a�adir capa
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
