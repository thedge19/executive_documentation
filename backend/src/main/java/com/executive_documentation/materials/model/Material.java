package com.executive_documentation.materials.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

@Table(name = "materials")
@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long id;

    @NotBlank
    @Column(name = "material_name", length = 100)
    private String name;

    @NotBlank
    @Column(name = "material_units", length = 20)
    private String units;

    @NotBlank
    @Column(name = "material_standard")
    private String standard;

    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY)
    @BatchSize(size = 100) // Оптимизация для ленивой загрузки
    private Set<Certificate> certificates = new HashSet<>();
}
