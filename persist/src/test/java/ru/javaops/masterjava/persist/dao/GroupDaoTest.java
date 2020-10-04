package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.GroupTestData;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupType;

import java.util.List;

import static ru.javaops.masterjava.persist.GroupTestData.GROUPS;

public class GroupDaoTest extends AbstractDaoTest<GroupDao> {

    public GroupDaoTest(){
        super(GroupDao.class);
    }

    @BeforeClass
    public static void init(){
        GroupTestData.init();
    }

    @Before
    public void setUp(){
        GroupTestData.setUp();
    }

    @Test
    public void getAll(){
        List<Group> groups = dao.getAll();
        Assert.assertEquals(GROUPS, groups);
    }

    @Test
    public void updateType(){
        Group masterjava01 = dao.getByName("masterjava01");
        masterjava01.setType(GroupType.finished);
        dao.insertWithId(masterjava01);
        Group masterjava01Finished = dao.getByName("masterjava01");
        Assert.assertEquals(masterjava01, masterjava01Finished);
    }

}
