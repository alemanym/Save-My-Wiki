package com.savemywiki.tools.export.ui.table.wikinamespace;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.WikiNamespaceData;

public class WikiNamespaceTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	private AppModel model;

	public WikiNamespaceTableCellRenderer(AppModel model) {
		super();
		this.model = model;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		// default
		setForeground(null);
		setIcon(null);
		setHorizontalAlignment(LEFT);
		
		switch (column) {
		case 2:
			// status du processus de lecture du wiki
			buildStatus(row);
			break;
		case 3:
			// Nombre de noms de page
			setHorizontalAlignment(CENTER);
			break;
		case 4:
			setHorizontalAlignment(CENTER);
			break;
		default:
			break;
		}

		return this;
	}

	private void buildStatus(int row) {
		if (row < 0) {
			return;
		}
		setHorizontalAlignment(CENTER);
		setForeground(Color.GRAY);
		WikiNamespaceData data = model.getWikiNamespaceDataList().get(row);
		
		switch (data.getExportStatus()) {
		case INTERRUPTED:
			setText("<html><span style=\"color: #ff0000;\">Interrompu</span></html>");
			return;
		case INTERRUPTING:
			setText("<html><span style=\"color: orange;\">Interrompu</span></html>");
			return;
		case WARNING:
			setText("<html><span style=\"color: orange;\">Requête(s) à refaire</span></html>");
			return;
		case DONE_FAILED:
			setText("<html><span style=\"color: #ff0000;\">Erreur</span></html>");
			return;
		case PROCESSING:
			setText("<html><span style=\"color: #ffffff;\">Export en cours...</span></html>");
			return;
		case DONE_SUCCESS:
			setText("<html><span style=\"color: #777799;\">Export effectué</span></html>");
			return;
		default:
		break;
		}
		
		switch (data.getReadStatus()) {
		case UNDEFINED:
			setText("<html><span style=\"color: #777799;\">Infos à récupérer</span></html>");
		break;
		case INTERRUPTED:
			setText("<html><span style=\"color: #ff0000;\">Interrompu</span></html>");
		break;
		case INTERRUPTING:
			setText("<html><span style=\"color: orange;\">Interrompu</span></html>");
		break;
		case DONE_FAILED:
			setText("<html><span style=\"color: #ff0000;\">Erreur</span></html>");
		break;
		case PROCESSING:
			setText("<html><span style=\"color: #ffffff;\">Lecture en cours...</span></html>");
		break;
		case DONE_SUCCESS:
			if (data.getExportDataList().size() > 0) {
				setText("<html><span style=\"color: #777799;\">Noms récupérés</span></html>");	
			} else {
				setText("<html><span style=\"color: #777799;\">Aucune page</span></html>");
			}
		break;
		default:
		break;
		}
	}

}
