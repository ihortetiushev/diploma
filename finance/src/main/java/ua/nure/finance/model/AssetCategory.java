package ua.nure.finance.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;


@Entity
@Valid
@Table(name = "assets_category", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
public class AssetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "read_only", nullable = false)
    private boolean readOnly = false;
}

