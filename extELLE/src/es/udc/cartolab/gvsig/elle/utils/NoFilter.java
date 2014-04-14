package es.udc.cartolab.gvsig.elle.utils;

public class NoFilter implements MapFilter {

    @Override
    public String[] filter(String[] maps) {
	return maps;
    }

}
