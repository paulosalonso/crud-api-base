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
    
    @NotBlank
    private String cpf;
    
    public Pessoa(String nome, String cpf) {
        this.nome = nome;
        this.cpf = cpf;
    }
    
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
    
    public String getCpf() {
        return this.cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
 }
    
```

#### Repositório PessoaRepository

```java
public interface PessoaRepository extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {}
```

#### Serviço PessoaService

```java
@Service
public class PessoaService extends CrudService<Pessoa, PessoaRepository> {

    @Autowired
    public PessoaService(PessoaRepository repository) {
        super(repository);
    }

}
```

Com estas implementações já é possível listar, criar, buscar, alterar e excluir entidades do tipo __Pessoa__:

```java
.
.
.
@Autowired
PessoaService service;
.
.
.
// Lista pessoas ordenando pelo nome
List<Pessoa> pessoas = service.listAll(1, Integer.MAX_VALUE, new Expression("nome:asc"));

// Cadastra uma pessoa
service.create(new Pessoa("Paulo", "xxx.xxx.xxx.xx");

// Busca a pessoa de id 1
Pessoa pessoa = service.read(1);

// Altera o nome da pessoa
pessoa.setNome("Paulo Alonso");

// Grava a alteração
service.update(pessoa);

// Exclui a pessoa de id 1
service.delete(1);
.
.
.
```
