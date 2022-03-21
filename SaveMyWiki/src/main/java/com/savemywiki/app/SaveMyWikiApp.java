package com.savemywiki.app;

import com.savemywiki.tools.export.ctrl.AppController;

/**
 * @author Marc Alemany
 * 
 * Main Application Class
 */
public class SaveMyWikiApp {

	public static void main(String[] args) {
		
		AppController controller = new AppController();
		controller.init(); 
	}

}
