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
package es.udc.cartolab.gvsig.elle.gui;

import java.awt.BorderLayout;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cresques.cts.IProjection;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.addlayer.AddLayerDialog;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.gui.WizardPanel;
import com.iver.cit.gvsig.gui.panels.CRSSelectPanel;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.jeta.forms.components.panel.FormPanel;

import es.udc.cartolab.gvsig.elle.utils.MapDAO;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class ElleWizard extends WizardPanel {

	private JPanel listPanel = null;
	private JList layerList = null;
	private JList groupList = null;
	private CRSSelectPanel crsPanel = null;
	private DBSession dbs;
	private String[][] layers;
	private View view;


	public void execute() {

	}

	public FLayer getLayer() {

		DBSession dbs = DBSession.getCurrentSession();
		FLayer layer = null;
		if (dbs != null) {
			PluginServices.getMDIManager().setWaitCursor();
			//load layer
			IProjection proj = crsPanel.getCurProj();
			int[] selectedPos = layerList.getSelectedIndices();

	    try {
			if (selectedPos.length > 1) {
		    // TODO: Users should have a preference to say if the want
		    // to load the layers in a group or not.
				layer = layersAsTocGroup(proj,
					selectedPos);
			} else {
				int pos = layerList.getSelectedIndex();
		    layer = getLayer(pos, proj);

		}
	    } catch (SQLException e) {
		JOptionPane.showMessageDialog(this,
			"SQLException: " + e.getMessage(), "SQL Error",
			JOptionPane.ERROR_MESSAGE);
				try {
		    dbs = DBSession.reconnect();
		} catch (DBException e1) {
		    e1.printStackTrace();
				}
	    } catch (DBException e) {
		JOptionPane.showMessageDialog(this,
			"SQLException: " + e.getMessage(), "DB Error",
			JOptionPane.ERROR_MESSAGE);
			}
			PluginServices.getMDIManager().restoreCursor();
		} else {
			//Show no connection error
			JOptionPane.showMessageDialog(this,
					PluginServices.getText(this, "notConnectedError"),
					PluginServices.getText(this, "notConnected"),
					JOptionPane.ERROR_MESSAGE);
		}

		return layer;
	}

	private FLayer layersAsTocGroup(IProjection proj,
 int[] selectedPos)
	    throws SQLException, DBException {
	FLayers layers = new FLayers();
	layers.setName("ELLE");
	layers.setMapContext(view.getMapControl().getMapContext());
	for (int pos : selectedPos) {
	    layers.addLayer(getLayer(pos, proj));
	}

	return layers;
	}

	protected String getWhereClause() {
		return "";
	}

	private FLayer getLayer(int pos, IProjection proj) throws SQLException, DBException {
		String layerName = layers[pos][1];
		String tableName = layers[pos][2];

		String schema = null;
		if (layers[pos].length > 8) {
			if (layers[pos][8].length()>0) {
				schema = layers[pos][8];
			}
		}

		String whereClause = getWhereClause();

		if (schema!=null) {
			return dbs.getLayer(layerName, tableName, schema, whereClause, proj);
		} else {
			return dbs.getLayer(layerName, tableName, whereClause, proj);
		}
	}

	public void initWizard() {
		if (!(PluginServices.getMDIManager().getActiveWindow() instanceof View)) {
			return;
		}

		view = (View) PluginServices.getMDIManager().getActiveWindow();

		dbs = DBSession.getCurrentSession();
		setTabName("ELLE");

		setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		if (dbs != null) {

			mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, PluginServices.getText(this, "Choose_Layer"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));


			mainPanel.add(getListPanel(), BorderLayout.CENTER);
			mainPanel.add(getCRSPanel(), BorderLayout.SOUTH);

		} else {
			JLabel label = new JLabel(PluginServices.getText(this, "notConnectedError"));
			mainPanel.add(label, BorderLayout.NORTH);
		}
		add(mainPanel, BorderLayout.CENTER);

	}

	private JPanel getListPanel() {
		if (listPanel == null) {

			listPanel = new JPanel();

			try {

				FormPanel form = new FormPanel("forms/loadLayer.jfrm");
				form.setFocusTraversalPolicyProvider(true);

				listPanel.add(form);

				dbs = DBSession.getCurrentSession();

				if (dbs.tableExists(dbs.getSchema(), "_map")) {
		    String[] maps = MapDAO.getInstance().getMaps();
					layerList = form.getList("layerList");
					groupList = form.getList("groupList");
					groupList.setListData(maps);

					groupList.addListSelectionListener(new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent arg0) {
							int[] selected = groupList.getSelectedIndices();
							if (selected.length == 1) {
										String where = String
												.format("where map_name ='%s'",
														groupList
																.getSelectedValues()[0]);
								try {
									layers = dbs.getTable("_map", where);
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									JOptionPane.showMessageDialog(null,
											"Error SQL: " + e.getMessage(),
											"SQL Exception",
											JOptionPane.ERROR_MESSAGE);
									try {
										dbs = DBSession.reconnect();
									} catch (DBException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
								if (layers != null) {
									String[] layerNames = new String[layers.length];
									for (int i=0; i<layers.length; i++) {
										layerNames[i] = layers[i][1];
									}
									layerList.setListData(layerNames);
								}
							} else {
								layerList.setListData(new Object[0]);
							}
							callStateChanged(false);
						}

					});

					layerList.addListSelectionListener(new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent arg0) {
							int[] selected = groupList.getSelectedIndices();
							if (selected.length > 0) {
								callStateChanged(true);
							} else {
								callStateChanged(false);
							}
						}

					});
				} else {
					listPanel = new JPanel();
					JLabel label = new JLabel(PluginServices.getText(this, "no_map_table_on_schema"));
					listPanel.add(label);
				}

			} catch (SQLException e) {
				listPanel = new JPanel();
				JLabel label = new JLabel("SQL Exception: " + e.getMessage());
				listPanel.add(label);
				try {
					dbs = DBSession.reconnect();
				} catch (DBException e1) {
					e1.printStackTrace();
				}
			}

		}

		return listPanel;

	}

	private JPanel getCRSPanel() {
		if (crsPanel == null) {
			crsPanel = CRSSelectPanel.getPanel(AddLayerDialog.getLastProjection());
			crsPanel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (crsPanel.isOkPressed()) {
						AddLayerDialog.setLastProjection(crsPanel.getCurProj());
					}
				}
			});
		}
		return crsPanel;
	}

}
