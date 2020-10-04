package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Project;

import java.util.List;

public class ProjectTestData {
    public static Project TOPJAVA;
    public static Project MASTERJAVA;
    public static List<Project> PROJECTS;

    public static void init() {
        TOPJAVA = new Project("topjava","Topjava");
        MASTERJAVA = new Project("masterjava","Masterjava");
        PROJECTS = ImmutableList.of(MASTERJAVA, TOPJAVA);
    }

    public static void setUp(){
        ProjectDao dao = DBIProvider.getDao(ProjectDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {PROJECTS.forEach(dao::insert);});
    }

}
