# Spring CRUD Base

###### Maven Dependency
```xml
<dependency>
    <groupId>com.github.paulosalonso</groupId>
    <artifactId>spring-crud-base</artifactId>
    <version>1.1.0</version>
</dependency>
```

Projeto base para criação de serviços e recusos de CRUD com Spring e Spring Data JPA.

## CrudService

A classe __com.alon.spring.crud.service.CrudService__ fornece métodos para listagem e CRUD de entidades. Um pré-requisito para utilização dessa classe é a implementação da interface __com.alon.spring.crud.model.BaseEntity__ pelas entidades JPA. Essa abordagem visa garantir que as entidades tenham os métodos __getId__ e __setId__.

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

Note que o repositório implementa, além da interface JpaRepository, a interface __JpaSpecificationExecutor__. Esse é um requisito para utilização de CrudService.

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
// Busca a primeira página de pessoas, informando Integer.MAX_VALUE como tamanho da página e ordena pelo nome
List<Pessoa> pessoas = service.list(0, Integer.MAX_VALUE, new Expression("nome:asc"));

// Cadastra uma pessoa
service.create(new Pessoa("Paulo", "xxx.xxx.xxx.xx"));

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

Note a anotação __@NotBlank__ nos atributos __nome__ e __cpf__ da entidade Pessoa. Os métodos __create__ e __update__ da classe __CrudService__ realizam a validação dos atributos anotados com validadores do pacote __javax.validation__. Nesse caso, se nome ou cpf estiverem nulos, vazios ou somente com espaços, chamar um desses métodos resultará em uma exceção.

Note também os parâmetros utilizados na chamada de __service.list__. O último é uma instância de __Expression__. Essa classe faz parte do projeto [Query Decoder](https://github.com/paulosalonso/query-decoder). O projeto Query Decoder também tem a classe __SpringJpaSpecificationDecoder__, que aplica filtros nas consultas a partir de expressões. Veja o [readme](https://github.com/paulosalonso/query-decoder/blob/master/README.md) do projeto para mais detalhes.

```java
.
.
.
@Autowired
PessoaService service;
.
.
.
// Busca pessoas que contém "Paulo" no nome
List<Pessoa> pessoas = service.list(new SpringJpaSpecificationDecoder("nome[CT]:Paulo"), 0, Integer.MAX_VALUE, new Expression("nome:asc"));
.
.
.
```

### Life Cycle Hooks

A classe __CrudService__ conta com os __Life Cycle Hooks__, ou __Ganchos de Ciclo de Vida__. Os hooks são funções que são executadas antes e depois de alguns dos métodos de CRUD. Os hooks existentes são:

* BEFORE_CREATE - antes da criação
* AFTER_CREATE - depois da criação
* BEFORE_UPDATE - antes da atualização
* AFTER_UPDATE - depois da atualização
* BEFORE_DELETE - antes da exclusão
* AFTER_DELETE - depois da exclusão

É possível adicionar quantas funções sejam necessárias para cada hook através dos seguintes métodos:

* addBeforeCreateHook
* addAfterCreateHook
* addBeforeUpdateHook
* addAfterUpdateHook
* addBeforeDeleteHook
* addAfterDeleteHook

Todos esses métodos esperam como parâmetro uma instância de __com.alon.spring.crud.service.CheckedFunction__, que é uma interface funcional. Seu método __apply__ deve ser implementado e pode lançar um __Throwable__. 

Também é possível utilizar o recurso de __method reference__ para informar os hooks, como veremos no exemplo abaixo.

No caso dos hooks para __create__ e __update__, o método apply recebe e retorna uma instância do tipo de entidade manipulado pelo serviço (no nosso exemplo, Pessoa). Já o hook de __delete__ recebe e devolve uma instância de __Long__, que deve ser o id da entidade.

#### Serviço PessoaService com hooks para create e update

```java
@Service
public class PessoaService extends CrudService<Pessoa, PessoaRepository> {

    @Autowired
    public PessoaService(PessoaRepository repository) {
        super(repository);
        super.addBeforeCreateHook(this::validarNome);
        super.addBeforeCreateHook(this::validarCpf);
        super.addBeforeUpdateHook(this::validarNome);
        super.addBeforeUpdateHook(this::validarCpf);
    }
    
    private Pessoa validarNome(Pessoa pessoa) throws Exception {
        if (pessoa.getNome().lenght() < 15)
            throw new Exception("O nome deve ter no mínimo 15 caracteres");
            
        return pessoa;
    }
    
    private Pessoa validarCpf(Pessoa pessoa) throws Exception {
        if (pessoa.getCpf().lenght() < 11)
            throw new Exception("O CPF deve ter 11 dígitos");
            
        return pessoa;
    }
}
```

Os hooks são executados na sequência em que são informados ao serviço. No nosso exemplo, o nome será validado primeiro, em seguida o cpf. Qualquer dos hooks que lançar uma exceção interrompe o processo.

Também é possível utilizar os hooks para manipular os objetos:

```java
.
.
.
addBeforeCreateHook(this::verificarNome);
addBeforeUpdateHook(this::verificarNome);
.
.
.
private Pessoa verificarNome(Pessoa pessoa) {
    if (pessoa.getNome() == null)
        pessoa.setNome("Paulo Alonso");
        
    return pessoa;
}
.
.
.
```

## CrudResource

A classe __com.alon.spring.crud.resource.CrudResource__ implementa os endpoints para listagem e CRUD de entidades via API. Pra utilizá-la, é necessário ter uma implementação de __CrudService__, conforme o tipo genérico solicitado na declaração da classe:

```java
public abstract class CrudResource<E extends BaseEntity, S extends CrudService<E, ?>> {
.
.
.
}
```

### Exemplo de implementação

#### Classe PessoaResource

```java
@RestController
@RequestMapping("/pessoa")
@CrossOrigin
public class PessoaResource extends CrudResource<Pessoa, PessoaService> {

    @Autowired
    public PessoaResource(PessoaService service) {
        super(service);
    }

}
```

Com essa implementação já temos disponíveis os seguintes endpoints:

* GET /pessoa
    * Retorna uma lista de pessoas
    * Aceita filtros através do query param "filter", utilizando a sintaxe de [Query Decoder](https://github.com/paulosalonso/query-decoder)
    * Aceita paginação através dos queries params "page" e "size"
    * Aceita ordenação através do query param "order", utilizando a sintaxe de [Query Decoder](https://github.com/paulosalonso/query-decoder)
* GET /pessoa/{id}
    * Retorna o cadastro de pessoa referente ao id informado no lugar de __{id}__
* POST /pessoa
    * Cadastra a pessoa enviada no corpo da requisição no formato JSON
* PUT /pessoa/{id}
    * Altera o cadastro da pessoa referente ao id informado no lugar de __{id}__ usando o JSON enviado no corpo da requisição
* DELETE /pessoa/{id}
    * Exclui o cadastro da pessoa referente ao id informado no lugar de __{id}__
    
## SpringJpaSpecificationDecoder

A classe __SpringJpaSpecificationDecoder__ extende a classe __QueryDecoder__ (projeto [QueryDecoder](https://github.com/paulosalonso/query-decoder) e implementa a interface __org.springframework.data.jpa.domain.Specification__ para aplicar filtros em consultas utilizando o __Spring Data JPA__. Para utilizá-la, o repositório Spring deve implementar, além de __org.springframework.data.jpa.repository.JpaRepository__, a interface __org.springframework.data.jpa.repository.JpaSpecificationExecutor__:

```java
public interface PessoaRepository extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {
}
```

Assim, ficam disponíveis os métodos de __JpaSpecificatonExecutor__ que recebem um __Specification__ como parâmetro. __SpringJpaSpecificationDecoder__ pode ser usado da seguinte maneira:

```java
@Autowired
private PessoaRepository repository;
.
.
.
Specification spec = new SpringJpaSpecificationDecoder("nome[CT]:Paulo");

List<Pessoa> pessoas = this.repository.findAll(spec);
```

O código acima deve retornar uma lista com todas as pessoas que contém "Paulo" no nome.
