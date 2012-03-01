/*
 * Copyright (c) 2010. Cartolab (Universidade da Coru�a)
 *
 * This file is part of ELLE
 *
 * ELLE is based on the forms application of GisEIEL <http://giseiel.forge.osor.eu/>
 * devoloped by Laboratorio de Bases de Datos (Universidade da Coru�a)
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

package es.udc.cartolab.gvsig.elle.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cresques.cts.IProjection;

import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;

import es.udc.cartolab.gvsig.elle.gui.wizard.save.LayerProperties;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class ELLEMap {

	private String name;
	private int styleSource = LoadLegend.NO_LEGEND;
	private List<LayerProperties> layers;
	private String styleName = "";
	private BaseView view;
	private String whereClause = "";
	private boolean loaded = false;
	private List<LayerProperties> overviewLayers;
	private IProjection projection;

	public ELLEMap(String name, BaseView view) {
		this.setName(name);
		this.setView(view);
		layers = new ArrayList<LayerProperties>();
		overviewLayers = new ArrayList<LayerProperties>();
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param styleSource the styleSource to set
	 */
	public void setStyleSource(int styleSource) {
		this.styleSource = styleSource;
	}

	/**
	 * @return the styleSource
	 */
	public int getStyleSource() {
		return styleSource;
	}

	/**
	 * @param styleName the styleName to set
	 */
	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	/**
	 * @return the styleName
	 */
	public String getStyleName() {
		return styleName;
	}

	/**
	 * @return the view
	 */
	public BaseView getView() {
		return view;
	}

	private void setView(BaseView view) {
		this.view = view;
	}


	/**
	 * @param whereClause the whereClause to set
	 */
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	/**
	 * @return the whereClause
	 */
	public String getWhereClause() {
		return whereClause;
	}

	private FLayers getGroup(FLayers layers, String group) {
		for (int i=0; i<layers.getLayersCount(); i++) {
			FLayer l = layers.getLayer(i);
			if (l instanceof FLayers) {
				if (l.getName().equalsIgnoreCase(group)) {
					return (FLayers) l;
				} else {
					FLayers g = getGroup((FLayers) l, group);
					if (g!=null) {
						return g;
					}
				}
			}
		}
		return null;
	}

	public void addLayer(LayerProperties layer) {
		layers.add(layer);
	}

	public void addOverviewLayer(LayerProperties layer) {
		overviewLayers.add(layer);
	}


	/* jlopez
	 *
	 * This method is used in order to retrieve all nested groups
	 * as FLayers with the string representation stored in DB.
	 */
	private List<String> getGroupNames(String allGroups) {
		List<String> groupNames = new ArrayList<String>();
		char previousChar = '/';
		int startName = 0;
		for (int i = 0; i < allGroups.length(); i++) {
			if (allGroups.charAt(i) == '/') {
				// We check whether the slash is being escaped.
				if (previousChar != '\\') {
					if ((i - startName) > 0) {
						// We undo previously existing backslashes duplication and slashes escapes.
						groupNames.add(allGroups.substring(startName, i).replace("\\/", "/").replace("\\\\", "\\"));
					} else {
						// Starting index == ending index --> empty string.
						groupNames.add("");
					}
					startName = i+1;
				}
			}
			if (allGroups.charAt(i) == '\\') {
				if (previousChar == '\\') {
					// The backslash is duplicated, so it's not escaping a slash.
					previousChar = '/';
				} else {
					previousChar = allGroups.charAt(i);
				}
			} else {
				previousChar = allGroups.charAt(i);
			}
	
		}
	
		// We undo previously existing backslashes duplication and slashes escapes.
		groupNames.add(allGroups.substring(startName).replace("\\/", "/").replace("\\\\", "\\"));
	
		return groupNames;
	}

	@SuppressWarnings("unchecked")
	private void loadViewLayers(IProjection proj) {
		//load view layers
		Collections.sort(layers);
		for (LayerProperties lp : layers) {
			FLayers group;
			if (!lp.getGroup().equals("")) {
				List<String> groupNames = getGroupNames(lp.getGroup());
				FLayers currentGroup = view.getMapControl().getMapContext().getLayers();
				for (String name:groupNames) {
					group = getGroup(currentGroup, name);
					if (group == null) {
						group = new FLayers();
						group.setName(name);
						group.setMapContext(view.getMapControl().getMapContext());
						currentGroup.addLayer(group);
					}
					currentGroup = group;
				}
				group = currentGroup;
			} else {
				group = view.getMapControl().getMapContext().getLayers();
			}

			FLayer layer;
			try {
				layer = getMapDAO().getLayer(lp, whereClause, proj);
				if (layer!=null) {
					if (lp.getMaxScale()>-1) {
						layer.setMaxScale(lp.getMaxScale());
					}
					if (lp.getMinScale()>-1) {
						layer.setMinScale(lp.getMinScale());
					}
					layer.setVisible(lp.visible());
					group.addLayer(layer);
					if (layer instanceof FLyrVect && styleSource != LoadLegend.NO_LEGEND) {
						LoadLegend.loadLegend((FLyrVect) layer, styleName, false, styleSource);
					}
				}
			} catch (Exception e) {
				if (e instanceof SQLException || e instanceof DBException) {
					try {
						DBSession.reconnect();
					} catch (DBException e1) {
						e1.printStackTrace();
					}
				}
				e.printStackTrace(); //TODO change this to logger
			}

		}
	}

	@SuppressWarnings("unchecked")
	private void loadOverviewLayers(IProjection proj) {
		Collections.sort(overviewLayers);
		for (LayerProperties lp : overviewLayers) {
			try {
				FLayer ovLayer = getMapDAO().getLayer(lp, whereClause, proj);
				ovLayer.setVisible(true);
				view.getMapOverview().getMapContext().beginAtomicEvent();
				// FLayer ovLayer = layer.cloneLayer(); // why this????
				view.getMapOverview().getMapContext().getLayers()
						.addLayer(ovLayer);
				view.getMapOverview().getMapContext().endAtomicEvent();
				if (ovLayer instanceof FLyrVect
						&& styleSource != LoadLegend.NO_LEGEND) {
					LoadLegend.loadLegend((FLyrVect) ovLayer, styleName, true,
							styleSource);
				}
			} catch (Exception e) {
				if (e instanceof SQLException || e instanceof DBException) {
					try {
						DBSession.reconnect();
					} catch (DBException e1) {
						e1.printStackTrace();
					}
				}
				e.printStackTrace(); //TODO change this to logger
			}
		}
	}

	public void load(IProjection proj) {
		if (!loaded) {
			//load view layers
			loadViewLayers(proj);
			//load overview layers
			loadOverviewLayers(proj);
			getMapDAO().addLoadedMap(this);
			loaded = true;
			projection = proj;
		}
	}

	public boolean layerInMap(String layerName) {
		for (LayerProperties lp : layers) {
			if (lp.getLayername().equalsIgnoreCase(layerName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * reloads view layers
	 * @param proj
	 */
	public void reload() {
		if (loaded) {
			removeViewLayers();
			loadViewLayers(projection);
		}
	}

	private void removeViewLayers() {
		for (LayerProperties lp : layers) {
			FLayer layer = view.getMapControl().getMapContext().getLayers().getLayer(lp.getLayername());
			if (layer != null) {
				view.getMapControl().getMapContext().getLayers().removeLayer(layer);
			}
			FLayer group = view.getMapControl().getMapContext().getLayers().getLayer(lp.getGroup());
			if (group!=null && group instanceof FLayers && ((FLayers) group).getLayersCount()==0) {
				view.getMapControl().getMapContext().getLayers().removeLayer(group);
			}
		}
	}

	private void removeOverviewLayers() {
		for (LayerProperties lp : overviewLayers) {
			FLayer layer = view.getMapOverview().getMapContext().getLayers().getLayer(lp.getLayername());
			if (layer != null) {
				view.getMapOverview().getMapContext().getLayers().removeLayer(layer);
			}
		}
	}

	public void removeMap() {
		removeViewLayers();
		removeOverviewLayers();
		loaded = false;
		getMapDAO().removeLoadedMap(this);
	}

	protected MapDAO getMapDAO() {
		return MapDAO.getInstance();
	}

}
