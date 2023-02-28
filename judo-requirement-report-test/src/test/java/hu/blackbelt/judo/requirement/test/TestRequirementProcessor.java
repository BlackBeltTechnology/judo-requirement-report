package hu.blackbelt.judo.requirement.test;

/*-
 * #%L
 * JUDO Requirement :: Report :: Test
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

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import hu.blackbelt.judo.requirement.report.annotation.Requirement;
import hu.blackbelt.judo.requirement.report.annotation.TestCase;
import hu.blackbelt.judo.requirement.report.processor.RequirementProcessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.io.FileReader;
import java.lang.annotation.Annotation;
import java.util.*;
import com.opencsv.CSVReader;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class TestRequirementProcessor {

    @Requirement(reqs = {

    })
    @TestCase("")
    @Test
    public void test00(){
        assertTrue(true);
    }

    @Requirement(reqs = {
            "R01"
    })
    @Test
    public void test01(){
        assertTrue(true);
    }

    @Requirement(reqs = {
            "R02"
    })
    @TestCase("TC01")
    @Test
    public void test02(){
        assertTrue(true);
    }

    @Requirement(reqs = {
            "R01",
            "R03"
    })
    @TestCase("TC03")
    @Test
    public void test03(){
        assertTrue(true);
    }

    @Requirement(reqs = {
            "R05",
            "R04",
            "R01"
    })
    @TestCase("TC02")
    @Test
    public void test04(){
        assertTrue(true);
    }

    @Requirement(reqs = {
            "R01",
            "R03"
    })
    @TestCase("TC04")
    /*
     * This is not a real test. Therefore, it isn't used the @Test annotation.
     */
    public void test05(){
        assertTrue(false);
    }


    @Test
    /*
     * This case verifies that the RequirementProcessor.processor() operation checking the existing of the reportPath option.
     */
    public void testReal01(){
        // Create a dummy RequirementProcessor
        RequirementProcessor rp = new RequirementProcessor();

        // Initialize the dummy processor
        rp.init(
                new ProcessingEnvironment() {
                    @Override
                    public Map<String, String> getOptions() {
                        return new HashMap<>();
                    }

                    @Override
                    public Messager getMessager() {
                        return new Messager() {
                            @Override
                            public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
                            }

                            @Override
                            public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
                            }

                            @Override
                            public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
                            }

                            @Override
                            public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
                            }
                        };
                    }

                    @Override
                    public Filer getFiler() {
                        return null;
                    }

                    @Override
                    public Elements getElementUtils() {
                        return null;
                    }

                    @Override
                    public Types getTypeUtils() {
                        return null;
                    }

                    @Override
                    public SourceVersion getSourceVersion() {
                        return null;
                    }

                    @Override
                    public Locale getLocale() {
                        return null;
                    }
                }
        );

        // start the process method
        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> rp.process(
                        Set.of(new TypeElement[]{}),
                        new RoundEnvironment() {
                            @Override
                            public boolean processingOver() {
                                return false;
                            }

                            @Override
                            public boolean errorRaised() {
                                return false;
                            }

                            @Override
                            public Set<? extends Element> getRootElements() {
                                return Set.of(new Element[]{});
                            }

                            @Override
                            public Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
                                return Set.of(new Element[]{});
                            }

                            @Override
                            public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a) {
                                return Set.of(new Element[]{});
                            }
                        }
                        )
        );

        assertNotNull(error);
        assertEquals(RequirementProcessor.ERROR_MSG_NO_REPORT_PATH, error.getMessage());
    }

    @Test
    /*
     * This is the real test case. This case checks the generated csv.
     */
    public void testReal02(){
        String reportPath = System.getProperty("reportPath");
        // The value of this property has to be the same as reportPath compiler argument.
        assertNotNull(reportPath, "The reportPath system variable must be set.");

        // Reading the generated csv
        List<String[]> table = readCsv(reportPath);

        // Checking the size of the csv
        assertThat(table.size(), greaterThanOrEqualTo(1));

        // First row of the csv
        assertThat(
                table.get(0),
                is(array(
                        equalTo("TEST METHOD"),
                        equalTo("STATUS"),
                        equalTo("REQUIREMENT")
                ))
        );

        //Other rows of the csv
        assertThat(
                table.subList(1, table.size()),
                containsInAnyOrder(
                        is(array(
                                equalTo("TestRequirementProcessor.test05"),
                                equalTo("Missing annotation: @Test."),
                                equalTo("R03")
                        )),
                        is(array(
                                equalTo("TestRequirementProcessor.test03"),
                                equalTo("OK"),
                                equalTo("R03")
                        )),
                        is(array(
                                equalTo("TestRequirementProcessor.test03"),
                                equalTo("OK"),
                                equalTo("R01")
                        )),
                        is(array(
                                equalTo("TestRequirementProcessor.test00"),
                                equalTo("There isn't any requirement id."),
                                equalTo("")
                        )),
                        is(array(
                                equalTo("TestRequirementProcessor.test04"),
                                equalTo("OK"),
                                equalTo("R04")
                        )),
                        is(array(
                                equalTo("TestRequirementProcessor.test02"),
                                equalTo("OK"),
                                equalTo("R02")
                        )),
                        is(array(
                                equalTo("TestRequirementProcessor.test01"),
                                equalTo("OK"),
                                equalTo("R01")
                        )),
                        is(array(
                                equalTo("TestRequirementProcessor.test04"),
                                equalTo("OK"),
                                equalTo("R05")
                        )),
                        is(array(
                                equalTo("TestRequirementProcessor.test05"),
                                equalTo("Missing annotation: @Test."),
                                equalTo("R01")
                        )),
                        is(array(
                                equalTo("TestRequirementProcessor.test04"),
                                equalTo("OK"),
                                equalTo("R01")
                        ))
                )
        );
    }

    private List<String[]> readCsv(String filePath) {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .withIgnoreQuotations(true)
                .build();

        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePath))
                .withCSVParser(parser)
                .build()
        ) {
            return csvReader.readAll();
        }
        catch(Exception e) {
            throw new RuntimeException(
                    "TestRequirementProcessor error: Can't read this file: " + filePath);
        }
    }
}
