package es.icarto.gvsig.elle.db;


public class DBStructure {

    public static String MAP_TABLE = "_map";
    public static String OVERVIEW_TABLE = "_map_overview";
    public static String MAP_STYLE_TABLE = "_map_style";
    public static String OVERVIEW_STYLE_TABLE = "_map_overview_style";
    public static String WMS_TABLE = "_wms";
    public static String SCHEMA_NAME = "elle";

    public static String getMapTable() {
	return MAP_TABLE;
    }

    public static String getOverviewTable() {
	return OVERVIEW_TABLE;
    }

    public static String getWMSTable() {
	return WMS_TABLE;
    }

    public static String getMapStyleTable() {
	return MAP_STYLE_TABLE;
    }

    public static String getOverviewStyleTable() {
	return OVERVIEW_STYLE_TABLE;
    }

    public static String getSchema() {
	return SCHEMA_NAME;
    }

}
