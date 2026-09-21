package org.edwin.bekal.enums;

public enum LoanStatus {
    IN_REVIEW("in_review"),
    REVIEW_REJECTED("review_rejected"),
    IN_APPROVAL("in_approval"),
    APPROVAL_REJECTED("approval_rejected"),
    IN_DISBURSEMENT("in_disbursement"),
    DISBURSED("disbursed"),
    CLOSED("closed"),
    CANCELLED("cancelled");

    private final String value;

    LoanStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
