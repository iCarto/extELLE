package es.udc.cartolab.gvsig.elle.constants;

public class Constant implements IConstant {

    private Object value;
    final private String layerName;
    final private String fieldName;
    final private int order;
    final private String nameInStatusBar;

    public Constant(String layerName, String fieldName, int order,
	    String nameInStatusBar) {
	this.layerName = layerName;
	this.fieldName = fieldName;
	this.order = order;
	this.nameInStatusBar = nameInStatusBar;
    }

    public int getOrder() {
	return order;
    }


    public String getLayerName() {
	return layerName;
    }


    public String getFieldName() {
	return fieldName;
    }

    public String getNameInStatusBar() {
	return nameInStatusBar;
    }

    public Object getValue() {
	return value;
    }

    public void setValue(Object value) {
	this.value = value;
    }

}
