package org.webrepogen;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ICRUDController<T, ID> {

    void init(JpaRepository<T, ID> jpaRepository, Class<T> clazz);

}
