import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

public class MainXml {

    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);
    private static final String PROJECT_ELEMENT = "Project";
    private static final String PROJECT_USERS = "Users";
    private static final String USER_ELEMENT = "User";


    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("==== by JAXB ====");
        Set<String> users = getUsersByProjectJAXB("masterjava");
        System.out.println("for project masterjava");
        for (String s : users) {
            System.out.println(s);
        }
        users = getUsersByProjectJAXB("topjava");
        System.out.println("for project topjava");
        for (String s : users) {
            System.out.println(s);
        }
        System.out.println("\n==== by Stax ====");
        users = getUsersByProjectSTAX("masterjava");
        for (String s : users) {
            System.out.println(s);
        }
    }

    private static Set<String> getUsersByProjectJAXB(String projectName) throws Exception {
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());
        List<Project> projects = payload.getProjects().getProject();
        Set<String> result = new TreeSet<>();
        for (Project p : projects) {
            if (projectName.equalsIgnoreCase(p.getName())) {
                List<Group> groups = p.getGroups().getGroup();
                for (Group g : groups) {
                    List<Object> users = g.getUsers();
                    for (Object o : users) {
                        User u = (User) o;
                        result.add(u.getFullName());
                    }
                }
                return result;
            }
        }
        return result;
    }

    private static Set<String> getUsersByProjectSTAX(final String projectName) throws Exception {
        Set<String> result = new TreeSet<>();
        Set<String> projectUsers = new TreeSet<>();
        Map<String,String> users = new HashMap<>();
        try (StaxStreamProcessor processor = new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            XMLEventReader eventReader = processor.getEventReader();
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (processor.checkElementByNameAndAttrValue(event, PROJECT_ELEMENT, "name", projectName)) {
                    List<String> localUsers = processor.getElementValuesInsideParent(PROJECT_ELEMENT, PROJECT_USERS);
                    for(String s : localUsers){
                        projectUsers.addAll(Arrays.asList(s.split(" ")));
                    }
                } else if (event.isStartElement() && USER_ELEMENT.equals(event.asStartElement().getName().getLocalPart())){
                    Attribute attribute =  processor.getAttributeByName(event.asStartElement(),"login");
                    String sLogin = attribute.getValue();
                    String fullName = processor.getElementValuesInsideParent(USER_ELEMENT, "fullName").get(0);
                    users.put(sLogin,fullName);
                }
            }
        }
        for (String s :projectUsers){
            result.add(users.get(s));
        }
        return result;
    }

}
