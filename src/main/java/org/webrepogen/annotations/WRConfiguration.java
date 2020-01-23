package org.webrepogen.annotations;

public @interface WRConfiguration {
    String repositoryBaseInterface() default "org.webrepogen.ICRUDRepository";
    String controllerBaseClass() default "org.webrepogen.AbstractCRUDController";
}
