package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.CityTestData;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

import static ru.javaops.masterjava.persist.CityTestData.*;

public class CityDaoTest extends AbstractDaoTest<CityDao> {

    public CityDaoTest() {
        super(CityDao.class);
    }

    @BeforeClass
    public static void init(){
        CityTestData.init();
    }

    @Before
    public void setUp(){
        CityTestData.setUp();
    }

    @Test
    public void getAll(){
        List<City> cities = dao.getAll();
        Assert.assertEquals(CITIES, cities);
    }

    @Test
    public void insertBatch(){
        dao.clean();
        dao.insertBatch(CITIES,4);
        List<City> cities = dao.getAll();
        Assert.assertEquals(CITIES.size(), cities.size());
    }
}
