import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import java.util.*;
import java.util.stream.Collectors;

public class MainXml {

    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

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
    }

    public static Set<String> getUsersByProjectJAXB(String projectName) throws Exception {
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

}
