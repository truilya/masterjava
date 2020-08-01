package ru.javaops.masterjava.xml.util;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StaxStreamProcessor implements AutoCloseable {
    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    private final XMLStreamReader reader;
    private XMLEventReader eventReader;

    public StaxStreamProcessor(InputStream is) throws XMLStreamException {
        reader = FACTORY.createXMLStreamReader(is);
        eventReader = FACTORY.createXMLEventReader(reader);
    }

    public XMLStreamReader getReader() {
        return reader;
    }

    public XMLEventReader getEventReader() {
        return eventReader;
    }

    public boolean doUntil(int stopEvent, String value) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == stopEvent) {
                if (value.equals(getValue(event))) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getValue(int event) throws XMLStreamException {
        return (event == XMLEvent.CHARACTERS) ? reader.getText() : reader.getLocalName();
    }

    public String getElementValue(String element) throws XMLStreamException {
        return doUntil(XMLEvent.START_ELEMENT, element) ? reader.getLocalName() : null;
    }

    public String getText() throws XMLStreamException {
        return reader.getElementText();
    }

    public boolean startElement(String element, String parent) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (parent != null && event == XMLEvent.END_ELEMENT &&
                    parent.equals(reader.getLocalName())) {
                return false;
            }
            if (event == XMLEvent.START_ELEMENT &&
                    element.equals(reader.getLocalName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                // empty
            }
        }
        if (eventReader != null) {
            try {
                eventReader.close();
            } catch (XMLStreamException e) {
                // empty
            }
        }
    }

    public boolean checkElementByNameAndAttrValue(XMLEvent event, String name, String attrName, final String value) throws Exception {
        if (event.isStartElement() && name.equals(event.asStartElement().getName().getLocalPart())) {
            Attribute attribute = getAttributeByName(event.asStartElement(), attrName);
            if (attribute!= null && value.equals(attribute.getValue())){
                return true;
            }
            return false;
        }
        return false;
    }

    public Attribute getAttributeByName(StartElement startElement, String attrName){
        Iterator<Attribute> iterator = startElement.getAttributes();
        while (iterator.hasNext()){
                Attribute attribute = iterator.next();
                if (attrName.equals(attribute.getName().getLocalPart())){
                    return attribute;
                }
            }
        return null;
    }

    public List<String> getElementValuesInsideParent(String parent,String elem) throws XMLStreamException{
        XMLEvent event = eventReader.nextEvent();
        List<String> result = new ArrayList<>();
        while (eventReader.hasNext() && !(event.isEndElement() && parent.equals(event.asEndElement().getName().getLocalPart()))){
            if (event.isStartElement() && elem.equals(event.asStartElement().getName().getLocalPart())){
                event = eventReader.nextEvent();
                if (event.isCharacters()){
                    result.add(event.asCharacters().getData());
                }
            }
            event = eventReader.nextEvent();
        }
        return result;
    }

}
