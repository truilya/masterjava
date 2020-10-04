package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.model.Group;

import java.util.List;

import static ru.javaops.masterjava.persist.ProjectTestData.MASTERJAVA;
import static ru.javaops.masterjava.persist.ProjectTestData.TOPJAVA;
import static ru.javaops.masterjava.persist.model.GroupType.current;
import static ru.javaops.masterjava.persist.model.GroupType.finished;

public class GroupTestData {
    public static Group TOPJAVA6;
    public static Group TOPJAVA7;
    public static Group TOPJAVA8;
    public static Group MASTERJAVA1;
    public static List<Group> GROUPS;

    public static void init() {
        ProjectTestData.init();
        TOPJAVA6 = new Group("topjava06", finished, TOPJAVA.getId());
        TOPJAVA7 = new Group("topjava07", finished, TOPJAVA.getId());
        TOPJAVA8 = new Group("topjava08", current, TOPJAVA.getId());
        MASTERJAVA1 = new Group("masterjava01", current, MASTERJAVA.getId());
        GROUPS = ImmutableList.of(TOPJAVA6, TOPJAVA7, TOPJAVA8, MASTERJAVA1);
    }

    public static void setUp() {
        GroupDao dao = DBIProvider.getDao(GroupDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction((((conn, status) -> {
            GROUPS.forEach(dao::insert);
        })));
    }

}
