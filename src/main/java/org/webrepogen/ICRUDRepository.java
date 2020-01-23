package org.webrepogen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ICRUDRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
