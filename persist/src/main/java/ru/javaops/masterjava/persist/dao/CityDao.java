package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class CityDao extends AbstractDao implements CommonDao {

    public City insert(City city){
        if (city.isNew()){
            int id = insertGeneratedId(city);
            city.setId(id);
        } else {
            insertWithId(city);
        }
        return city;
    }

    @SqlUpdate("truncate city cascade")
    @Transaction
    public abstract void clean();

    @SqlUpdate("insert into city(name, code) values (:name, :code) on conflict (code) do nothing")
    @GetGeneratedKeys
    public abstract int insertGeneratedId(@BindBean City city);

    @SqlUpdate("insert into city(id, name, code) values (:id, :name, :code) on conflict (code) do nothing")
    public abstract void insertWithId(@BindBean City city);

    @SqlBatch("insert into city(name, code) values (:name, :code)" +
            "on conflict (name) do nothing")
    public abstract int[] insertBatch(@BindBean List<City> cities, @BatchChunkSize int chunkSize);

    @SqlQuery("select * from city order by name")
    public abstract List<City> getAll();
}
