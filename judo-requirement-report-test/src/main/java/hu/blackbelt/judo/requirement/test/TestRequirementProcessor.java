package hu.blackbelt.judo.requirement.test;

import hu.blackbelt.judo.requirement.report.annotation.Requirement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestRequirementProcessor {

    @Requirement(reqs = {

    })
    @Test
    public void test00(){

    }

    @Requirement(reqs = {
            "R01"
    })
    @Test
    public void test01(){

    }

    @Requirement(reqs = {
            "R02"
    })
    @Test
    public void test02(){

    }

    @Requirement(reqs = {
            "R01",
            "R03"
    })
    @Test
    public void test03(){

    }

    @Requirement(reqs = {
            "R04",
            "R05"
    })
    @Test
    public void test04(){

    }

    @Requirement(reqs = {
            "R01",
            "R03"
    })
    public void test05(){

    }

    @Test
    public void testReal(){

    }
}
