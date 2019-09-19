# Spring CRUD Base

Projeto base para criação de serviços e recusos de CRUD com Spring Data JPA.

## CrudService

A classe __com.alon.spring.crud.service.CrudService__ fornece métodos para listagem e CRUD de entidades. Um pré-requisito para utilização dessa classe é a implementação da interface __com.alon.spring.crud.model.BaseEntity__ pelas entidades JPA. Essa abordagem visa garantir que as entidades tenham os métodos getId e setId.
