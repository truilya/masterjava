package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

import java.util.List;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class User extends BaseEntity {
    @Column("full_name")
    @NonNull
    private String fullName;
    @NonNull
    private String email;
    @NonNull
    private UserFlag flag;
    private Integer cityId;

    private List<Group> groups;

    public User(Integer id, String fullName, String email, UserFlag flag, Integer cityId) {
        this(fullName, email, flag);
        this.id=id;
        this.cityId = cityId;
    }
}