package com.savemywiki.utils;

import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.ui.AppView;

public class Logger {

	private AppModel model;
	private AppView view;
	
	public Logger(AppModel model, AppView view) {
		this.model = model;
		this.view = view;
	}

	public void appendLog(String str) {
		view.appendLog("[" + model.fromStartDate() + "] " + str);
	}

	public void appendProgress(String str) {
		view.appendProgress(str);
	}

}
