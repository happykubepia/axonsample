package org.axon.dto;

public enum StatusEnum {
    READY("Ready"),
    ENTER("Enter"),
    EXIT("Exit");

    private String value;
    StatusEnum(String value) { this.value = value; }
    public String value() { return this.value; }
}
