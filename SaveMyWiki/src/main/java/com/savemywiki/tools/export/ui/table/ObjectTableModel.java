package com.savemywiki.tools.export.ui.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public abstract class ObjectTableModel<T> extends AbstractTableModel {

	private static final long serialVersionUID = 5238995105124002253L;

	private List<T> objectRows = new ArrayList<>();

	public List<T> getObjectRows() {
		return objectRows;
	}

	public void setObjectRows(List<T> objectRows) {
		this.objectRows = objectRows;
	}

	@Override
	public int getRowCount() {
		return objectRows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		T t = objectRows.get(rowIndex);
		return getValueAt(t, columnIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (objectRows.isEmpty()) {
			return Object.class;
		}
		Object valueAt = getValueAt(0, columnIndex);
		return valueAt != null ? valueAt.getClass() : Object.class;
	}

	public abstract Object getValueAt(T t, int columnIndex);

	@Override
	public abstract String getColumnName(int column);
}