package ru.javaops.masterjava.xml;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class XmlEndElementException extends Exception {

    public XmlEndElementException(String message) {
        super(message);
    }
}
