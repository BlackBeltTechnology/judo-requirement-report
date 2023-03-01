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
import java.util.stream.Stream;

import hu.blackbelt.judo.requirement.report.annotation.Requirement;
import hu.blackbelt.judo.requirement.report.annotation.TestCase;

@SupportedAnnotationTypes({
    "hu.blackbelt.judo.requirement.report.annotation.Requirement",
    "hu.blackbelt.judo.requirement.report.annotation.TestCase"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class RequirementProcessor extends AbstractProcessor {

    public static final String ERROR_MSG_NO_REPORT_PATH =
            "The maven-compiler-plugin doesn't have \"reportPath\" compilerArgs. Add this to the pom.xml.\n" +
            "<plugin>\n" +
            "    <groupId>org.apache.maven.plugins</groupId>\n" +
            "    <artifactId>maven-compiler-plugin</artifactId>\n" +
            "    <configuration>\n" +
            "        <compilerArgs>\n" +
            "            <arg>-AreportPath=${project.basedir}/target/classes/requirements-report.csv</arg>\n" +
            "        </compilerArgs>\n" +
            "    </configuration>\n" +
            "</plugin>";

    public static final String REQUIREMENT_REPORT_CSV = "requirements-report.csv";
    public static final String TEST_CASE_REPORT_CSV = "testcase-report.csv";
            
    public RequirementProcessor(){}

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        String reportPath = processingEnv.getOptions().get("reportPath");
        if (reportPath == null || reportPath.isBlank()) {
            throw new RuntimeException(ERROR_MSG_NO_REPORT_PATH);
        }
        
        String fileNameOfReqReportCsv = null;
        String fileNameOfTestCasesCsv = null;
        
        if (reportPath.endsWith("/")) {
            fileNameOfReqReportCsv = reportPath + REQUIREMENT_REPORT_CSV;
            fileNameOfTestCasesCsv = reportPath + TEST_CASE_REPORT_CSV;
        } else {
            fileNameOfReqReportCsv = reportPath + "/" + REQUIREMENT_REPORT_CSV;
            fileNameOfTestCasesCsv = reportPath + "/" + TEST_CASE_REPORT_CSV;
        }

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "RequirementProcessor start."
        );

        if (annotations.isEmpty()) {
            return false;
        }
        
        // Annotation[] -> Element[] -> AnnotatedElement[]
        List<AnnotatedElement> annotatedMethods = annotations.stream()
                .flatMap(a -> roundEnv.getElementsAnnotatedWith(a).stream())
                // filter the duplications
                .collect(Collectors.toSet()).stream()
                .map(a -> new AnnotatedElement(a))
                // sort by test case id
                .sorted()
                .collect(Collectors.toList());
                
        // check ids of all test case are unique
        AnnotatedElement prevAe = null;
        Set<AnnotatedElement> wrongTCs= new HashSet<AnnotatedElement>();
        for (AnnotatedElement ae : annotatedMethods) {
            if (prevAe != null) {
                if (!(ae.getTestCaseId().equals("")) && ae.getTestCaseId().equals(prevAe.getTestCaseId())) {
                    // This TestCaseId isn't unique
                    wrongTCs.add(prevAe);
                    wrongTCs.add(ae);
                } else {
                    // update the result string of wrongTCs
                    wrongTCs.stream()
                        .forEach(
                                tc ->
                                tc.addResultStringForTestCaseReport(
                                        "Test case id isn't unique. It is used by " +
                                                wrongTCs
                                                .stream()
                                                // remove tc from the list.
                                                .filter(
                                                        tca ->
                                                        !(tca.getElementName().equals(tc.getElementName()))
                                                       )
                                                .sorted()
                                                .map(tcb -> tcb.getElementName())
                                                .collect(Collectors.joining(", "))
                                )
                        );
                    
                    // clear the wrongTCs
                    wrongTCs.clear();
                }
            }
            prevAe = ae;
        }
        
        // create the list of test-cases
        writeTestCases(new File(fileNameOfTestCasesCsv), annotatedMethods.stream());

        // create the requirements-report
        writeRequirementReportCsv(
                new File(fileNameOfReqReportCsv),
                annotatedMethods
                    .stream()
                    .flatMap(e -> collectReqForElement(e).stream())
        );

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "RequirementProcessor end."
        );
        return false;
    }
    
    private Collection<Info> collectReqForElement(AnnotatedElement e) {
        return Arrays.stream(
                    (e.getReqAnnotation().reqs() == null || e.getReqAnnotation().reqs().length == 0) ?
                            new String[]{""}
                            : e.getReqAnnotation().reqs()
                )
                .map(a -> new Info(e.getElementName(), e.getTestCaseId(), e.getResultStringForRequirementReport(), a))
                .collect(Collectors.toSet());
    }

    private void writeTestCases(File file, Stream<AnnotatedElement> elements) {
        chkDirectory(file);
        
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .withIgnoreQuotations(true)
                .build();

        try (ICSVWriter out = new CSVWriterBuilder(new FileWriter(file))
                .withParser(parser)
                .build()
        ) {
            // file header
            out.writeNext(new String[]{"TEST CASE ID", "TEST METHOD", "STATUS"});
            
            // write rows
            elements.forEach(i -> out.writeNext(i.toTestCaseRowStringArray()));
        } catch (IOException e) {
            throw new RuntimeException("RequirementProcessor error", e);
        }
    }

    private void writeRequirementReportCsv(File file, Stream<Info> elements) {
        chkDirectory(file);

        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .withIgnoreQuotations(true)
                .build();

        try (ICSVWriter out = new CSVWriterBuilder(new FileWriter(file))
                .withParser(parser)
                .build()
        ) {
            // file header
            out.writeNext(new String[]{"TEST METHOD","TEST CASE ID","STATUS","REQUIREMENT"});
            
            // write rows
            elements.forEach(i -> out.writeNext(i.toRequirementReportRowStringArray()));
        } catch (IOException e) {
            throw new RuntimeException("RequirementProcessor error", e);
        }
    }

    private void chkDirectory(File file) {
        try {
            file.getParentFile().mkdirs();
        } catch (Exception e) {
            throw new RuntimeException(
                    "RequirementProcessor error: Can't create this directory: " +
                            file.getParentFile().getAbsolutePath(),
                    e
            );
        }
    }

    /**
     * This class contains all important meta info of an annotated element.
     *
     */
    private class AnnotatedElement implements Comparable<AnnotatedElement>  {
        private Element element = null;
        private String elementName;
        private boolean isTestCaseAnnotation;
        private String testCaseId;
        private boolean isTestAnnotation;
        private Requirement reqAnnotation;
        private String resultStringForRequirementReport;
        private String resultStringForTestCaseReport;
        
        public AnnotatedElement(Element element) {
            this.element = element;
            this.elementName = element.getEnclosingElement().getSimpleName() + "." + element.getSimpleName();
            this.reqAnnotation = element.getAnnotation(Requirement.class);
            this.isTestAnnotation = (element.getAnnotation(Test.class) != null) ? true : false;
            
            TestCase testCaseAnnotation = element.getAnnotation(TestCase.class);
            if (testCaseAnnotation != null) {
                this.isTestCaseAnnotation = true;
                if (testCaseAnnotation.value() != null) {
                    this.testCaseId = testCaseAnnotation.value();
                    this.resultStringForTestCaseReport = (this.testCaseId.equals("")) ? "Empty string isn't a valid value of a @TestCase annotation." : "OK";
                } else {
                    this.testCaseId = "";
                    this.resultStringForTestCaseReport = "No value of @TestCase annotation.";
                }
            } else {
                this.isTestCaseAnnotation = false;
                this.testCaseId = "";
                this.resultStringForTestCaseReport = "Missing annotation: @TestCase.";
            }
            
            if ( reqAnnotation != null
                    && reqAnnotation.reqs().length > 0
                    && isTestAnnotation
            ) {
                // everything is OK
                this.resultStringForRequirementReport = "OK";
            }
            else if (reqAnnotation != null
                    && reqAnnotation.reqs().length == 0
                    && isTestAnnotation
            ) {
                // There isn't any requirement id.
                this.resultStringForRequirementReport = "There isn't any requirement id.";
            }
            else if ( reqAnnotation != null
            ) {
                // Missing annotation: @Test.
                this.resultStringForRequirementReport = "Missing annotation: @Test.";
            }
            else {
                throw new RuntimeException("There is a big problem. We should not be here.");
            }

        }
        
        public String getElementName() {
            return elementName;
        }
        
        public Requirement getReqAnnotation() {
            return reqAnnotation;
        }
        
        public String getTestCaseId() {
            return testCaseId;
        }

        public String getResultStringForRequirementReport() {
            return resultStringForRequirementReport;
        }

        @Override
        public int compareTo(AnnotatedElement arg0) {
            if (arg0 == null) {
                throw new RuntimeException("The null value isn't comparable.");
            }
            int result = this.testCaseId.compareTo(arg0.testCaseId);
            if (result == 0) {
                result = this.elementName.compareTo(arg0.elementName);
            }
            return result;
        }
        
        public void addResultStringForTestCaseReport(String resultStringForTestCaseReport) {
            this.resultStringForTestCaseReport = (this.resultStringForTestCaseReport.equals("OK")) ?
                    resultStringForTestCaseReport
                    : this.resultStringForTestCaseReport + " " + resultStringForTestCaseReport;
        }

        String[] toTestCaseRowStringArray() {
            return new String[]{
                    this.testCaseId,
                    this.elementName,
                    this.resultStringForTestCaseReport
                    };
        }
    }
    
    private class Info {
        String testMethod;
        String status;
        String reqId;
        String testCaseId;

        public Info(String testMethod, String testCaseId, String status, String reqId) {
            this.testMethod = testMethod;
            this.status = status;
            this.reqId = reqId;
            this.testCaseId = testCaseId;
        }

        String[] toRequirementReportRowStringArray() {
            return new String[]{testMethod, testCaseId, status, (reqId == null ? "" : reqId)};
        }
    }
}
