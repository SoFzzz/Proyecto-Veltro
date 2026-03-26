package com.veltro.inventory.domain.purchasing.model;

import com.veltro.inventory.domain.shared.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Supplier entity for the purchasing module (B2-04).
 *
 * Represents suppliers from whom purchase orders are created.
 * Tax ID is kept as a simple unique string without format validation
 * as per project requirements.
 */
@Entity
@Table(name = "supplier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tax identification (RUC, RFC, etc.).
     * Simple unique string - no format validation per project requirements.
     */
    @Column(name = "tax_id", nullable = false, unique = true, length = 50)
    private String taxId;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Override
    public String toString() {
        return "SupplierEntity{" +
                "id=" + id +
                ", taxId='" + taxId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}