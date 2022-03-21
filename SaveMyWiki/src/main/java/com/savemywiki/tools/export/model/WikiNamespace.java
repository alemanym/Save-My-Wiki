package com.savemywiki.tools.export.model;

public enum WikiNamespace {
	
//	MEDIA("-2"),
//	SPECIAL("-1"),
	GENERAL("0", "G�n�ral"),
	DISCUSSION("1", "Discussion"),
	UTILISATEUR("2", "Utilisateur"),
	DISCUSSION_UTILISATEUR("3", "Discussion Utilisateur"),
	SITE("4", "Site"),
	DISCUSSION_SITE("5", "Discussion Site"),
	FICHIER("6", "Fichier"),
	DISCUSSION_FICHIER("7", "Discussion Fichier"),
	MEDIAWIKI("8", "Mediawiki"),
	DISCUSSION_MEDIAWIKI("9", "Discussion Mediawiki"),
	MODELE("10", "Mod�le"),
	DISCUSSION_MODELE("11", "Discussion Mod�le"),
	AIDE("12", "Aide"),
	DISCUSSION_AIDE("13", "Discussion Aide"),
	CATEGORIE("14", "Cat�gorie"),
	DISCUSSION_CATEGORIE("15", "Discussion Cat�gorie"),
	GADGET("2300", "Gadget"),
	DISCUSSION_GADGET("2301", "Discussion Gadget"),
	DEFINITION_DE_GADGET("2302", "D�finition de Gadget"),
	DISCUSSION_DEFINITION_DE_GADGET("2303", "Discussion D�finition de Gadget"),
	;

	private String id;
	private String desc;
	
	private WikiNamespace(String id, String desc) {
		this.id = id;
		this.desc = desc;
	}
	
	public String getId() {
		return id;
	}

	public String desc() {
		return desc;
	}
	
}
