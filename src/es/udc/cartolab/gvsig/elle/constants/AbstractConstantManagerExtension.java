package es.udc.cartolab.gvsig.elle.constants;

import java.util.HashMap;

import com.iver.andami.plugins.Extension;

public abstract class AbstractConstantManagerExtension extends Extension {

    // TODO: Use a Object HashMap allows handled complex "constants" like the
    // "municipios" field on Constants.java on extEIELUtils, but this make that
    // we have to cast the results of getConstants all the time
    // Maybe use another approach like define a IConstants interface for the
    // objects that handles the values itself will be useful
    HashMap<String, Object> constants = new HashMap<String, Object>();


    public void setConstant(String constantName, Object constantValue) {
	// TODO: It should be better that the extension that implements this
	// defined the available keys and here only set the value if the key
	// already exists
	constants.put(constantName, constantValue);
    }

    public Object getConstant(String constantName) {
	return constants.get(constantName);
    }

    public void clearConstants() {
	// TODO: Maybe we should not clear, we should maintains the keys and put
	// to null all the values of the entries
	constants.clear();
    }

    public boolean areConstantsSetFor(String constantName) {
	// TODO: We should put here a hook that allows the implementing
	// extension check the business logic
	// Maybe use a TreeMap allow us a trivial test: The values for the
	// higher hierarchies is set

	return constants.get(constantName) != null;
    }

    @Override
    public void execute(String actionCommand) {
    }

}
