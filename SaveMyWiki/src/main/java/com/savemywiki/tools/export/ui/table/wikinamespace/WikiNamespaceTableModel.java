package com.savemywiki.tools.export.ui.table.wikinamespace;

import javax.swing.SwingUtilities;

import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.ExportData;
import com.savemywiki.tools.export.model.ModelAdapter;
import com.savemywiki.tools.export.model.TaskStatus;
import com.savemywiki.tools.export.model.WikiNamespaceData;
import com.savemywiki.tools.export.ui.table.ObjectTableModel;

public class WikiNamespaceTableModel extends ObjectTableModel<WikiNamespaceData> {

	private static final long serialVersionUID = 6960639100277160392L;

	private static final String COLUMN_ID = "#";
	private static final String COLUMN_NAMESPACE = "Type de page";
	private static final String COLUMN_STATUS = "Status";
	private static final String COLUMN_PAGE_NB = "Noms de pages";
	private static final String COLUMN_NB_EXPORTED = "Pages exportées";
	
	private AppModel model;

	public WikiNamespaceTableModel(final AppModel model) {
		super();
		this.model = model;
		
		// update row listening
		model.addActionListener(new ModelAdapter() {
			@Override
			public void onNamespaceDataUpdate(WikiNamespaceData wikiData, TaskStatus oldStatus, TaskStatus newStatus) {
				int i = 0;
				for (WikiNamespaceData data : model.getWikiNamespaceDataList()) {
					if (data.getNamespace() == wikiData.getNamespace()) {
						final int n = i;
						SwingUtilities.invokeLater(() -> {
							WikiNamespaceTableModel.this.fireTableRowsUpdated(n, n);
						});
						break;
					}
					i++;
				}
			}
			@Override
			public void onExportDone(ExportData exporData) {
				int i = 0;
				for (WikiNamespaceData data : model.getWikiNamespaceDataList()) {
					if (data.getNamespace() == exporData.getNamespace()) {
						final int n = i;
						SwingUtilities.invokeLater(() -> {
							WikiNamespaceTableModel.this.fireTableRowsUpdated(n, n);
						});
						break;
					}
					i++;
				}
			}
		});
	}

	@Override
	public Object getValueAt(WikiNamespaceData rowData, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return rowData.getNamespace().getId();
		case 1:
			return rowData.getNamespace().desc();
		case 2:
			return rowData;
		case 3:
			if (rowData.getReadStatus() == TaskStatus.UNDEFINED) {
				return "<html><span style=\"color: #777799;\">?</span></html>";
			}
			int pageCount =  rowData.getExportDataList().stream().mapToInt((ExportData e) -> {
				return e.getPageNames().size();
			}).sum();
			switch (rowData.getReadStatus()) {
			case DONE_FAILED:
			case INTERRUPTING:
			case INTERRUPTED:
				return "<html><span style=\"color: #ff0000;\">"+pageCount+"</span></html>";
			case DONE_SUCCESS:
				return "<html><span style=\"color: #00ff00;\">"+pageCount+"</span></html>";
			case PROCESSING:
				return "<html><span style=\"color: #777799;\">"+pageCount+"</span></html>";
			default:
				return "";
			}
		case 4:
			int count = 0;
			int total = 0;
			for (ExportData exportData : rowData.getExportDataList()) {
				if (exportData.getStatus() == TaskStatus.DONE_SUCCESS) {
					count += exportData.getPageNames().size();
				}
				total += exportData.getPageNames().size();
			}
			for (ExportData exportData : model.getRetryList()) {
				if (exportData.getNamespace() == rowData.getNamespace()
						&& exportData.getStatus() == TaskStatus.DONE_SUCCESS) {
					count += exportData.getPageNames().size();
				}
			}
			if (total == 0) {
				return "<html><span style=\"color: #777799;\">-</span></html>";
			} else if (count == total) {
				return "<html><span style=\"color: #00ff00;\">"+ count + "/" + total + "</span></html>";
			} else if (count == 0) {
				return "<html><span style=\"color: #777799;\">0/" + total + "</span></html>";
			} else {
				return total != 0 ? (count + "/" + total) : "-";
			}
		}
		return null;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return COLUMN_ID;
		case 1:
			return COLUMN_NAMESPACE;
		case 2:
			return COLUMN_STATUS;
		case 3:
			return COLUMN_PAGE_NB;
		case 4:
			return COLUMN_NB_EXPORTED;
		default:
			return null;
		}
	}

	public void update(WikiNamespaceData data) {
		// TODO Auto-generated method stub

	}
}
