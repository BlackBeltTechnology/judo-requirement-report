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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;

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
        // Create Reference Array
        List<String[]> refTable = createRefTable();

        // Read generated csv
        List<String[]> table = readCsv(System.getProperty("reportPath"));

        // Check the lists
        assertArrayEquals(refTable.toArray(), table.toArray());
    }

    private List<String[]> createRefTable() {
        List<String[]> refTable = new LinkedList<>();
        refTable.add(
                new String[]{
                        "TEST METHOD",
                        "STATUS",
                        "REQUIREMENT"
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test05",
                        "Missing annotation: @Test.",
                        "R03"
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test03",
                        "OK",
                        "R03"
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test03",
                        "OK",
                        "R01"
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test00",
                        "There isn't any requirement id.",
                        ""
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test04",
                        "OK",
                        "R04"
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test02",
                        "OK",
                        "R02"
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test01",
                        "OK",
                        "R01"
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test04",
                        "OK",
                        "R05"
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test05",
                        "Missing annotation: @Test.",
                        "R01"
                }
        );
        refTable.add(
                new String[]{
                        "TestRequirementProcessor.test04",
                        "OK",
                        "R01"
                }
        );
//        refTable.add(
//                new String[]{
//                        "",
//                        "",
//                        ""
//                }
//        );

        return refTable
                .stream()
                .sorted(new RowComparator())
                .collect(Collectors.toList());
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
            return csvReader.readAll()
                    .stream()
                    .sorted(new RowComparator())
                    .collect(Collectors.toList());
        }
        catch(Exception e) {
            throw new RuntimeException(
                    "TestRequirementProcessor error: Can't read this file: " + filePath);
        }
    }

    private class RowComparator implements Comparator<String[]> {
        // Order rows of csv by TEST METHOD, REQUIREMENT
        public int compare(String[] a, String[] b)
        {
            int result = a[0].compareTo(b[0]);
            if(result == 0)
                result = a[2].compareTo(b[2]);

            return result;
        }
    }
}
