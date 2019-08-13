package to.thepiratebay;

public enum IllegalService {
	
	WEED("Weed"),
	TUEUR_A_GAGE("Tueur à gages"),
	BLANCHISSEUR("Blanchiment"),
	ARMES_ILLEGAL("Ventes d'armes illégales");
	
	private String name;
	private IllegalService(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
