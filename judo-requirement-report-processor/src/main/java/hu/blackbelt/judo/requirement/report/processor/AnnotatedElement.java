package hu.blackbelt.judo.requirement.report.processor;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import org.junit.jupiter.api.Test;
import hu.blackbelt.judo.requirement.report.annotation.Requirement;
import hu.blackbelt.judo.requirement.report.annotation.TestCase;

/**
 * This class contains all important meta info of an annotated element.
 *
 */
class AnnotatedElement implements Comparable<AnnotatedElement>  {
    private String elementName;
    private boolean isTestCaseAnnotation;
    private String testCaseId;
    private boolean isTestAnnotation;
    private Requirement reqAnnotation;
    private String resultStringForRequirementReport;
    private String resultStringForTestCaseReport;
    
    public AnnotatedElement(Element element) {
        TestCase testCaseAnnotation = element.getAnnotation(TestCase.class);
        this.elementName = element.getEnclosingElement().getSimpleName() + "." + element.getSimpleName();
        this.reqAnnotation = element.getAnnotation(Requirement.class);
        this.isTestAnnotation = (element.getAnnotation(Test.class) != null) ? true : false;
        this.isTestCaseAnnotation = (testCaseAnnotation != null) ? true : false;
        
        if (this.isTestCaseAnnotation) {
            if (testCaseAnnotation.value() != null) {
                this.testCaseId = testCaseAnnotation.value();
                this.resultStringForTestCaseReport = (this.testCaseId.equals("")) ? "Empty string isn't a valid value of a @TestCase annotation." : "OK";
            } else {
                this.testCaseId = "";
                this.resultStringForTestCaseReport = "No value of @TestCase annotation.";
            }
        } else {
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
    
    public String getTestCaseId() {
        return testCaseId;
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
                : (this.resultStringForTestCaseReport + " " + resultStringForTestCaseReport);
    }
    
    public Collection<RequirementReportRow> collectReqForElement() {
        return Arrays.stream(
                    (this.reqAnnotation.reqs() == null || this.reqAnnotation.reqs().length == 0) ?
                            new String[]{""}
                            : this.reqAnnotation.reqs()
                )
                .map(a -> new RequirementReportRow(this.elementName, this.testCaseId, this.resultStringForRequirementReport, a))
                .collect(Collectors.toSet());
    }

    String[] toTestCaseRowStringArray() {
        return new String[]{
                this.testCaseId,
                this.elementName,
                this.resultStringForTestCaseReport
                };
    }
}
