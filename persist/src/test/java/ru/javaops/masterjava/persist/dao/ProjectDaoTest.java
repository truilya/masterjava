package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.ProjectTestData;
import ru.javaops.masterjava.persist.model.Project;

import java.util.List;

import static ru.javaops.masterjava.persist.ProjectTestData.MASTERJAVA;
import static ru.javaops.masterjava.persist.ProjectTestData.PROJECTS;

public class ProjectDaoTest extends AbstractDaoTest<ProjectDao> {

    public ProjectDaoTest(){super(ProjectDao.class);}

    @BeforeClass
    public static void init(){
        ProjectTestData.init();
    }

    @Before
    public void setUp(){
        ProjectTestData.setUp();
    }

    @Test
    public void getAll(){
        List<Project> projects = dao.getAll();
        Assert.assertEquals(PROJECTS, projects);
    }

    @Test
    public void updateNameAndDescription(){
        Project masterjava = dao.getByName("masterjava");
        masterjava.setDescription("Monsterjava");
        dao.insertWithId(masterjava);
        Project monsterjava = dao.getByName("masterjava");
        Assert.assertEquals(masterjava, monsterjava);
    }

}
