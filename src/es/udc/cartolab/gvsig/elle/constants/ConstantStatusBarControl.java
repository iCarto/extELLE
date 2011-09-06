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

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import org.gvsig.gui.beans.controls.IControl;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.IExtension;

/**
 * 
 * Implementation extension should define the following properties:
 * no_constants_selected,
 * 
 * @author Francisco Puga <fpuga@cartolab.es>
 * 
 */
public class ConstantStatusBarControl extends JLabel implements IControl {

    private String name = "ELLE_CONSTANTS";

    /**
     * String to use for represent the not set constants
     */
    private String nullCharacter = "-";

    public ConstantStatusBarControl() {
	super();
	setText(PluginServices.getText(this, "no_constants_selected"));
	setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    /**
     * If a bar with this name not exists jet in the StatusBar, this bar is
     * added
     * 
     * @param extension
     *            The extension that will control the visibility and enability
     *            of this bar
     * @return true if a bar whit this name not exists jet in the status bar
     */
    public boolean addThisStatusBarControl(Class<IExtension> extension) {
	boolean controlNotExistsJet = false;

	if (PluginServices.getMainFrame().getStatusBar().getControl(name) != null) {
	    PluginServices.getMainFrame().addStatusBarControl(extension, this);
	    controlNotExistsJet = true;
	}

	return controlNotExistsJet;
    }


    public void addActionListener(ActionListener listener) {
    }


    public void removeActionListener(ActionListener listener) {
    }


    public Object setValue(Object value) {
	setText(value.toString());
	return value;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public void setName(String name) {
	this.name = name;
    }

    public void setNullCharacter(String nullCharacter) {
	this.nullCharacter = nullCharacter;
    }

    private String nullToString(String s) {
	if ((s == null) || (s.trim().isEmpty())) {
	    return nullCharacter;
	} else {
	    return s;
	}
    }
}