package ru.javaops.masterjava.persist.model;

import lombok.*;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Project extends NamedEntity {
    @NonNull
    private String description;

    public Project(Integer id, String name, String description){
        this(name, description);
        this.id = id;
    }

    public Project(String name, String description){
        this(description);
        this.name = name;
    }

}
