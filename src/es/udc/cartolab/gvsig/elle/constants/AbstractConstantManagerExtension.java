package es.udc.cartolab.gvsig.elle.constants;

import java.util.HashMap;

import com.iver.andami.plugins.Extension;

public abstract class AbstractConstantManagerExtension extends Extension {

    // TODO: Use a Object HashMap allows handled complex "constants" like the
    // "municipios" field on Constants.java on extEIELUtils, but this make that
    // we have to cast the results of getConstants all the time
    // Maybe use another approach like define a IConstants interface for the
    // objects that handles the values itself will be useful
    private HashMap<String, Object> constants = new HashMap<String, Object>();

    // Each time setConstant or clearConstants is used previousConstant is
    // updated
    private HashMap<String, Object> previousConstants = null;

    private ConstantStatusBarControl statusBar = null;

    @Override
    public void postInitialize() {
	statusBar = new ConstantStatusBarControl();
    }

    public void setConstant(String constantName, Object constantValue) {
	previousConstants = (HashMap<String, Object>) constants.clone();

	// TODO: It should be better that the extension that implements this
	// defined the available keys and here only set the value if the key
	// already exists
	constants.put(constantName, constantValue);

	statusBar.setValue(getConstantsInfo());
    }

    public void setConstants(HashMap<String, Object> constants) {
	previousConstants = (HashMap<String, Object>) constants.clone();
	constants = this.constants;

	statusBar.setValue(getConstantsInfo());
    }

    public Object getConstant(String constantName) {
	return constants.get(constantName);
    }

    public HashMap<String, Object> getPreviousConstants() {
	return previousConstants;
    }

    public void clearConstants() {
	previousConstants = (HashMap<String, Object>) constants.clone();

	// TODO: Maybe we should not clear, we should maintains the keys and put
	// to null all the values of the entries
	constants.clear();

	statusBar.setValue(getConstantsInfo());
    }

    public boolean areConstantsSetFor(String constantName) {
	// TODO: We should put here a hook that allows the implementing
	// extension check the business logic
	// Maybe use a TreeMap allow us a trivial test: The values for the
	// higher hierarchies is set

	return constants.get(constantName) != null;
    }

    /**
     * A representative text of the actual state of the constants. The value
     * returned for this method is the message that will be used in the status
     * bar
     * 
     * Implementation Example: 
     * return "C: " + getConstant("Country") + " C: " + getConstant("Region");
     * 
     */
    // String.format(format, args);
    public abstract String getConstantsInfo();

    @Override
    public void execute(String actionCommand) {
    }

}
