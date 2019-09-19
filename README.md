# Spring CRUD Base

Projeto base para criação de serviços e recusos de CRUD com Spring Data JPA.

## CrudService

A classe __com.alon.spring.crud.service.CrudService__ fornece métodos para listagem e CRUD de entidades. Um pré-requisito para utilização dessa classe é a implementação da interface __com.alon.spring.crud.model.BaseEntity__ pelas entidades JPA. Essa abordagem visa garantir que as entidades tenham os métodos getId e setId.

### Exemplo de implementação

#### Entidade Pessoa

```java
@Entity
public class Pessoa implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;
    
    @Override
    public Long getId() {
      return this.id;
    }
    
    @Override
    public void setId(Long id) {
      this.id = id;
    }
    
    public String getNome() {
      return this.nome;
    }
    
    public void setNome(String nome) {
      this.nome = nome;
    }
    
 }
    
```

#### Repositório PessoaRepository

```java
public interface PessoaRepository extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {}
```
