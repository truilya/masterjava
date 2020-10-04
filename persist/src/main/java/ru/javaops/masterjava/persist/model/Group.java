package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

import java.util.List;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Group extends NamedEntity {
    @Column("type")
    @NonNull
    private GroupType type;

    @Column("id_project")
    private Integer idProject;

    public Group(String name, GroupType type, Integer idProject){
        this(type);
        this.idProject = idProject;
        this.name = name;
    }

    public Group(Integer id, String name, GroupType type, Integer idProject){
        this(type);
        this.idProject = idProject;
        this.id = id;
        this.name = name;
    }
}
