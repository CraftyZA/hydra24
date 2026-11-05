package za.co.hydra24.classes;

import io.quarkus.runtime.annotations.RegisterForReflection;
import za.co.hydra24.options.Labelled;

@RegisterForReflection
public enum ClassType implements Labelled {
    WATER_ASSAULT("Water Assault"),
    LAND_ASSAULT("Land Assault");

    private final String label;

    ClassType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}
