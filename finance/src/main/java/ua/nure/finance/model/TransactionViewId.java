package ua.nure.finance.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class TransactionViewId implements Serializable {
    private Long id;
    private String type;

    public TransactionViewId() {
    }

    public TransactionViewId(Long id, String type) {
        this.id = id;
        this.type = type;
    }

    // Getters and Setters

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionViewId that = (TransactionViewId) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
