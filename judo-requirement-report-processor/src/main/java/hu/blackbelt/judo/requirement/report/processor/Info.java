package hu.blackbelt.judo.requirement.report.processor;

class Info {
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
