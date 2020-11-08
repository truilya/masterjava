package ru.javaops.masterjava.service.mail.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.service.mail.model.MailEntity;

import java.time.LocalDateTime;
import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class MailDao implements AbstractDao {

    @SqlUpdate("insert into email_send_result (result, error_cause) values (:result, :cause)")
    public abstract void insert(@BindBean MailEntity mailEntity);

    @SqlQuery("select * from email_send_result order by time_send desc")
    public abstract List<MailEntity> getAll();

    @SqlQuery("select * from email_send_result where time_send " +
            "between coalesce(:startDate,to_date('01011900','ddmmyyyy')) " +
            "and coalesce(:endDate,to_date(31124000,'ddmmyyyy')) order by time_send desc")
    public abstract List<MailEntity> getBetween(@Bind LocalDateTime startDate, @Bind LocalDateTime endDate);
}
