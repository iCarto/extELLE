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
package es.udc.cartolab.gvsig.elle.gui.wizard.save;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import com.iver.andami.PluginServices;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;


public class LayerListPopupMenu extends JPopupMenu implements ActionListener {

    private JMenuItem invertSelection, selectAll, selectVisibles, selectActives;
    private JTable table;
    private List<LayerProperties> lp;
    private WizardComponent wc;

    public LayerListPopupMenu(WizardComponent wc, JTable table, List<LayerProperties> lp) {
	super();
	this.table = table;
	this.lp = lp;
	this.wc = wc;

	invertSelection = new JMenuItem(PluginServices.getText(this, "invert_selection"));
	invertSelection.addActionListener(this);
	selectAll = new JMenuItem(PluginServices.getText(this, "select_all_layers"));
	selectAll.addActionListener(this);
	selectVisibles = new JMenuItem(PluginServices.getText(this, "select_visible_layers"));
	selectVisibles.addActionListener(this);
	selectActives = new JMenuItem(PluginServices.getText(this, "select_active_layers"));
	selectActives.addActionListener(this);

	add(selectAll);
	add(selectActives);
	add(selectVisibles);
	add(invertSelection);
    }

    public void actionPerformed(ActionEvent e) {

	if (e.getSource() == invertSelection) {
	    for (int i=0; i<table.getRowCount(); i++) {
		if (table.getModel().isCellEditable(i, 0)) {
		    Boolean selected = (Boolean) table.getModel().getValueAt(i, 0);
		    table.getModel().setValueAt(new Boolean(!selected), i, 0);
		    if (wc instanceof SaveMapWizardComponent) {
			lp.get(i).setSave(!selected);
		    }
		}
	    }
	}
	if (e.getSource() == selectAll) {
	    for (int i=0; i<table.getRowCount(); i++) {
		if (table.getModel().isCellEditable(i, 0)) {
		    table.getModel().setValueAt(true, i, 0);
		    if (wc instanceof SaveMapWizardComponent) {
			lp.get(i).setSave(true);
		    }
		}
	    }
	}
	if (e.getSource() == selectVisibles) {
	    for (int i=0; i<table.getRowCount(); i++) {
		if (table.getModel().isCellEditable(i, 0)) {
		    boolean visible = lp.get(i).getLayer().isVisible();
		    table.getModel().setValueAt(visible, i, 0);
		    if (wc instanceof SaveMapWizardComponent) {
			lp.get(i).setSave(visible);
		    }
		}
	    }
	}
	if (e.getSource() == selectActives) {
	    for (int i=0; i<table.getRowCount(); i++) {
		if (table.getModel().isCellEditable(i, 0)) {
		    boolean active = lp.get(i).getLayer().isActive();
		    table.getModel().setValueAt(active, i, 0);
		    if (wc instanceof SaveMapWizardComponent) {
			lp.get(i).setSave(active);
		    }
		}
	    }
	}
    }

}
