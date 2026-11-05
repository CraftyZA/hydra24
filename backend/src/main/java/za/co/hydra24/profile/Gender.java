package za.co.hydra24.profile;

import io.quarkus.runtime.annotations.RegisterForReflection;
import za.co.hydra24.options.Labelled;

@RegisterForReflection
public enum Gender implements Labelled {
    MALE("Male"),
    FEMALE("Female");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}
