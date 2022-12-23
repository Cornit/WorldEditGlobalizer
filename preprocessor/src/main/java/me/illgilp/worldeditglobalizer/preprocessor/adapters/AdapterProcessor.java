package me.illgilp.worldeditglobalizer.preprocessor.adapters;

import com.google.auto.service.AutoService;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes( {
    "me.illgilp.worldeditglobalizer.preprocessor.adapters.AdapterFilter",
})
@SupportedSourceVersion(SourceVersion.RELEASE_9)
@AutoService(Processor.class)
public class AdapterProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> annotatedHandlers = roundEnvironment.getElementsAnnotatedWith(AdapterFilter.class);
        if (annotatedHandlers.size() == 0) {
            return false;
        }
        HashSet<String> adapters = annotatedHandlers.stream()
            .map(e -> {
                    AdapterFilter filter = e.getAnnotation(AdapterFilter.class);
                    return ((PackageElement) e.getEnclosingElement()).getQualifiedName().toString() +
                        "." +
                        e.getSimpleName() +
                        ";" +
                        Arrays.stream(filter.minMcVersion())
                            .mapToObj(Integer::toString)
                            .collect(Collectors.joining(".")) +
                        ";" +
                        filter.wePluginType().name() +
                        ";" +
                        Arrays.stream(filter.wePluginVersion())
                            .mapToObj(Integer::toString)
                            .collect(Collectors.joining("."));
                }
            ).collect(Collectors.toCollection(HashSet::new));
        try {
            FileObject file = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "resources", "me/illgilp/worldeditglobalizer/server/bukkit/api/worldedit/adapter/adapters.txt");
            Writer writer = file.openWriter();
            writer.write(String.join("\n", adapters));
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

}
