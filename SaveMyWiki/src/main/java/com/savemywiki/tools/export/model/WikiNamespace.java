package com.savemywiki.tools.export.model;

public enum WikiNamespace {
	
//	MEDIA("-2"),
//	SPECIAL("-1"),
	MAIN("0"),
	DISCUSSION("1"),
	UTILISATEUR("2"),
	DISCUSSION_UTILISATEUR("3"),
	OMNIS_BIBLIOTHECA("4"),
	DISCUSSION_OMNIS_BIBLIOTHECA("5"),
	FICHIER("6"),
	DISCUSSION_FICHIER("7"),
	MEDIAWIKI("8"),
	DISCUSSION_MEDIAWIKI("9"),
	MODELE("10"),
	DISCUSSION_MODELE("11"),
	AIDE("12"),
	DISCUSSION_AIDE("13"),
	CATEGORIE("14"),
	DISCUSSION_CATEGORIE("15"),
	GADGET("2300"),
	DISCUSSION_GADGET("2301"),
	DEFINITION_DE_GADGET("2302"),
	DISCUSSION_DEFINITION_DE_GADGET("2303"),
	;
	
	private static final String EMPTY = " ";
	private static final String UNDERSCORE = "_";
	
	private String id;
	
	private WikiNamespace(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String format() {
		String str = name().replace(UNDERSCORE, EMPTY).toLowerCase();
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
}
