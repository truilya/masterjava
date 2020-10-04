package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.Project;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class ProjectDao extends AbstractDao implements CommonDao {

    public Project insert(Project project){
        if (project.isNew()){
            int id = insertGeneratedId(project);
            project.setId(id);
        } else {
            insertWithId(project);
        }
        return project;
    }

    @Override
    @SqlUpdate("truncate projects cascade")
    public abstract void clean();

    @SqlUpdate("insert into projects(id, name, description) values (:id, :name, :description) on conflict (name) do update set description = :description")
    public abstract void insertWithId(@BindBean Project project);

    @SqlUpdate("insert into projects(name, description) values (:name, :description) on conflict (name) do update set description = :description")
    public abstract int insertGeneratedId(@BindBean Project project);

    @SqlQuery("select * from projects where name = :name")
    public abstract Project getByName(@Bind("name") String name);

    @SqlQuery("select * from projects order by name")
    public abstract List<Project> getAll();

}
