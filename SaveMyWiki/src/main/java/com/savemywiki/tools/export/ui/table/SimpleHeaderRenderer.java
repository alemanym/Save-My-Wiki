package com.savemywiki.tools.export.ui.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class SimpleHeaderRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -2530501333818670826L;

	public SimpleHeaderRenderer() {
		setForeground(Color.WHITE);
		setBorder(new EmptyBorder(5, 5, 5, 5));
	}

	@Override
	public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column) {        
		setText(value.toString());
	    return this;
	}

}