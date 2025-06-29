package com.executive_documentation.materials.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Table(name = "certificates")
@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Long id;

    @NotBlank
    @Column(name="certificate_path")
    private String path;

    @Column(name="number_of_pages")
    private Integer numberOfPages;

    @Column(name = "certificate_name")
    private String name;

    @Column(name = "certificate_author")
    private String author;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;
}
