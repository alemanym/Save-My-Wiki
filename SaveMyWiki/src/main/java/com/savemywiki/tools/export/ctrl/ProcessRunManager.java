package com.savemywiki.tools.export.ctrl;

import com.savemywiki.tools.export.model.AppModel;
import com.savemywiki.tools.export.model.TaskStatus;
import com.savemywiki.utils.AbortProcessException;
import com.savemywiki.utils.Logger;

public class ProcessRunManager {

	private AppModel model;
	private Logger logger;

	public ProcessRunManager(AppModel model, Logger logger) {
		this.model = model;
		this.logger = logger;
	}

	public void pause(long millisec) throws InterruptedException, AbortProcessException {
		checkInterruption();
		if (model.getQueryPauseTime() > 0) {
			logger.appendLog("pause (" + model.getQueryPauseTime() + "ms)...");
			Thread.sleep( millisec);
		}
		checkInterruption();
	}

	public void pause() throws InterruptedException, AbortProcessException {
		checkInterruption();
		pause(model.getQueryPauseTime());
		checkInterruption();
	}

	public void checkInterruption() throws AbortProcessException {
		if (this.model.getCurrenProcessState() == TaskStatus.INTERRUPTING) {
			throw new AbortProcessException();
		}
	}

}
