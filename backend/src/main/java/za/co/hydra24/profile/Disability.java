package za.co.hydra24.profile;

import io.quarkus.runtime.annotations.RegisterForReflection;
import za.co.hydra24.options.Labelled;

@RegisterForReflection
public enum Disability implements Labelled {
    ABLE_BODIED("Able-bodied"),
    PHYSICALLY_DISABLED("Physically disabled");

    private final String label;

    Disability(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}
