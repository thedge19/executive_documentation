package com.executive_documentation.workings.model;

import com.executive_documentation.standard.model.Standard;
import com.executive_documentation.subobjects.model.SubObject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Table(name = "works")
@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Working {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_id")
    private Long id;

    @NotBlank
    @Column(name = "work_name")
    private String name;

    @NotBlank
    @Column(name = "work_units")
    private String units;

    @Column(name = "work_quantity")
    private BigDecimal quantity;

    @Column(name = "work_done")
    private BigDecimal done;

    @NotNull
    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", insertable = false, updatable = false)
    private BigDecimal totalAmount;

    @Column(name = "done_amount", insertable = false, updatable = false)
    private BigDecimal doneAmount;

    @Column(name = "remaining_amount", insertable = false, updatable = false)
    private BigDecimal remainingAmount;

    @JoinColumn(name = "work_standard")
    @ManyToOne
    private Standard standard;

    @JoinColumn(name = "work_subobject_id")
    @ManyToOne
    private SubObject subObject;

    @Column(name = "final_quantity", insertable = false, updatable = false)
    private BigDecimal finalQuantity;

    @Override
    public String toString() {
        return "Working{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", units='" + units + '\'' +
                ", quantity=" + quantity +
                ", done=" + done +
                '}';
    }
}