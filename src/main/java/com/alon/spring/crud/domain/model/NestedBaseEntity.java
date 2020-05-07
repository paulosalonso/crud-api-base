package com.alon.spring.crud.domain.model;

import java.io.Serializable;

public abstract class NestedBaseEntity<ID extends Serializable, MASTER_ENTITY_TYPE extends BaseEntity> extends BaseEntity<ID> {

    public abstract MASTER_ENTITY_TYPE getMasterEntity();
    public abstract void setMasterEntity(MASTER_ENTITY_TYPE masterEntity);
}
