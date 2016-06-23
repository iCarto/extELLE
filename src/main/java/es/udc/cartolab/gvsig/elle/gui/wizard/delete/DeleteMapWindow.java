package es.udc.cartolab.gvsig.elle.gui.wizard.delete;

import java.awt.Component;
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

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.messages.NotificationManager;
import org.gvsig.andami.ui.mdiManager.WindowInfo;
import org.gvsig.fmap.dal.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gui.WidgetFactory;
import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.utils.LoadLegend;
import es.udc.cartolab.gvsig.elle.utils.MapDAO;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class DeleteMapWindow extends AbstractIWindow implements ActionListener {
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(DeleteMapWindow.class);

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

	    mapList.setVisibleRowCount(16);

	    this.add(new JScrollPane(mapList,
		    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "grow");

	    dbs = DBSession.getCurrentSession();

	    if (dbs.tableExists(DBStructure.getSchema(),
		    DBStructure.getMapTable())
		    && dbs.tableExists(DBStructure.getSchema(),
			    DBStructure.getOverviewTable())) {

		String[] maps = MapDAO.getInstance().getMaps();

		setScrollPaneWidth(maps);

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
	    } catch (DataException e1) {
	    	logger.error(e1.getMessage(), e1);
	    }
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * There is a bug in ScrollPane Layout that makes that the horizontal bar is
     * always shown. As the name of the maps should always be "short" there is
     * not problem with set the width of the window bigger enough to show the
     * full name and never show horizontal bar
     * http://stackoverflow.com/questions/11587292/jscrollpane-not-
     * wide-enough-when-vertical-scrollbar-appears
     *
     */
    private void setScrollPaneWidth(String[] maps) {
	String prototypeCellValue = "XXXXXXXXXXXXXXXXXXXXXXXXXX";
	for (String map : maps) {
	    prototypeCellValue = map.length() > prototypeCellValue.length() ? map
		    : prototypeCellValue;
	}
	mapList.setPrototypeCellValue(prototypeCellValue);
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

    @Override
    protected JButton getDefaultButton() {
	return okBtn;
    }

    @Override
    protected Component getDefaultFocusComponent() {
	return null;
    }

}
