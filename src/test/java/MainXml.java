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
        Map<String,String> users = getUsersByProjectJAXB("masterjava");
        System.out.println("for project masterjava");
        for (Map.Entry s : users.entrySet()) {
            System.out.println(s.getKey() + " - " +s.getValue());
        }
        users = getUsersByProjectJAXB("topjava");
        System.out.println("for project topjava");
        for (Map.Entry s : users.entrySet()) {
            System.out.println(s.getKey() + " - " +s.getValue());
        }
        System.out.println("\n==== by Stax ====");
        users = getUsersByProjectSTAX("masterjava");
        for (Map.Entry s : users.entrySet()) {
            System.out.println(s.getKey() + " - " +s.getValue());
        }
        printHtmlFromMap(users);
    }

    private static Map<String,String> getUsersByProjectJAXB(String projectName) throws Exception {
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());
        List<Project> projects = payload.getProjects().getProject();
        Map<String,String> result = new HashMap<>();
        for (Project p : projects) {
            if (projectName.equalsIgnoreCase(p.getName())) {
                List<Group> groups = p.getGroups().getGroup();
                for (Group g : groups) {
                    List<Object> users = g.getUsers();
                    for (Object o : users) {
                        User u = (User) o;
                        result.putIfAbsent(u.getFullName(),u.getEmail());
                    }
                }
                return result;
            }
        }
        return result;
    }

    private static Map<String,String> getUsersByProjectSTAX(final String projectName) throws Exception {
        Set<String> projectUsers = new TreeSet<>();
        Map<String,List<String>> users = new HashMap<>();
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
                    attribute =  processor.getAttributeByName(event.asStartElement(),"email");
                    String sEmail = attribute.getValue();
                    String fullName = processor.getElementValuesInsideParent(USER_ELEMENT, "fullName").get(0);
                    users.put(sLogin,Arrays.asList(fullName,sEmail));
                }
            }
        }
        Map<String, String> result = new HashMap<>();
        for (String s :projectUsers){
            result.putIfAbsent(users.get(s).get(0),users.get(s).get(1));
        }
        return result;
    }

    private static void printHtmlFromMap(Map<String,String> map){
        StringBuilder builder = new StringBuilder();
        builder.append("<table>")
               .append("<thead><tr>")
                .append("<td>Full Name</td>")
                .append("<td>Email</td>")
                .append("</tr></thead>");
        for(Map.Entry e : map.entrySet()){
            builder.append("<tr>")
                    .append("<td>")
                    .append(e.getKey())
                    .append("</td>")
                    .append("<td>")
                    .append(e.getValue())
                    .append("</td>")
                    .append("</tr>");
        }
        builder.append("</table>");
        System.out.println(builder.toString());
    }

}
