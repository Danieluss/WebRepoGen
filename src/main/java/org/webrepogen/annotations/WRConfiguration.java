package org.webrepogen.annotations;

public @interface WRConfiguration {
    String repositoryBaseInterface() default "org.springframework.data.jpa.repository.JpaRepository";
    String controllerBaseClass() default "org.webrepogen.AbstractCRUDController";
}
