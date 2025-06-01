 package theBugApp.backend.dto;

import lombok.Data;
import theBugApp.backend.enums.Country;
import java.util.Set;
import java.util.HashSet;

@Data
public class UpdateUserDto {
    private String photoUrl;
    private Integer reputation;
    private Boolean isConfirmed;
    private Country country;

    // Champs pour indiquer explicitement quels attributs doivent être mis à jour
    private Set<String> fieldsToUpdate = new HashSet<>();

    // Méthodes pour marquer les champs à mettre à jour
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        this.fieldsToUpdate.add("photoUrl");
    }

    public void setReputation(Integer reputation) {
        this.reputation = reputation;
        this.fieldsToUpdate.add("reputation");
    }

    public void setIsConfirmed(Boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
        this.fieldsToUpdate.add("isConfirmed");
    }

    public void setCountry(Country country) {
        this.country = country;
        this.fieldsToUpdate.add("country");
    }

    // Méthodes pour forcer la mise à jour d'un champ même s'il est null
    public void forceUpdatePhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        this.fieldsToUpdate.add("photoUrl");
    }

    public void forceUpdateCountry(Country country) {
        this.country = country;
        this.fieldsToUpdate.add("country");
    }

    // Vérifier si un champ doit être mis à jour
    public boolean shouldUpdate(String fieldName) {
        return fieldsToUpdate.contains(fieldName);
    }

    // Méthode utilitaire pour vérifier si au moins un champ est défini
    public boolean hasUpdates() {
        return !fieldsToUpdate.isEmpty();
    }
}