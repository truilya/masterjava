package ru.javaops.masterjava.service.mail.model;

public enum MailResults {
    SUCCESS("success"),
    ERROR("error");

    private final String value;

    MailResults(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
