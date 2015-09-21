package es.udc.cartolab.gvsig.elle.gui.wizard.delete;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.drivers.DBException;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gui.WidgetFactory;
import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.utils.LoadLegend;
import es.udc.cartolab.gvsig.elle.utils.MapDAO;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class DeleteMapWindow extends AbstractIWindow implements ActionListener {

    protected JList mapList;
    private DBSession dbs;
    private final JButton okBtn;

    public DeleteMapWindow() {
	super();
	okBtn = WidgetFactory.okCancelPanel(this, this, this).getOkButton();
	okBtn.setEnabled(false);
	setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
	setWindowTitle("delete_map");
	dbs = DBSession.getCurrentSession();
	setUpUI();
    }

    private void setUpUI() {

	try {
	    JLabel mapLabel = WidgetFactory.labelTitled(PluginServices.getText(
		    this, "Choose_Map"));
	    this.add(mapLabel, "wrap");
	    mapList = new JList();
	    mapList.setPrototypeCellValue("XXXXXXXXXXXXXXXXXXXXXXXXXX");
	    mapList.setVisibleRowCount(20);
	    JScrollPane s = new JScrollPane();
	    this.add(new JScrollPane(mapList));

	    dbs = DBSession.getCurrentSession();

	    if (dbs.tableExists(DBStructure.getSchema(),
		    DBStructure.getMapTable())
		    && dbs.tableExists(DBStructure.getSchema(),
			    DBStructure.getOverviewTable())) {

		String[] maps = MapDAO.getInstance().getMaps();

		mapList.setListData(maps);
		mapList.addListSelectionListener(new ListSelectionListener() {
		    @Override
		    public void valueChanged(ListSelectionEvent arg0) {
			okBtn.setEnabled(mapList.getSelectedIndex() != -1);
		    }
		});
	    } else {
		JLabel label = new JLabel(PluginServices.getText(this,
			"no_map_table_on_schema"));
		this.add(label);
	    }

	} catch (SQLException e) {
	    try {
		dbs = DBSession.reconnect();
	    } catch (DBException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();
	if (command.equals(OkCancelPanel.OK_ACTION_COMMAND)) {
	    finish();
	}
	closeDialog();
    }

    public void finish() {
	int[] indexes = mapList.getSelectedIndices();
	if (indexes.length > 0) {
	    int opt = JOptionPane.showOptionDialog(this,
		    PluginServices.getText(this, "delete_maps_confirm_dialog"),
		    PluginServices.getText(this, "delete_map"),
		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
		    null, null, null);
	    if (opt == JOptionPane.OK_OPTION) {
		for (int i = 0; i < indexes.length; i++) {
		    try {
			final String mapName = mapList.getModel()
				.getElementAt(indexes[i]).toString();
			MapDAO.getInstance().deleteMap(mapName);
			LoadLegend.deleteLegends(mapName);
		    } catch (SQLException e) {
			NotificationManager.addError(e);
		    }
		}
	    }
	}
    }

}
