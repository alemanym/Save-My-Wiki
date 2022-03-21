package com.savemywiki.tools.export.ui.table.wikinamespace;

import java.awt.Component;

import javax.swing.JTable;

import com.savemywiki.tools.export.ui.table.SimpleHeaderRenderer;

public class WikiNamespaceTableHeaderRenderer extends SimpleHeaderRenderer {

	private static final long serialVersionUID = 8696339590198015143L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		switch (column) {
		case 2:
		case 3:
		case 4:
			setHorizontalAlignment(CENTER);
			break;
		default:
			setHorizontalAlignment(LEFT);
			break;
		}

		return this;
	}
}
