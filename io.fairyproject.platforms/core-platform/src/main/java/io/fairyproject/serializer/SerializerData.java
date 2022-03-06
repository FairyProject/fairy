package io.fairyproject.serializer;

import io.fairyproject.ObjectSerializer;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class SerializerData {

    private final ObjectSerializer<?, ?> serializer;
    private final boolean avoidDuplication;

}
