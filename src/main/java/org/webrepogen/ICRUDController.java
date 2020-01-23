package org.webrepogen;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ICRUDController<T, ID> {

    void init(ICRUDRepository<T, ID> repository, Class<T> clazz);

}
