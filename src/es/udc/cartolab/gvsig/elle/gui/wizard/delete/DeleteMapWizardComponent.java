package es.udc.cartolab.gvsig.elle.gui.wizard.delete;

import java.awt.BorderLayout;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.utils.MapDAO;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DeleteMapWizardComponent extends WizardComponent {

    private JPanel listPanel;
    private JList mapList;
    private DBSession dbs;

    public DeleteMapWizardComponent(Map<String, Object> properties) {
	super(properties);
	dbs = DBSession.getCurrentSession();
	setLayout(new BorderLayout());
	add(getListPanel(), BorderLayout.CENTER);
    }

    @Override
    public boolean canFinish() {
	if (mapList != null) {
	    return mapList.getSelectedIndices().length > 0;
	}
	return false;
    }

    @Override
    public boolean canNext() {
	return false;
    }

    @Override
    public String getWizardComponentName() {
	return "delete_map_wizard_component";
    }

    @Override
    public void showComponent() throws WizardException {
	// nothing to do
    }

    @Override
    public void finish() throws WizardException {
	int[] indexes = mapList.getSelectedIndices();
	if (indexes.length > 0) {
	    int opt = JOptionPane.showOptionDialog(this,
		    PluginServices.getText(this, "delete_maps_confirm_dialog"),
		    PluginServices.getText(this, "delete_map"),
		    JOptionPane.YES_NO_OPTION,
		    JOptionPane.QUESTION_MESSAGE,
		    null,
		    null,
		    null);
	    if (opt == JOptionPane.OK_OPTION) {
		for (int i = 0; i < indexes.length; i++) {
		    try {
			MapDAO.getInstance().deleteMap(
				mapList.getModel().getElementAt(indexes[i]).toString());
		    } catch (SQLException e) {
			NotificationManager.addError(e);
		    }
		}
	    }
	}
    }

    @Override
    public void setProperties() throws WizardException {
	// Nothing to do
    }

    private JPanel getListPanel() {
	if (listPanel == null) {

	    listPanel = new JPanel();

	    try {
		 InputStream stream = getClass().getClassLoader()
			    .getResourceAsStream("forms/delete.jfrm");
		FormPanel form = new FormPanel(stream);
		form.setFocusTraversalPolicyProvider(true);
		JLabel mapLabel = form.getLabel("itemsLabel");
		mapLabel.setText(PluginServices.getText(this, "Choose_Map"));

		listPanel.add(form);

		dbs = DBSession.getCurrentSession();

		if (dbs.tableExists(DBStructure.getSchema(),
			DBStructure.getMapTable())
			&& dbs.tableExists(DBStructure.getSchema(),
				DBStructure.getOverviewTable())) {

		    String[] maps = MapDAO.getInstance().getMaps();
		    mapList = form.getList("itemsList");
		    mapList.setListData(maps);
		    mapList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
			    callStateChanged();
			}
		    });
		} else {
		    listPanel = new JPanel();
		    JLabel label = new JLabel(PluginServices.getText(this,
			    "no_map_table_on_schema"));
		    listPanel.add(label);
		}

	    } catch (SQLException e) {
		try {
		    dbs = DBSession.reconnect();
		} catch (DBException e1) {
		    e1.printStackTrace();
		}
		e.printStackTrace();
	    } catch (FormException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	return listPanel;
    }

}
