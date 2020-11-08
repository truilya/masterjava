package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserGroup;
import ru.javaops.masterjava.persist.model.type.UserFlag;
import ru.javaops.masterjava.upload.PayloadProcessor.FailedEmails;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class UserProcessor {
    private static final int NUMBER_THREADS = 4;

    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private static UserDao userDao = DBIProvider.getDao(UserDao.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

    /*
     * return failed users chunks
     */
    public List<FailedEmails> process(final StaxStreamProcessor processor, Map<String, City> cities, Map<String, Group> groups, int chunkSize) throws XMLStreamException, JAXBException {
        log.info("Start processing with chunkSize=" + chunkSize);

        Map<String, Future<List<String>>> chunkFutures = new LinkedHashMap<>();  // ordered map (emailRange -> chunk future)

        //int id = userDao.getSeqAndSkip(chunkSize);
        List<User> chunk = new ArrayList<>(chunkSize);
        Map<String, List<Group>> chunkUserGroup = new HashMap<>();
        val unmarshaller = jaxbParser.createUnmarshaller();
        List<FailedEmails> failed = new ArrayList<>();

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            String cityRef = processor.getAttribute("city");  // unmarshal doesn't get city ref
            String groupRefs = processor.getAttribute("groupRefs");
            List<String> userGroups = new ArrayList<>();

            if (groupRefs != null) {
                String[] arrGroupRefs = groupRefs.split(" ");
                userGroups = Arrays.asList(arrGroupRefs);
            }
            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);
            if (cities.get(cityRef) == null) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "City '" + cityRef + "' is not present in DB"));
            } else if (!checkExistsGroup(userGroups, groups)) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "Groups is not present in DB"));
            }
            else {
                final User user = new User(xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()), cityRef);
                chunk.add(user);
                List<Group> groupsForChunk = new ArrayList<>();
                for(String s : userGroups){
                    groupsForChunk.add(groups.get(s));
                }
                chunkUserGroup.put(user.getEmail(),groupsForChunk);
                if (chunk.size() == chunkSize) {
                    addChunkFutures(chunkFutures, chunk, chunkUserGroup);
                    chunk = new ArrayList<>(chunkSize);
                    chunkUserGroup = new HashMap<>();
                    //id = userDao.getSeqAndSkip(chunkSize);
                }
            }
        }

        if (!chunk.isEmpty()) {
            addChunkFutures(chunkFutures, chunk, chunkUserGroup);
        }

        List<String> allAlreadyPresents = new ArrayList<>();
        chunkFutures.forEach((emailRange, future) -> {
            try {
                List<String> alreadyPresentsInChunk = future.get();
                log.info("{} successfully executed with already presents: {}", emailRange, alreadyPresentsInChunk);
                allAlreadyPresents.addAll(alreadyPresentsInChunk);
            } catch (InterruptedException | ExecutionException e) {
                log.error(emailRange + " failed", e);
                failed.add(new FailedEmails(emailRange, e.toString()));
            }
        });
        if (!allAlreadyPresents.isEmpty()) {
            failed.add(new FailedEmails(allAlreadyPresents.toString(), "already presents"));
        }
        return failed;
    }

    private boolean checkExistsGroup(List<String> userGroups, Map<String, Group> allGroups) {
        if (userGroups.size() == 0) {
            return true;
        }
        for (String ug : userGroups) {
            if (!allGroups.containsKey(ug)) {
                return false;
            }
        }
        return true;
    }

    private void addChunkFutures(Map<String, Future<List<String>>> chunkFutures, List<User> chunk, Map<String,List<Group>> userGroups) {
        String emailRange = String.format("[%s-%s]", chunk.get(0).getEmail(), chunk.get(chunk.size() - 1).getEmail());
        Future<List<String>> future = executorService.submit(() -> userDao.insertAndGetConflictEmails(chunk, userGroups));
        chunkFutures.put(emailRange, future);
        log.info("Submit chunk: " + emailRange);
    }
}
