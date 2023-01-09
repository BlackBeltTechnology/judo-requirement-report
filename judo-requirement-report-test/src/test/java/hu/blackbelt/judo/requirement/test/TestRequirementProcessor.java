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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class TestRequirementProcessor {

    @Requirement(reqs = {

    })
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
    @Test
    public void test02(){
        assertTrue(true);
    }

    @Requirement(reqs = {
            "R01",
            "R03"
    })
    @Test
    public void test03(){
        assertTrue(true);
    }

    @Requirement(reqs = {
            "R05",
            "R04",
            "R01"
    })
    @Test
    public void test04(){
        assertTrue(true);
    }

    @Requirement(reqs = {
            "R01",
            "R03"
    })
    /**
     * This is not a real test. Therefore, it isn't used the @Test annotation.
     */
    public void test05(){
        assertTrue(false);
    }

    @Test
    /**
     * This is the real test case.
     */
    public void testReal(){
        // Read generated csv
        List<String[]> table = readCsv(System.getProperty("reportPath"));

        // Check the size of csv
        assertThat(table.size(), greaterThanOrEqualTo(1));

        // First row
        assertThat(
                table.get(0),
                is(array(
                        equalTo("TEST METHOD"),
                        equalTo("STATUS"),
                        equalTo("REQUIREMENT")
                ))
        );

        //Other rows
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
                .build();
        ) {
            return csvReader.readAll();
        }
        catch(Exception e) {
            throw new RuntimeException(
                    "TestRequirementProcessor error: Can't read this file: " + filePath);
        }
    }
}
