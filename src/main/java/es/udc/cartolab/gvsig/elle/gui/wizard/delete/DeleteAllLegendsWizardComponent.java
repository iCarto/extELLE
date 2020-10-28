package es.udc.cartolab.gvsig.elle.gui.wizard.delete;

import static es.icarto.gvsig.commons.i18n.I18n._;

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

import org.gvsig.andami.messages.NotificationManager;
import org.gvsig.fmap.dal.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.utils.LoadLegend;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DeleteAllLegendsWizardComponent extends WizardComponent {

	private static final Logger logger = LoggerFactory.getLogger(DeleteAllLegendsWizardComponent.class);

	private JPanel listPanel;
	private JList legendsList;
	private DBSession dbs;

	public DeleteAllLegendsWizardComponent(Map<String, Object> properties) {
		super(properties);
		dbs = DBSession.getCurrentSession();
		setLayout(new BorderLayout());
		add(getListPanel(), BorderLayout.CENTER);
	}

	@Override
	public boolean canFinish() {
		if (legendsList != null) {
			return legendsList.getSelectedIndices().length > 0;
		}
		return false;
	}

	@Override
	public boolean canNext() {
		return false;
	}

	@Override
	public String getWizardComponentName() {
		return "delete_legend_wizard_component";
	}

	@Override
	public void showComponent() throws WizardException {
		// nothing to do
	}

	@Override
	public void finish() throws WizardException {
		int[] indexes = legendsList.getSelectedIndices();
		if (indexes.length > 0) {
			int opt = JOptionPane.showOptionDialog(this, _("delete_legends_confirm_dialog"), _("delete_legends"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
			if (opt == JOptionPane.OK_OPTION) {
				for (int i = 0; i < indexes.length; i++) {
					try {
						LoadLegend.deleteLegends(legendsList.getModel().getElementAt(indexes[i]).toString());
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

				InputStream stream = getClass().getClassLoader().getResourceAsStream("forms/delete.jfrm");
				FormPanel form = new FormPanel(stream);
				form.setFocusTraversalPolicyProvider(true);
				JLabel legendsLabel = form.getLabel("itemsLabel");
				legendsLabel.setText(_("Choose_Legend"));

				listPanel.add(form);

				dbs = DBSession.getCurrentSession();

				if (dbs.tableExists(DBStructure.getSchema(), DBStructure.getMapStyleTable())
						&& dbs.tableExists(DBStructure.getSchema(), DBStructure.getOverviewStyleTable())) {

					String[] legends = LoadLegend.getLegends();
					legendsList = form.getList("itemsList");
					legendsList.setListData(legends);
					legendsList.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent arg0) {
							callStateChanged();
						}
					});
				} else {
					listPanel = new JPanel();
					JLabel label = new JLabel(_("no_legend_table_on_schema"));
					listPanel.add(label);
				}

			} catch (SQLException e) {
				try {
					dbs = DBSession.reconnect();
				} catch (DataException e1) {
					logger.error(e1.getMessage(), e1);
				}
				logger.error(e.getMessage(), e);
			} catch (FormException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return listPanel;
	}

}
