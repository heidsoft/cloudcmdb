package org.cmdbuild.services.soap.types;

public class RelationExt extends Relation {

	private String card1Code;
	private String card1Description;
	private String card2Code;
	private String card2Description;

	public String getCard1Code() {
		return card1Code;
	}

	public void setCard1Code(final String card1Code) {
		this.card1Code = card1Code;
	}

	public String getCard1Description() {
		return card1Description;
	}

	public void setCard1Description(final String card1Description) {
		this.card1Description = card1Description;
	}

	public String getCard2Code() {
		return card2Code;
	}

	public void setCard2Code(final String card2Code) {
		this.card2Code = card2Code;
	}

	public String getCard2Description() {
		return card2Description;
	}

	public void setCard2Description(final String card2Description) {
		this.card2Description = card2Description;
	}

}
