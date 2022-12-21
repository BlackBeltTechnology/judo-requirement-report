package hu.blackbelt.judo.requirement.test;

import hu.blackbelt.judo.requirement.report.annotation.Requirement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestRequirementProcessor {

    @Requirement(reqs = {
            "aaa",
            "bbb"
    })
    @Test
    public void test01(){

    }

    @Requirement(reqs = {
            "aaa"
    })
    @Test
    public void test02(){

    }

    @Test
    public void testReal(){

    }
}
