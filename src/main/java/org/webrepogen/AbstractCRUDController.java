package org.webrepogen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractCRUDController<T, ID extends Serializable> implements ICRUDController<T, ID> {

    protected ICRUDRepository<T, ID> repo;

    public AbstractCRUDController() {
    }

    @Override
    public void init(ICRUDRepository<T, ID> repository, Class<T> clazz, Class<ID> idClazz) {
        this.repo = repository;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<T> list() {
        return repo.findAll();
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public T create(@RequestBody T entity) {
        return repo.save(entity);
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
    public T update(@PathVariable(value = "id") ID id, @RequestBody T entity) {
        return repo.save(entity);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable(value = "id") ID id) {
        repo.deleteById(id);
    }

    @RequestMapping(value = "get/{id}", method = RequestMethod.GET)
    public T get(@PathVariable(value = "id") ID id) {
        return repo.getOne(id);
    }
}
