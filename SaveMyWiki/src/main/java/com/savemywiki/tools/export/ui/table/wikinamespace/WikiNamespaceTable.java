package com.savemywiki.tools.export.ui.table.wikinamespace;

import javax.swing.JTable;

public class WikiNamespaceTable extends JTable {

	private static final long serialVersionUID = -4711694562293862689L;
	
	WikiNamespaceTableModel wikiTableModel;
	
	public WikiNamespaceTable(WikiNamespaceTableModel wikiTableModel) {
		super(wikiTableModel);
		this.wikiTableModel = wikiTableModel;
	}

}
