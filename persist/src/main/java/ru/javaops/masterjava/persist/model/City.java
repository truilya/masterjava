package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class City extends NamedEntity {
    @Column("code")
    @NonNull
    private String code;

    public City(Integer id, String name, String code){
        this(code);
        this.id = id;
        this.name = name;
    }

    public City(String name, String code) {
        this(code);
        this.name = name;
    }

}
