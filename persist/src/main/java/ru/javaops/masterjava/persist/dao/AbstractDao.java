package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class AbstractDao {

    @SqlQuery("SELECT nextval('all_seq')")
    @Transaction
    abstract int getNextVal();


}
