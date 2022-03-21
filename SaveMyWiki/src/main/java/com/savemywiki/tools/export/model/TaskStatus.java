package com.savemywiki.tools.export.model;

/**
 * Task status (for Read wiki data or export).
 * 
 * @author Marc Alemany
 */
public enum TaskStatus {

	UNDEFINED,
	WARNING,
	PROCESSING,
	INTERRUPTING,
	INTERRUPTED,
	DONE_SUCCESS,
	DONE_FAILED,
	
}
