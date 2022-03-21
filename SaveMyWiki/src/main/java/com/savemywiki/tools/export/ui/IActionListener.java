package com.savemywiki.tools.export.ui;

/**
 * Application GUI Actions listener.
 * 
 * @author Marc Alemany
 */
public interface IActionListener {
	
	void performGetPageNames();
	
	void performExportPages();

	void performSaveXMLFiles();

	void performSaveNamesOnlyFiles();
	
	void stopCurrentProcess();

}
