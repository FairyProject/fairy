package io.fairyproject.gradle;

import com.google.gson.JsonObject;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class Lib implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String dependency;
    @Nullable
    private String repository;

    public static Lib fromJsonObject(JsonObject jsonObject) {
        String dependency = jsonObject.get("dependency").getAsString();
        String repository = null;
        if (jsonObject.has("repository")) {
            repository = jsonObject.get("repository").getAsString();
        }
        return new Lib(dependency, repository);
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dependency", this.dependency);
        if (this.repository != null)
            jsonObject.addProperty("repository", this.repository);
        return jsonObject;
    }

    @SuppressWarnings("unused")
    private static void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    // Gradle only needs to serialize objects, so this isn't strictly needed
    @SuppressWarnings("unused")
    private static void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }

}
