package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

public class CityTestData {

    public static City MSK;
    public static City SPB;
    public static City MNSK;
    public static City KIV;
    public static List<City> CITIES;

    public static void init(){
        MSK = new City("Москва","msk");
        SPB = new City("Санкт-Петербург","spb");
        MNSK = new City("Минск","mnsk");
        KIV = new City("Киев","kiv");
        CITIES = ImmutableList.of(KIV, MNSK, MSK, SPB);
    }

    public static void setUp(){
        CityDao dao = DBIProvider.getDao(CityDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction(((conn, status) -> {
            CITIES.forEach(dao::insert);
        }));
    }
}
