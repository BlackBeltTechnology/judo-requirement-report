package hu.blackbelt.judo.requirement.report.processor;

/*-
 * #%L
 * JUDO Requirement :: Report :: Processor
 * %%
 * Copyright (C) 2018 - 2023 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import com.google.auto.service.AutoService;
import com.opencsv.*;
import org.junit.jupiter.api.Test;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import hu.blackbelt.judo.requirement.report.annotation.Requirement;

@SupportedAnnotationTypes(
        "hu.blackbelt.judo.requirement.report.annotation.Requirement"
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
        try {
            file.getParentFile().mkdirs();
        } catch (Exception e) {
            throw new RuntimeException(
                    "RequirementProcessor error: Can't create this directory: " +
                            file.getParentFile().getAbsolutePath(),
                    e
            );
        }

        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .withIgnoreQuotations(true)
                .build();

        try (ICSVWriter out = new CSVWriterBuilder(new FileWriter(file))
                .withParser(parser)
                .build()
        ) {
            // file header
            out.writeNext(new String[]{"TEST METHOD","STATUS","REQUIREMENT"});
            infos.forEach(i -> out.writeNext(i.toStringArray()));
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
            return processRequirementAnnotation(reqAnnotation, elementName, "OK");
        }
        else if (reqAnnotation != null
                && reqAnnotation.reqs().length == 0
                && testAnnotation != null
        ) {
            // There isn't any requirement id.
            return processRequirementAnnotation(reqAnnotation, elementName, "There isn't any requirement id.");
        }
        else if ( reqAnnotation != null
        ) {
            // Missing annotation: @Test.
            return processRequirementAnnotation(reqAnnotation, elementName, "Missing annotation: @Test.");
        }
        else {
            throw new RuntimeException("There is a big problem. We should not be here.");
        }
    }

    private Collection<Info> processRequirementAnnotation(Requirement reqAnnotation, String elementName, String status) {
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

        String[] toStringArray() {
            return new String[]{testMethod, status, (reqId == null ? "" : reqId)};
        }
    }
}
