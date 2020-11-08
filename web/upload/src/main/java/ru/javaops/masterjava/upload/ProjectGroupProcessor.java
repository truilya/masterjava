package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.persist.model.type.GroupType;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProjectGroupProcessor {
    private final ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);
    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);

    public Map<String, Group> process(StaxStreamProcessor processor) throws XMLStreamException, JAXBException {
        val mapProjects = projectDao.getAsMap();
        val mapGroups = groupDao.getAsMap();
        val unmarshaller = jaxbParser.createUnmarshaller();
        while (processor.startElement("Project", "Projects")) {
            val name = processor.getAttribute("name");
            int projectId;
            if (!mapProjects.containsKey(name)) {
                Project newProject = new Project(name, processor.getElementValue("description"));
                projectDao.insert(newProject);
                projectId = newProject.getId();
            } else {
                projectId = mapProjects.get(name).getId();
            }
            while (processor.startElement("Group", "Project")) {
                ru.javaops.masterjava.xml.schema.Project.Group xmlGroup = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.Project.Group.class);
                if (!mapGroups.containsKey(xmlGroup.getName())) {
                    Group newGroup = new Group(xmlGroup.getName(), GroupType.valueOf(xmlGroup.getType().value()), projectId);
                    groupDao.insert(newGroup);
                }
            }
        }
        return groupDao.getAsMap();
    }

}
