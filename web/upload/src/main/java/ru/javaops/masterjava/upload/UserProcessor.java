package ru.javaops.masterjava.upload;

import javafx.util.Pair;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.JaxbUnmarshaller;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class UserProcessor {
    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private static final String OK = "OK";
    private static final String INTERRUPTED_BY_TIMEOUT = "+++ Interrupted by timeout";
    private static final String INTERRUPTED_EXCEPTION = "+++ InterruptedException";

    private final ExecutorService userExecutor = Executors.newFixedThreadPool(8);

    public List<User> process(final InputStream is) throws XMLStreamException, JAXBException {
        final StaxStreamProcessor processor = new StaxStreamProcessor(is);
        List<User> users = new ArrayList<>();

        JaxbUnmarshaller unmarshaller = jaxbParser.createUnmarshaller();
        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);
            final User user = new User(xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()));
            users.add(user);
        }
        return users;
    }

    public GroupResult process(final InputStream is, final int chunkSize) throws XMLStreamException, JAXBException,Exception {
        final StaxStreamProcessor processor = new StaxStreamProcessor(is);
        List<User> users = new ArrayList<>();
        List<User> chunkUsers = new ArrayList<>();
        CompletionService<ChunkResult> completionService = new ExecutorCompletionService<>(userExecutor);
        List<Future<ChunkResult>> futures = new ArrayList<>();
        JaxbUnmarshaller unmarshaller = jaxbParser.createUnmarshaller();
        int counter = 0;
        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);
            final User user = new User(xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()));
            chunkUsers.add(user);
            counter++;
            if (counter==chunkSize){
                final List<User> chunk = new ArrayList<>(chunkUsers);
                futures.add(completionService.submit(()->writeUsersInDB(chunk)));
                chunkUsers.clear();
                counter = 0;
            }
        }
        if (chunkUsers.size()>0){
            futures.add(completionService.submit(()->writeUsersInDB(chunkUsers)));
        }
        return new Callable<GroupResult>(){
            private int success = 0;
            private List<String> existsEmails = new ArrayList<>();
            private List<ChunkResult> chunkResults = new ArrayList<>();

            @Override
            public GroupResult call() throws Exception {
                while (!futures.isEmpty()){
                    try {
                        Future<ChunkResult> future = completionService.poll(1,TimeUnit.SECONDS);
                        if (future == null){
                            return cancelWithFail(INTERRUPTED_BY_TIMEOUT);
                        }
                        futures.remove(future);
                        ChunkResult chunkResult = future.get();
                        chunkResults.add(chunkResult);
                        if (chunkResult.isOk()){
                            existsEmails.addAll(chunkResult.getExistsEmails());
                        }
                    } catch (ExecutionException e){
                        return cancelWithFail(e.getCause().toString());
                    } catch (InterruptedException e){
                        return cancelWithFail(INTERRUPTED_EXCEPTION);
                    }
                }
                return new GroupResult(success,existsEmails,chunkResults,null);
            }

            public GroupResult cancelWithFail(String cause){
                futures.forEach(f -> f.cancel(true));
                return new GroupResult(-1,null,null,cause);
            }
        }.call();
    }

    private ChunkResult writeUsersInDB(List<User> users) {
        Pair<String, String> interval = new Pair<>(users.get(0).getEmail(), users.get(users.size() - 1).getEmail());
        List<String> existsEmails = new ArrayList<>();
        String cause = OK;
        try {
            DBI dbi = DBIProvider.getDBI();
            Handle handle = dbi.open();
            UserDao userDao = handle.attach(UserDao.class);
            int[] rows = userDao.insertAll(users, users.size());
            for (int i = 0; i < rows.length; i++) {
                if (rows[i] == 0) {
                    existsEmails.add(users.get(i).getEmail());
                }
            }
        } catch (Exception e) {
            cause = e.getCause().toString();
        }
        return new ChunkResult(existsEmails, interval, cause);
    }

    public static class ChunkResult{
        private final String result;
        private final List<String> existsEmails;
        private final Pair<String,String> interval;

        public ChunkResult(List<String> existsEmails, Pair<String, String> interval, String result) {
            this.existsEmails = existsEmails;
            this.interval = interval;
            this.result = result;
        }

        public List<String> getExistsEmails() {
            return existsEmails;
        }

        public boolean isOk(){
            return OK.equals(result);
        }
    }

    public static class GroupResult{

        private final int success;
        private final List<String> existsEmails;
        private final List<ChunkResult> chunkResults;
        private final String cause;

        public GroupResult(int success, List<String> existsEmails, List<ChunkResult> chunkResults, String cause) {
            this.success = success;
            this.existsEmails = existsEmails;
            this.chunkResults = chunkResults;
            this.cause = cause;
        }
    }
}
