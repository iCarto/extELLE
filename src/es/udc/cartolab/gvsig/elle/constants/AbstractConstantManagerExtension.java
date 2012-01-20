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

    private HashMap<String, IConstant> constants = new HashMap<String, IConstant>();

    // Each time setConstant or clearConstants is used previousConstant is
    // updated
    private HashMap<String, IConstant> previousConstants = null;

    private ConstantStatusBarControl statusBar = null;


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

    /**
     * Be aware to call super if this method is overided, or reimplement it
     * functionality.
     */
    public void initialize() {
	statusBar = new ConstantStatusBarControl();
	statusBar.register((Class<IExtension>) this.getClass());
	registerIcons();
	setConstants(initConstants());

    }

    /**
     * Instantiated here the constants that are going to be used. This method
     * will be called by the initialize method of
     * AbstractConstantManagerExtension. You should avoid change the parameters
     * of the constants after instantiated it here
     */
    protected abstract HashMap<String, IConstant> initConstants();


    private void setConstants(HashMap<String, IConstant> constants) {
	if (constants != null) {
	    previousConstants = (HashMap<String, IConstant>) constants.clone();
	}

	// TODO: Hacer chequeos
	this.constants = constants;
	TreeMap<Integer, IConstant> constantsInStatusBar = new TreeMap<Integer, IConstant>();
	for (IConstant c : constants.values()) {
	    if (c.getNameInStatusBar() != null) {
		constantsInStatusBar.put(c.getOrder(), c);
	    }
	}
	statusBar.setNullCharacter("-");
	statusBar.setConstats(constantsInStatusBar);
    }



    public IConstant getConstant(String constantName) {
	return constants.get(constantName);
    }
    
    /**
     * Wrap method to set the value of a constant. It's recommended to use this
     * method instead of call directly the setValue of IConstant to avoid so
     * boring operations like update the status bar
     */
    public void setValue(String constantName, Object value) {
	constants.get(constantName).setValue(value);
	statusBar.printConstantsInfo();
    }



    public HashMap<String, IConstant> getPreviousConstants() {
	return previousConstants;
    }


    public void clearConstants() {
	previousConstants = (HashMap<String, IConstant>) constants.clone();

	for (IConstant c : constants.values()) {
	    c.setValue(null);
	}

	statusBar.printConstantsInfo();
    }

    public boolean areConstantsSetFor(String constantName) {
	// TODO: We should put here a hook that allows the implementing
	// extension check the business logic
	// Maybe use a TreeMap allow us a trivial test: The values for the
	// higher hierarchies is set

	return constants.get(constantName).getValue() != null;
    }

    /*****************************************************************************
     * METHODS TO REPRESENT THE INFORMATION IN THE STATUS BAR fpuga. I not
     * really like this approach. Probably it should be in another but i can't
     * figure now an easy to implement / easy to use by third party extension
     * way to do this
     * ****************************************************************************/


    public void setNullCharacter(String nullCharacter) {
	statusBar.setNullCharacter(nullCharacter);
    }

}
