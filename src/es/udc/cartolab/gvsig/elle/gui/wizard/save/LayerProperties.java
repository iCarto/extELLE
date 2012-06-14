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

import java.sql.SQLException;

import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.drivers.jdbc.postgis.PostGisDriver;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.VectorialDBAdapter;

import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;

public class LayerProperties implements Comparable {


    private String schema, tablename, layername;
    private FLyrVect layer;
    private String group = "";
    private boolean save = true, visible = false;
    private double maxScale = -1, minScale = -1;
    private int position;
    private String legendType;


    public LayerProperties(FLyrVect layer) throws WizardException {
	VectorialDriver driver = (layer).getSource().getDriver();
	if (driver instanceof PostGisDriver) {
	    DBLayerDefinition layerDef = ((VectorialDBAdapter) (layer).getSource()).getLyrDef();

	    this.layer = layer;

	    this.schema = layerDef.getSchema();
	    this.tablename = layerDef.getTableName();
	    this.layername = layer.getName();
	} else {
	    throw new WizardException("");
	}
    }

    public LayerProperties(String schema, String tablename, String layername) {

	this.schema = schema;
	this.tablename = tablename;
	this.layername = layername;

    }

    public String getSchema() {
	return schema;
    }

    public String getTablename() {
	return tablename;
    }

    public String getUserName() throws SQLException {
	if (layer != null) {
	    VectorialDriver driver = (layer).getSource().getDriver();
	    return ((ConnectionJDBC)((PostGisDriver) driver).getConnection()).getConnection().getMetaData().getUserName();
	} else {
	    return "";
	}
    }

    public FLyrVect getLayer() {
	return layer;
    }

    /**
     * It returns the layer name on the current view.
     * @return
     */
    public String getLayername() {
	return layername;
    }

    public boolean save() {
	return save;
    }

    public void setSave(boolean save) {
	this.save = save;
    }

    public boolean visible() {
	return visible;
    }

    public void setVisible(boolean visible) {
	this.visible = visible;
    }

    public String getGroup() {
	return group;
    }

    public void setGroup(String group) {
	this.group = group;
    }

    public double getMaxScale() {
	return maxScale;
    }

    public void setMaxScale(double maxScale) {
	this.maxScale = maxScale;
    }

    public double getMinScale() {
	return minScale;
    }

    public void setMinScale(double minScale) {
	this.minScale = minScale;
    }

    public int getPosition() {
	return position;
    }

    public void setPosition(int position) {
	this.position = position;
    }

    public void setLegendType(String type) {
	legendType = type;
    }

    public String getLegendType() {
	return legendType;
    }

    public int compareTo(Object o) {
	if (o instanceof LayerProperties) {
	    LayerProperties lp = (LayerProperties) o;
	    return position - lp.getPosition();
	}
	return 0;
    }
}
