package za.co.hydra24.options;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
import java.util.stream.Stream;

/** One choice in a drop-down. */
@RegisterForReflection
public record Option(String value, String label) {

    public static List<Option> of(Labelled[] values) {
        return Stream.of(values).map(value -> new Option(value.name(), value.label())).toList();
    }
}
