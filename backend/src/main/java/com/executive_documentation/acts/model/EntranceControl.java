package com.executive_documentation.acts.model;

import com.executive_documentation.materials.model.Material;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "entrance_controls")
@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@ToString
public class EntranceControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "control_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "control_act_id")
    private Act act;

    @Column(name = "control_number")
    private String controlNumber;

    @Column(name = "control_date")
    private LocalDate date;

    @JoinColumn(name = "material_id")
    @OneToOne(cascade = CascadeType.ALL)
    private Material material;

    @Column(name = "quantity", precision = 15, scale = 3)
    private BigDecimal quantity;
}
