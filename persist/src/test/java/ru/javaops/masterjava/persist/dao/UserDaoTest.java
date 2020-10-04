package ru.javaops.masterjava.persist.dao;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import ru.javaops.masterjava.persist.UserTestData;
import ru.javaops.masterjava.persist.model.User;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static ru.javaops.masterjava.persist.UserTestData.FIST5_USERS;
import static ru.javaops.masterjava.persist.UserTestData.USER1;

public class UserDaoTest extends AbstractDaoTest<UserDao> {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public UserDaoTest() {
        super(UserDao.class);
    }

    @BeforeClass
    public static void init() throws Exception {
        UserTestData.init();
    }

    @Before
    public void setUp() throws Exception {
        UserTestData.setUp();
    }

    @Test
    public void getWithLimit() {
        List<User> users = dao.getWithLimit(5);
        Assert.assertEquals(FIST5_USERS, users);
    }

    @Test
    public void insertBatch() throws Exception {
        dao.clean();
        dao.insertBatch(FIST5_USERS, 3);
        Assert.assertEquals(5, dao.getWithLimit(100).size());
    }

    @Test
    public void cityReferenceException(){
        thrown.expect(UnableToExecuteStatementException.class);
        thrown.expectMessage(containsString("fk_city"));
        USER1.setCityId(-1);
        dao.insert(USER1);

    }

}