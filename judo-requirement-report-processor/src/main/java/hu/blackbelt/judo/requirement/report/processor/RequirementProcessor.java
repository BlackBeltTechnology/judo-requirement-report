package hu.blackbelt.judo.requirement.report.processor;

import com.google.auto.service.AutoService;
import org.junit.jupiter.api.Test;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import hu.blackbelt.judo.requirement.report.annotation.Requirement;

@SupportedAnnotationTypes(
        {"hu.blackbelt.judo.requirement.report.annotation.Requirement",
        "org.junit.jupiter.api.Test"}
)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class RequirementProcessor extends AbstractProcessor {

    public RequirementProcessor(){}

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "RequirementProcessor start."
        );

        if (annotations.isEmpty()) {
            return false;
        }

        // Annotation[] -> Element[] -> Info[]
        Collection<Info> infos = annotations.stream()
                .flatMap(a -> roundEnv.getElementsAnnotatedWith(a).stream())
                .collect(Collectors.toSet()).stream()
                .flatMap(e -> collectReqForElement(e).stream())
                .collect(Collectors.toSet());

        writeCsv(new File(processingEnv.getOptions().get("reportPath")), infos);

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "RequirementProcessor end."
        );
        return false;
    }

    private void writeCsv(File file, Collection<Info> infos) {
        file.mkdirs();
        try (PrintWriter out = new PrintWriter(file)) {
            // file header
            out.println("TEST METHOD;STATUS;REQUIREMENTS");
            infos.forEach(i -> out.println(i.toLine()));
        } catch (IOException e) {
            throw new RuntimeException("RequirementProcessor error", e);
        }
    }

    private Collection<Info> collectReqForElement(Element element) {
        Requirement reqAnnotation = element.getAnnotation(Requirement.class);
        Test testAnnotation = element.getAnnotation(Test.class);
        String elementName = element.getEnclosingElement().getSimpleName() + "." + element.getSimpleName();
        if ( reqAnnotation != null
                && reqAnnotation.reqs().length > 0
                && testAnnotation != null
        ) {
            // everything is OK
            return processRequrementAnnotation(reqAnnotation, elementName, "OK");
        }
        else if (reqAnnotation != null
                && reqAnnotation.reqs().length == 0
                && testAnnotation != null
        ) {
            // There isn't any requirement id.
            return processRequrementAnnotation(reqAnnotation, elementName, "There isn't any requirement id.");
        }
        else if ( reqAnnotation != null
            //&& testAnnotation == null
        ) {
            // Missing annotation: @Test.
            return processRequrementAnnotation(reqAnnotation, elementName, "Missing annotation: @Test.");
        }
        else if ( testAnnotation != null
            //&& reqAnnotation == null
        ) {
            // Missing annotation: @Requirement.
            return processRequrementAnnotation(reqAnnotation, elementName, "Missing annotation: @Requirement.");
        }
        else {
            throw new RuntimeException("There is a big problem. We should not be here.");
        }
    }

    private Collection<Info> processRequrementAnnotation(Requirement reqAnnotation, String elementName, String status) {
        if (reqAnnotation != null && reqAnnotation.reqs().length > 0) {
            return Arrays.stream(reqAnnotation.reqs()).map(a -> new Info(elementName, status, a)).collect(Collectors.toSet());
        } else {
            return Arrays.asList(new Info(elementName, status, null));
        }
    }

    private class Info {
        String testMethod;
        String status;
        String reqId;

        public Info(String testMethod, String status, String reqId) {
            this.testMethod = testMethod;
            this.status = status;
            this.reqId = reqId;
        }

        String toLine() {
            return testMethod + ";" + status + ";" + (reqId == null ? "" : reqId);
        }
    }
}
