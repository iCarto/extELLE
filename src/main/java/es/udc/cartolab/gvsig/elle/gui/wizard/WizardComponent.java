/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coruña
 * 
 * This file is part of ELLE
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
 */
package es.udc.cartolab.gvsig.elle.gui.wizard;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.JPanel;

public abstract class WizardComponent extends JPanel {

    private ArrayList<WizardListener> listeners = new ArrayList<WizardListener>();
    protected Map<String, Object> properties;

    public WizardComponent(Map<String, Object> properties) {
	this.properties = properties;
    }

    public abstract boolean canFinish();

    public abstract boolean canNext();

    public void addWizardListener(WizardListener l) {
	if (!listeners.contains(l)) {
	    listeners.add(l);
	}
    }

    public void removeWizardListener(WizardListener l) {
	if (listeners.contains(l)) {
	    listeners.remove(l);
	}
    }

    protected void callStateChanged() {
	for(WizardListener listener : listeners) {
	    listener.wizardChanged();
	}
    }

    public abstract String getWizardComponentName();

    public abstract void showComponent() throws WizardException;

    public abstract void finish() throws WizardException;

    public abstract void setProperties() throws WizardException;

    public void putProperty(String key, Object value) {
	properties.put(key, value);
    }

    public Object getProperty(String key) {
	return properties.get(key);
    }

}
