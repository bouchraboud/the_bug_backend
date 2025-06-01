package theBugApp.backend.enums;

public enum Country {
    // Pays francophones
    FRANCE("France", "FR"),
    BELGIUM("Belgique", "BE"),
    SWITZERLAND("Suisse", "CH"),
    CANADA("Canada", "CA"),
    MOROCCO("Maroc", "MA"),
    TUNISIA("Tunisie", "TN"),
    ALGERIA("Algérie", "DZ"),

    // Pays anglophones principaux
    UNITED_STATES("États-Unis", "US"),
    UNITED_KINGDOM("Royaume-Uni", "GB"),
    AUSTRALIA("Australie", "AU"),

    // Autres pays européens
    GERMANY("Allemagne", "DE"),
    SPAIN("Espagne", "ES"),
    ITALY("Italie", "IT"),
    NETHERLANDS("Pays-Bas", "NL"),
    PORTUGAL("Portugal", "PT"),

    // Autres pays
    BRAZIL("Brésil", "BR"),
    INDIA("Inde", "IN"),
    CHINA("Chine", "CN"),
    JAPAN("Japon", "JP"),
    SOUTH_KOREA("Corée du Sud", "KR");

    private final String displayName;
    private final String code;

    Country(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return displayName;
    }
}