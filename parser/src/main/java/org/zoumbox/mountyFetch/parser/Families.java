package org.zoumbox.mountyFetch.parser;

import java.util.Optional;

/**
 * La liste des différentes familles de monstre
 */
public enum Families implements WithLabel {

    Animal,
    Démon,
    Humanoïde,
    Insecte,
    Monstre,
    MortVivant("Mort-Vivant");

    private Optional<String> label = Optional.empty();

    Families() {}

    Families(String label) {
        this();
        this.label = Optional.of(label);
    }

    @Override
    public String getLabel() {
        return label.orElse(name());
    }

}
