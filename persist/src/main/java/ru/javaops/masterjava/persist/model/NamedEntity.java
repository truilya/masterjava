package ru.javaops.masterjava.persist.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
abstract public class NamedEntity extends BaseEntity {

    @Getter
    @Setter
    protected String name;

}
