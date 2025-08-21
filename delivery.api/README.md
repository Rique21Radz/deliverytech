# 🍕 DeliveryTech API

## 📝 Descrição
Projeto API REST para aplicação de delivery do curso Arquitetura de Software - FAT (QualificaSP).
Essa API permite o gerenciamento completo de: clientes, restaurantes, produtos e criação de pedidos.

## 🛠️ Funcionalidades
✅ Gerenciamento CRUD completo (Criar, Ler, Atualizar, Inativar).

✅ Fluxo de Pedidos:

- Criação de um novo pedido;
- Adição de itens a um pedido existente;
- Confirmação de pedido com cálculo de totais;
- Listagem de pedidos por cliente;

✅ Validação de Dados: Garante a integridade dos dados na entrada da API.

✅ Banco de Dados em Memória.

## 🤖 Tecnologias Utilizadas

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3
- **Data:** Spring Data JPA / Hibernate
- **Banco de Dados:** H2 In-Memory Database
- **Build Tool:** Maven
- **Validação:** Jakarta Bean Validation
- **Utilitários:** Lombok

## 💻 Como executar

### Passo a Passo
1. **Clone o repositório:**
   `git clone [URL_DO_SEU_REPOSITORIO_AQUI]`

2. **Navegue até a pasta do projeto:**
   `cd [NOME_DA_PASTA_DO_PROJETO]`

3. **Execute a aplicação com o Maven:**
   `mvn spring-boot:run`

4. **Acesse a API:**
- O servidor estará rodando em http://localhost:8080.
- Você pode testar os endpoints usando o Postman ou outro cliente HTTP.

5. **Acesse o Console do H2 (Opcional):**
- Para visualizar o banco de dados em memória, acesse http://localhost:8080/h2-console.
- Use as seguintes credenciais para logar:

   **JDBC URL: jdbc:h2:mem:delivery**

   **User Name: sa**

   **Password: (deixe em branco)**

## 📖 Documentação da API

Aqui estão os principais endpoints disponíveis.

### *Clientes*
| Método URL | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/clientes` | Lista todos os clientes ativos. |
| `GET` | `/clientes/{id}` | Busca um cliente por ID. |
| `POST` | `/clientes` | Cadastra um novo cliente. |
| `PUT` | `/clientes/{id}` | Atualiza os dados de um cliente. |
| `DELETE`| `/clientes/{id}` | Inativa um cliente (soft delete). |

### *Restaurantes*
| Método URL | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/restaurantes` | Lista todos os restaurantes ativos. |
| `GET` | `/restaurantes/{id}` | Busca um restaurante por ID. |
| `POST` | `/restaurantes` | Cadastra um novo restaurante. |
| `PUT` | `/restaurantes/{id}` | Atualiza os dados de um restaurante. |
| `DELETE`| `/restaurantes/{id}` | Inativa um restaurante (soft delete). |

### *Produtos*
| Método URL | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/produtos/restaurante/{id}` | Lista os produtos de um restaurante. |
| `GET` | `/produtos/{id}` | Busca um produto por ID. |
| `POST` | `/produtos?restauranteId={id}` | Cadastra um novo produto. |
| `PUT` | `/produtos/{id}` | Atualiza os dados de um produto. |
| `DELETE`| `/produtos/{id}` | Torna um produto indisponível. |

### *Pedidos*
| Método URL | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/pedidos/cliente/{id}` | Lista todos os pedidos de um cliente. |
| `POST`| `/pedidos?clienteId={id}&restauranteId={id}` | Cria um novo pedido (vazio). |
| `POST`| `/pedidos/{id}/itens?produtoId={id}&quantidade={qtd}` | Adiciona um item a um pedido. |
| `PUT` | `/pedidos/{id}/confirmar` | Confirma um pedido, calculando os totais. |

---
## 🗂️ Estrutura do Projeto
O projeto está organizado em uma arquitetura de camadas para separação de responsabilidades:

- **`Controller`**: Camada de API REST, responsável por receber requisições HTTP.
- **`Service`**: Camada de serviço, onde reside a lógica de negócio.
- **`Repository`**: Camada de acesso a dados usando Spring Data JPA.
- **`Model`**: As entidades JPA que modelam o banco de dados.
- **`Enums`**: Enumerações usadas no projeto, como **`StatusPedido`**.

## 👨🏼‍💻 Desenvolvedor
*Henrique Radzevicius Toledo*

*TI 56 - Arquitetura de Sistemas*

*Desenvolvido com JDK 21 e Spring Boot 3.5.4*
