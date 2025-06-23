package com.executive_documentation.subobjects.model;

import com.executive_documentation.projects.model.Project;
import com.executive_documentation.workings.model.Working;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Table(name = "subobjects")
@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class SubObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subobject_id")
    private Long id;

    @Column(name = "subobject_name")
    @NotEmpty
    private String name;

    @Column(name = "subobject_title")
    @NotEmpty
    private String title;

    @JoinColumn(name = "project_id")
    @NotNull
    @ManyToOne
    private Project project;

    @OneToMany(mappedBy = "subObject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Working> workings;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubObject subobject = (SubObject) o;
        return Objects.equals(id, subobject.id) && Objects.equals(name, subobject.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
