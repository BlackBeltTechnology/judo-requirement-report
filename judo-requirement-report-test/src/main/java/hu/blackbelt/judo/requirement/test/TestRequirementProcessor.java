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

import hu.blackbelt.judo.requirement.report.annotation.Requirement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
            "R04",
            "R05"
    })
    @Test
    public void test04(){
        assertTrue(true);
    }

    @Requirement(reqs = {
            "R01",
            "R03"
    })
    public void test05(){
        assertTrue(true);
    }

    @Test
    public void testReal(){
        assertTrue(false);
    }
}
