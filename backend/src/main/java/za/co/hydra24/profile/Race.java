package za.co.hydra24.profile;

import io.quarkus.runtime.annotations.RegisterForReflection;
import za.co.hydra24.options.Labelled;

@RegisterForReflection
public enum Race implements Labelled {
    AFRICAN("African"),
    COLOURED("Coloured"),
    INDIAN("Indian"),
    ASIAN("Asian"),
    WHITE("White");

    private final String label;

    Race(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}
