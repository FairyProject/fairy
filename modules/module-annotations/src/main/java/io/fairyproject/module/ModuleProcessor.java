package io.fairyproject.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"org.fairy.module.Modular"})
public class ModuleProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<? extends Element> annotatedElements = env.getElementsAnnotatedWith(Modular.class);
        if (annotatedElements.isEmpty()) {
            return false;
        }

        for (Element element : annotatedElements) {

            if (!(element instanceof TypeElement)) {
                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Plugin element is not instance of TypeElement");
                continue;
            }

            TypeElement type = ((TypeElement) element);
            JsonObject jsonObject = new JsonObject();
            Modular annotation = type.getAnnotation(Modular.class);

            jsonObject.addProperty("name", annotation.value());
            jsonObject.addProperty("classPath", annotation.classPath());
            jsonObject.addProperty("abstraction", annotation.abstraction());

            JsonArray array = new JsonArray();
            for (Depend depend : annotation.depends()) {
                JsonObject dependObject = new JsonObject();

                dependObject.addProperty("module", depend.value());
                dependObject.addProperty("state", depend.state().name());

                array.add(dependObject);
            }

            try {
                jsonObject.add("depends", array);
                FileObject resource = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "module.json");

                try (Writer writer = resource.openWriter(); BufferedWriter bw = new BufferedWriter(writer)) {
                    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(jsonObject, bw);
                    bw.flush();
                }
            } catch (IOException ex) {
                throw new RuntimeException("Cannot serialize module descriptor: " + ex.getMessage(), ex);
            }
        }

        return true;
    }

}