/*
 * This file is part of ELLE
 * 
 * Copyright (c) 2011. Empresa Pública de Obras e Servizos Hidráulicos
 * 
 * ELLE was originally developed by Cartolab, Cartography Laboratory from
 * A Coruña University (Spain) directed by Fco. Alberto Varela Garcia
 * http://www.cartolab.es
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
 * 
 */

package es.udc.cartolab.gvsig.elle.constants;

import java.net.URL;
import java.util.HashMap;
import java.util.TreeMap;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.plugins.IExtension;

/**
 * 
 * @author Francisco Puga <fpuga@cartolab.es>
 * 
 */
public abstract class AbstractConstantManagerExtension extends Extension {

    // TODO: Use a Object HashMap allows handled complex "constants" like the
    // "municipios" field on Constants.java on extEIELUtils, but this make that
    // we have to cast the results of getConstants all the time
    // Maybe use another approach like define a IConstants interface for the
    // objects that handles the values itself will be useful
    private HashMap<String, Object> constants = new HashMap<String, Object>();

    // Each time setConstant or clearConstants is used previousConstant is
    // updated
    private HashMap<String, Object> previousConstants = null;

    private ConstantStatusBarControl statusBar = null;

    private TreeMap<String, String> constantsNamesInStatusBar;


    /**
     * The identifiers of the icons that appears in the config.xml related to
     * elle-constants extensions. The images should be present in the images
     * folder of the plugin and have .png as extensions. This field can be
     * override to use your own names
     * 
     * If the image exists in your own plugin the image provided by your plugin
     * will be used, if not exists the image used will the image provided in
     * elle
     */
    protected String[] iconsIdentifiers = {"zoom-to-constants", "select-constants"};

    private void registerIcons() {
	URL url = null;

	for (String iconIdentifier:iconsIdentifiers) {
	    String iconResourcePath = "images/" + iconIdentifier + ".png";
	    url = this.getClass().getClassLoader().getResource(iconResourcePath);
	    if (url == null) {
		url = PluginServices
		.getPluginServices("es.udc.cartolab.gvsig.elle")
		.getClassLoader().getResource(iconResourcePath);
	    }
	    PluginServices.getIconTheme().registerDefault(iconIdentifier,url);
	}
    }

    public void initialize() {
	statusBar = new ConstantStatusBarControl();
	statusBar.register((Class<IExtension>) this.getClass());
	registerIcons();
    }

    public void setConstant(String constantName, Object constantValue) {
	previousConstants = (HashMap<String, Object>) constants.clone();

	// TODO: It should be better that the extension that implements this
	// defined the available keys and here only set the value if the key
	// already exists
	constants.put(constantName, constantValue);

	statusBar.setValue(getConstantsInfo());
    }

    public void setConstants(HashMap<String, Object> constants) {
	previousConstants = (HashMap<String, Object>) constants.clone();
	constants = this.constants;

	statusBar.setValue(getConstantsInfo());
    }

    /**
     * For the constants that we want to be showed in some form in the status
     * bar we should provide a String that will represent it's name in the bar.
     * The value will be automatically get. Here we must put only the constants
     * that we want that appear in the bar, there is not need to put all of it
     */
    public void setConstantsNamesInStatusBar(
	    TreeMap<String, String> constantsNamesInStatusBar) {
	this.constantsNamesInStatusBar = constantsNamesInStatusBar;
    }

    public Object getConstant(String constantName) {
	return constants.get(constantName);
    }

    public HashMap<String, Object> getConstants() {
	return constants;
    }

    public HashMap<String, Object> getPreviousConstants() {
	return previousConstants;
    }

    public TreeMap<String, String> getConstantsNamesInStatusBar() {
	return constantsNamesInStatusBar;
    }

    public void clearConstants() {
	previousConstants = (HashMap<String, Object>) constants.clone();

	// TODO: Maybe we should not clear, we should maintains the keys and put
	// to null all the values of the entries
	constants.clear();

	statusBar.setValue(getConstantsInfo());
    }

    public boolean areConstantsSetFor(String constantName) {
	// TODO: We should put here a hook that allows the implementing
	// extension check the business logic
	// Maybe use a TreeMap allow us a trivial test: The values for the
	// higher hierarchies is set

	return constants.get(constantName) != null;
    }

    /*****************************************************************************
     * METHODS TO REPRESENT THE INFORMATION IN THE STATUS BAR fpuga. I not
     * really like this approach. Probably it should be in another but i can't
     * figure now an easy to implement / easy to use by third party extension
     * way to do this
     * ****************************************************************************/

    /**
     * String to use for represent the not set constants
     */
    private String nullCharacter = "-";

    public void setNullCharacter(String nullCharacter) {
	this.nullCharacter = nullCharacter;
    }

    /**
     * A representative text of the actual state of the constants. The value
     * returned for this method is the message that will be used in the status
     * bar
     * 
     * Child can override this method to get a more acquired representation
     */
    public String getConstantsInfo() {
	StringBuilder s = new StringBuilder();
	for (String k : constantsNamesInStatusBar.keySet()) {
	    s.append(constantsNamesInStatusBar.get(k));
	    s.append(": ");
	    s.append(nullToString(constants.get(k)));
	    s.append(" ");
	}

	return s.toString();
    }

    private String nullToString(Object s) {
	if ((s == null) || (s.toString().trim().isEmpty())) {
	    return nullCharacter;
	} else {
	    return s.toString();
	}
    }

}
