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


/**
 * 
 * @author Francisco Puga <fpuga@cartolab.es> http://conocimientoabierto.es
 * 
 */
public interface IConstant {

    /**
     * If the constants can be ordered by hierarchy, get the order of this
     * constant. Less order, upper in the hierarchy This order is used also to
     * represent the constants in the status bar. Less order more close to the
     * left it will be Return null if there is no order
     */
    public int getOrder();

    /**
     * The TOC name of the layer that has the field that represents this
     * constant
     */
    public String getLayerName();

    /**
     * The name of the field that represents this constant
     */
    public String getFieldName();

    /**
     * The string that represents this constant in the status bar. Returns null
     * to not represent this constant
     */
    public String getNameInStatusBar();

    /**
     * Returns the value that actually holds the constant
     */
    public Object getValue();

    /**
     * Set the value that actually have the constant
     */
    public void setValue(Object value);


}
