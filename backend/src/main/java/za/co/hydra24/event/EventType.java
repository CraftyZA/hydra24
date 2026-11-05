package za.co.hydra24.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import za.co.hydra24.options.Labelled;

@RegisterForReflection
public enum EventType implements Labelled {
    HYDRA_24("Hydra 24"),
    EVADER("Evader"),
    FISHING("Fishing");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}
