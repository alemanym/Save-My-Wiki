package com.savemywiki.tools.export.ui;

public interface IActionListener {
	
	void performGetPageNames();
	
	void performExportPages();

	void performSaveXMLFiles();
	
	void stopCurrentProcess();

}
