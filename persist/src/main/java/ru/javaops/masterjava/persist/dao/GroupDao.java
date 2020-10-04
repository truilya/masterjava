package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.Group;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class GroupDao extends AbstractDao implements CommonDao {

    public Group insert(Group group){
        if (group.isNew()){
            int id = insertGeneratedId(group);
            group.setId(id);
        } else {
            insertWithId(group);
        }
        return group;
    }

    @Override
    @SqlUpdate("truncate groups cascade")
    public abstract void clean();

    @SqlUpdate("insert into groups(id, name, type, id_project) values (:id, :name, cast(:type as group_type), :idProject) on conflict (name) do update set type = cast(:type as group_type), id_project = :idProject")
    public abstract void insertWithId(@BindBean Group group);

    @SqlUpdate("insert into groups(name, type, id_project) values (:name, cast(:type as group_type), :idProject) on conflict (name) do update set type = cast(:type as group_type), id_project = :idProject")
    public abstract int insertGeneratedId(@BindBean Group group);

    @SqlBatch("insert into groups(name, type, id_project) values (:name, cast(:type as group_type), :idProject) on conflict (name) do update set type = cast(:type as group_type), id_project = :idProject")
    public abstract void insertBatch(@BindBean List<Group> groups, @BatchChunkSize int chunkSize);

    @SqlQuery("select * from groups order by name")
    public abstract List<Group> getAll();

    @SqlQuery("select * from groups where name = :name")
    abstract Group getByName(@Bind("name") String name);
}
