# Spring CRUD Base

Projeto base para criação de serviços e recusos de CRUD com Spring e Spring Data JPA.

###### Maven Dependency

```xml
<dependency>
    <groupId>com.github.paulosalonso</groupId>
    <artifactId>spring-crud-base</artifactId>
    <version>2.0.0</version>
</dependency>
```

## CrudService

A interface __com.alon.spring.crud.service.CrudService__ fornece métodos padrão para listagem e CRUD de entidades. Um pré-requisito para utilização dessa classe é a implementação da interface __com.alon.spring.crud.model.BaseEntity__ pelas entidades JPA. Essa abordagem visa garantir que as entidades tenham os métodos __getId__ e __setId__.

### Exemplo de implementação

#### Entidade Pessoa

```java
@Entity
public class Pessoa implements BaseEntity<Long> {

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
public class PessoaService implements CrudService<Long, Pessoa, PessoaRepository> {
   
   @Autowired
   private PessoaRepository repository;
   
   @Override
   public PessoaRepository getRepository() {
       return this.repository;
   }
    
   @Override
   public List<Order> getDefaultSort() {
       Collections.singletonList(Order.asc("nome"));
   }

}
```

Com estas implementações já é possível listar, criar, buscar, alterar e excluir entidades do tipo __Pessoa__:

```java
@Autowired
PessoaService service;

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
```

Note a anotação __@NotBlank__ nos atributos __nome__ e __cpf__ da entidade Pessoa. Os métodos __create__ e __update__ da classe __CrudService__ realizam a validação dos atributos anotados com validadores do pacote __javax.validation__. Nesse caso, se nome ou cpf estiverem nulos, vazios ou somente com espaços, chamar um desses métodos resultará em uma exceção.

Note também os parâmetros utilizados na chamada de __service.list__. O último é uma instância de __Expression__ e neste escopo é responsável pela ordenação da listagem. Essa classe faz parte do projeto [Query Decoder](https://github.com/paulosalonso/query-decoder). Veja o [readme](https://github.com/paulosalonso/query-decoder/blob/master/README.md) do projeto para mais detalhes.

A string informada no construtor de __Expression__ deve seguir o seguinte formato:

```
atributo:tipo_ordenacao
```

onde __tipo_ordenacao__ aceita os valores asc/ASC e desc/DESC. Caso o valor informado seja diferente, será considerado ASC por padrão. Exemplos:

```
new Expression("nome:asc");
new Expression("nome:ASC");
new Expression("nome:desc");
new Expression("nome:DESC");
```

### Life Cycle Hooks

A interface __CrudService__ conta com os __Life Cycle Hooks__, ou __Ganchos de Ciclo de Vida__. Os hooks são funções que são executadas antes e depois de alguns dos métodos de CRUD. Os hooks existentes são:

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

No caso dos hooks para __create__ e __update__, o método apply recebe e retorna uma instância do tipo de entidade manipulado pelo serviço (no nosso exemplo, Pessoa). Já o hook de __delete__ recebe e devolve uma instância do tipo do ID da entidade.

#### Serviço PessoaService com hooks para create e update

```java
@Service
public class PessoaService implements CrudService<Long, Pessoa, PessoaRepository> {

    @Autowired
    private PessoaRepository repository;
    
    public PessoaService() {
        super.addBeforeCreateHook(this::validarNome);
        super.addBeforeCreateHook(this::validarCpf);
        super.addBeforeUpdateHook(this::validarNome);
        super.addBeforeUpdateHook(this::validarCpf);
    }
   
    @Override
    public PessoaRepository getRepository() {
        return this.repository;
    }
    
    @Override
    public List<Order> getDefaultSort() {
        Collections.singletonList(Order.asc("nome"));
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

A classe __com.alon.spring.crud.resource.CrudResource__ implementa os endpoints para listagem e CRUD de entidades via API.
Ela é uma classe abstrata e precisa ser estendida por uma classe anotada com __@RestController__. Essa é sua declaração:

```java
public abstract class CrudResource< 
        S extends CrudService,
        C, 
        U,
        P extends ResourceDtoConverterProvider
> {
.
.
.
}
```

Entenda os tipos genéricos:

* __S extends CrudService__: Serviço responsável pelo CRUD da entidade.
* __C extends InputDto__: Tipo do parâmetro recebido pelo método __create__.
* __U extends InputDto__: Tipo de parâmetro recebido pelo método __update__.
* __P extends ResourceDtoConverterProvider__: Provedor de conversores de tipos de entrada para entidade, e entidade para tipos de saída.

### Entidades como DTO's

É possível fazer com que a classe CrudResource utilize as próprias entidades (models) para entrada e saída de dados. Para isso, é preciso informar a classe da entidade como tipo para os genéricos __C__ e __U__ (a entidade deve implementar a interface __BaseEntity__), e __EntityOutputConverter__ para __P extends ResourceDtoConverterProvider__.

As classes de conversão são injetadas em CrudResource, porém, como não estarão dentro do pacote escaneado pelo Spring para carregamento de componentes (que é o pacote do princial do projeto que está usando Spring CRUD Base, e seus descendentes) elas não serão providas automaticamente. Existem duas soluções para isso:

Informar os pacotes nos quais o Spring deve procurar por componentes, sendo o pacote principal do seu projeto, e o pacote __com.alon.spring.crud__, através da anotação __@ComponentScan__:

```java
@SpringBootApplication
@ComponentScan(basePackages = { "br.com.myapplication", "com.alon.spring.crud" })
public class MyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}

}
```

Ou criar uma classe provedora de Beans no seu projeto:

```java
@Configuration
public class SpringCrudBaseBeansProvider {
	
	@Bean
	public EntityConverterProvider provideEntityConverterProvider() {
		return new EntityConverterProvider();
	}
	
	@Bean
	public <T extends BaseEntity> EntityListOutputConverter<T> provideEntityListOutputConverter() {
		return new EntityListOutputConverter<>();
	}
	
	@Bean
	public <T extends BaseEntity> EntityInputConverter<T> provideEntityInputConverter() {
		return new EntityInputConverter<>();
	}
	
	@Bean
	public <T extends BaseEntity> EntityOutputConverter<T> provideEntityOutputConverter() {
		return new EntityOutputConverter<>();
	}

}
```


### DTO's customizados

Como uma boa prática, por questões de segurança tando da aplicação quanto das regras do negócio, os DTO's não devem expor dados internos da API. Para atender esse requisito podemos customizar os inputs e outputs dos métodos CRUD e listagem.

Para facilitar a compreensão, nos exemplos a seguir os nomes dos atributos dos DTO's estão iguais ao da entidade Pessoa utilizada anteriormente.

Primeiro, criamos os DTO's responsáveis pela comunicação com a API. Perceba que apesar de serem DTO's com atributos públicos, os getters e setters foram incluídos. Isso é necessário para que a serialização/desserialização do objeto possa ser realizada pelo Spring. Também é necessário um construtor padrão. Como foram criados construtores com todos os atributos, foi necessário declarar explicitamente os construtores padrão.

#### PessoaDto

```java
public class PessoaDto {

    public Long id;
    public String nome;
    public String cpf;
    
    public PessoaDto() {}
    
    public PessoaDto(Long id, String nome, String cpf){
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
    }
    
    // getters & setters

}
```

#### ListPessoaOuput

```java
public class ListPessoaOutput extends ListOutput<PessoaDto> {

    public ListPessoaOutput() {
    }

    public ListPessoaOutput(List<PessoaDto> content, int page, int pageSize, int totalPages, int totalSize) {
        super(content, page, pageSize, totalPages, totalSize);
    }
    
}
```

#### CreatePessoaInput

```java
public CreatePessoaInput {
    
    public String nome;
    public String cpf;
    
    public CreatePessoaInput() {}
    
    public CreatePessoaInput(String nome, String cpf) {
        this.nome = nome;
        this.cpf = cpf;
    }
    
    // getters & setters
}
```

#### UpdatePessoaInput

```java
public UpdatePessoaInput {
    
    public Long id;
    public String nome;
    public String cpf;
    
    public CreatePessoaInput() {}
    
    public CreatePessoaInput(Long id, String nome, String cpf) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
    }
    
    // getters & setters
}
```

Depois, criamos os conversores:

#### PessoaEntityToPessoaDtoConverter

```java
@Component
class PessoaEntityToPessoaDtoConverter implements OutputDtoConverter<Pessoa, PessoaDto> {
    
    @Override
    public PessoaDto convert(Pessoa data) {
        PessoaDto pessoaDto = new PessoaDto();
        pessoaDto.id = data.getId();
        pessoaDto.nome = data.getNome();
        pessoaDto.cpf = data.cpf;
        
        return pessoaDto;
    }
    
}
```

#### ListPessoaOutputConverter
```java
@Component
public class ListPessoaOutputConverter implements OutputDtoConverter<Page<Pessoa>, ListPessoaOutput> {

    @Autowired
    private PessoaEntityToPessoaDtoConverter converter;
    
    @Override
    public ListPessoaOutput convert(Page<Pessoa> data) {
        List<PessoaDto> pessoasDto = data.getContent()
                                         .stream()
                                         .map(pessoa -> this.converter.convert(user))
                                         .collect(Collectors.toList());
        
        ListPessoaOutput output = new ListPessoaOutput();
        output.content = pessoasDto;
        output.page = data.getNumber() + 1;
        output.pageSize = modelosDto.size();
        output.totalPages = data.getTotalPages();
        output.totalSize = data.getNumberOfElements();
        
        return output;
    }
    
}
```

#### CreatePessoaInputConverter

```java
@Component
public class CreatePessoaInputConverter implements InputDtoConverter<CreatePessoaInput, Pessoa> {
    
    @Override
    public Pessoa convert(CreatePessoaInput input) {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(input.nome);
        pessoa.setCpf(input.cpf);
        
        return pessoa;
    }
    
}
```

#### UpdatePessoaInputConverter

```java
@Component
public class UpdatePessoaInputConverter implements InputDtoConverter<UpdatePessoaInput, Pessoa> {
    
    @Override
    public Pessoa convert(UpdatePessoaInput input) {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(input.id);
        pessoa.setNome(input.nome);
        pessoa.setCpf(input.cpf);
        
        return pessoa;
    }
    
}
```

E por fim, o provedor de conversores:

#### PessoaResourceDtoConverterProvider

```java
@Component
public class PessoaResourceDtoConverterProvider implements ResourceDtoConverterProvider {

    @Autowired
    private ListPessoaOutputConverter listConverter;
    
    @Autowired 
    private PessoaEntityToPessoaDtoConverter entityToDtoConverter;
    
    @Autowired
    private CreatePessoaInputConverter createInputConverter;
    
    @Autowired
    private UpdatePessoaInputConverter updateInputConverter;
    
    @Override
    public ListPessoaOutputConverter getListOutputDtoConverter() {
        return this.listConverter;
    }

    @Override
    public PessoaEntityToPessoaDtoConverter getReadOutputDtoConverter() {
        return this.entityToDtoConverter;
    }

    @Override
    public CreatePessoaInputConverter getCreateInputDtoConverter() {
        return this.createInputConverter;
    }

    @Override
    public PessoaEntityToPessoaDtoConverter getCreateOutputDtoConverter() {
        return this.entityToDtoConverter;
    }

    @Override
    public UpdatePessoaInputConverter getUpdateInputDtoConverter() {
        return this.updateInputConverter;
    }

    @Override
    public PessoaEntityToPessoaDtoConverter getUpdateOutputDtoConverter() {
        return this.entityToDtoConverter;
    }
    
}
```

Com os DTO's e seus conversores definidos, implementamos PessoaResource:

#### PessoaResource

```java
@RestController
@RequestMapping("/pessoa")
@CrossOrigin
public class PessoaResource extends CrudResource<
        PessoaService, 
        CreatePessoaInput, 
        UpdatePessoaInput,
        PessoaResourceDtoConverterProvider
> {}
```

### Paths

Para que o Spring possa mapear os métodos de CrudResource, é preciso configurar as propriedades para os paths no arquivo __applications.properties__ do projeto:

```properties
com.alon.spring.crud.path.list=/list
com.alon.spring.crud.path.read=/read/{id}
com.alon.spring.crud.path.create=/create
com.alon.spring.crud.path.update=/update
com.alon.spring.crud.path.delete=/delete/{id}
```

Os paths acima são apenas exemplos, eles podem ser configurados de acordo com a necessidade ou padrão do projeto.

Com isso, temos os seguintes endpoints disponíveis para CRUD:

* GET /pessoa/list
    * Retorna uma lista de pessoas
    * Aceita filtros através do query param "filter", utilizando a sintaxe de [Query Decoder](https://github.com/paulosalonso/query-decoder)
    * Aceita paginação através dos queries params "page" e "size"
    * Aceita ordenação através do query param "order", utilizando a sintaxe de [Query Decoder](https://github.com/paulosalonso/query-decoder)
* GET /pessoa/read/{id}
    * Retorna o cadastro de pessoa referente ao id informado no lugar de __{id}__
* POST /pessoa/create
    * Cadastra a pessoa enviada no corpo da requisição no formato JSON
* PUT /pessoa/update
    * Altera o cadastro da pessoa enviada no corpo da requisição no formato JSON
* DELETE /pessoa/delete/{id}
    * Exclui o cadastro da pessoa referente ao id informado no lugar de __{id}__
    
## SpringJpaSpecificationDecoder

A classe __SpringJpaSpecificationDecoder__ extende a classe __QueryDecoder__ (projeto [QueryDecoder](https://github.com/paulosalonso/query-decoder)) e implementa a interface __org.springframework.data.jpa.domain.Specification__ para aplicar filtros em consultas utilizando o __Spring Data JPA__. Para utilizá-la, o repositório Spring deve implementar, além de __org.springframework.data.jpa.repository.JpaRepository__, a interface __org.springframework.data.jpa.repository.JpaSpecificationExecutor__:

```java
public interface PessoaRepository extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {}
```

Assim, ficam disponíveis os métodos de __JpaSpecificatonExecutor__ que recebem um __Specification__ como parâmetro. __SpringJpaSpecificationDecoder__ pode ser usado da seguinte maneira:

```java
@Autowired
private PessoaRepository repository;
.
.
.
Specification<Pessoa> spec = new SpringJpaSpecificationDecoder<>("nome[CT]:Paulo");

List<Pessoa> pessoas = this.repository.findAll(spec);
```

O código acima deve retornar uma lista com todas as pessoas que contém "Paulo" no nome.

### NULL e NOT NULL

Para verificar se valores são NULL ou NOT NULL, basta utilizar o operador EQ ([ou suprimir o operador, que é equivalente](https://github.com/paulosalonso/query-decoder#operadores)), seguido da comparação desejada:

* nome[EQ]:NULL ou nome:NULL
* nome[EQ]:NOT NULL ou nome: NOT NULL

Esta operação é case insensitive, então as expressões abaixo têm o mesmo efeito:

* nome:NULL ou nome:null
* nome:NOT NULL ou nome:not null

### Encadeamento de atributos

Quando temos relacionamentos entre entidades, é possível verificar atributos da classe relacionada através do encadeamento de atributos. Veja as entidades abaixo:

```java
@Entity
public class Cidade {
    @Id
    private Long codigoIbge;
    
    private String nome;
    
    // getters & setters
}

@Entity
public class Pessoa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    
    @ManyToOne
    @JoinColumn(name = "codigoIbgeCidade")
    private Cidade cidade;
    
    // getters & setters
}
```

Para buscar por todas as pessoas cadastradas que são de São Paulo (código IBGE 3550308), fazemos o seguinte:

```java

@Autowired
private PessoaRepository repository;
.
.
.
Specification<Pessoa> spec = new SpringJpaSpecificationDecoder<>("cidade.codigoIbge[EQ]:3550308");

List<Pessoa> pessoas = this.repository.findAll(spec);
```
