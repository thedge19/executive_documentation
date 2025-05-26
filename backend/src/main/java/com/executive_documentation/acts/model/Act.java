package com.executive_documentation.acts.model;

import com.executive_documentation.projects.model.Project;
import com.executive_documentation.subobjects.model.SubObject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "acts")
@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@ToString
public class Act {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "act_id", columnDefinition = "integer")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "act_project_id")
    @NonNull
    private Project project;

    @ManyToOne
    @JoinColumn(name = "act_subobject_id")
    @NonNull
    private SubObject subObject;

    @Column(name = "act_works")
    @NotEmpty
    private String works;

    @Column(name = "act_number")
    @NotEmpty
    private String actNumber;

    @Column(name = "start_date")
    @NotNull
    private LocalDate startDate;

    @Column(name = "end_date")
    @NotNull
    private LocalDate endDate;

    @Column(name="act_materials")
    private String materials;

    @Column(name = "submitted_documents")
    private String submittedDocuments;

    @NotNull
    @Column(name = "in_accord_with")
    private String inAccordWith;

    @Column(name = "act_next_works")
    private String nextWorks;

    @Column(name = "act_work_done")
    @NotNull
    private BigDecimal workDone;

    @Column(name = "in_registry")
    private String inRegistry;

    @OneToOne
    @JoinColumn(name="executive_schema_id")
    private ExecutiveSchema executiveSchema;
}
