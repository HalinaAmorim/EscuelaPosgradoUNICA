# Escuela Posgrado UNICA — Refatoração e Qualidade de Software

## 📚 Informações do projeto

**Projeto:** `escuela-posgrado-unica-backend`  
**Disciplina:** **Testes e Qualidade de Software**  
**Integrantes:** **Alice e Halina**  
**Repositório:** `https://github.com/HalinaAmorim/EscuelaPosgradoUNICA.git`  
**Ferramentas:** SonarQube, Lizard e Inteligência Artificial (GPT/Cursor)

---

## 🎯 Objetivo

Este projeto teve como objetivo analisar e melhorar a qualidade do backend Java/Spring Boot dos microsserviços **Autenticacion, Intranet e Matricula**, utilizando métricas de qualidade e refatorações orientadas por problemas identificados no código.

A análise inicial considerou as 60 classes mais críticas do backend. Os principais problemas encontrados foram **code smells, dívida técnica, duplicação, alta complexidade ciclomática/cognitiva, baixa coesão, alto acoplamento, responsabilidades excessivas e uso de field injection**.

O processo foi realizado com apoio de IA, mas as decisões finais foram avaliadas pelas integrantes, priorizando a preservação das regras de negócio, contratos REST, mensagens de erro, comportamento de autenticação e compatibilidade do sistema.

---

## 📊 Diagnóstico inicial

Na análise inicial do Top 60 foram identificados:

| Indicador | Resultado |
|---|---:|
| Code Smells | 356 |
| Dívida técnica | 3101 min (~51,7 h) |
| Duplicação | 10,1% |
| Complexidade estrutural | 1441 |
| Complexidade cognitiva | 547 |
| Bugs | 0 |
| Vulnerabilidades | 0 |
| Cobertura de testes | 0% |

As classes mais críticas concentravam problemas principalmente em `UsuarioService`, `EncuestaService`, `AuthService`, `GoogleOAuthService`, `ExcelService`, `CalificacionService` e `AsistenciaService`.

---

# 🔧 Principais refatorações realizadas

## 1. Services críticos

### `UsuarioService`
- Remoção de APIs paralelas e duplicadas entre Entity e DTO.
- Extração de métodos privados para validação, busca e atualização.
- Redução de exceções genéricas e responsabilidades repetidas.
- Resultado registrado: **18 → 2 code smells**.

### `EncuestaService`
- Separação das responsabilidades relacionadas a respostas, persistência e estatísticas.
- Extração de DTOs e exceção específica.
- Organização de métodos para reduzir o efeito de *God Service* e facilitar manutenção.
- O acoplamento original a múltiplos repositórios foi tratado por uma organização mais clara das responsabilidades.

### `AdminController`
- Retirada da lógica de importação de Excel e limpeza de duplicados do controller.
- Criação/uso de `AdminExcelService` e `AdminCleanupService`.
- Limpeza de caracteres inválidos nas anotações.
- Resultado registrado: **601 → 442 LOC** e **15 → 1 smell**.

### `AuthService`
- Remoção do grande `switch` por `Role`.
- Extração da validação de campos únicos para `UsuarioValidationService`.
- Extração do preenchimento específico por papel para `RoleFieldService`.
- Resultado registrado: **CCN máximo 15 → 0**, com **0 smells** no trecho refatorado.

### `ExcelService` / `ExcelServiceNew`
- Remoção de `ExcelServiceNew`, que representava código duplicado/morto.
- Extração de `ExcelWorkbookSupport`, `UsuarioExcelMapper` e `UsuarioImportColumns`.
- Redução da complexidade dos métodos de importação.
- A duplicação de aproximadamente **89,8% foi eliminada**.

### `CalificacionService`
- Extração dos DTOs.
- Uso de exceções tipadas.
- Separação dos cálculos de média/ranking em métodos menores.
- Introdução de helpers como `computeWeightedAverage`, `toRankingEntry` e `scaleOrZero`.
- Uso de `Stream.toList()` e constructor injection.
- Fórmulas e mensagens foram preservadas.

### `AsistenciaService`
- Unificação das operações repetidas de criação/atualização.
- Extração de `EstadisticasAsistenciaDTO`.
- Criação de métodos auxiliares para aplicação dos dados.
- Uso de guard clauses, exceções tipadas e DI por construtor.

### `TurnoMatriculaService`
- Centralização das validações compartilhadas entre `create()` e `update()`.
- Substituição de busca com `findAll` + filtro em memória por consulta específica no repositório.
- Uso de `Boolean.TRUE.equals`.
- Mensagens diferentes de criação e atualização foram preservadas.

### Services de Matrícula
Foram refatorados também:
- `AulaService`
- `TasaPagoService`
- `ProgramaEstudioService`
- `MencionService`
- `FacultadService`
- `ComisionUnidadPosgradoService`
- `SedeService`
- `PeriodoAcademicoService`

As principais ações foram **constructor injection, extração de validações, buscas, conversões, mapeadores e métodos auxiliares**, reduzindo duplicação e complexidade sem alterar as regras funcionais.

Destaques:
- `TasaPagoService`: duplicação **8,5% → 0%**.
- `PeriodoAcademicoService`: duplicação **8,7% → 0%** e CCN máximo **8 → 3**.
- `AulaService`: redução significativa de smells e dívida técnica.
- `ProgramaEstudioService`: centralização das validações e redução de complexidade.
- `MencionService`, `FacultadService`, `SedeService` e `ComisionUnidadPosgradoService`: melhoria de coesão e redução de repetição.

---

# 🧩 Refatorações de Controllers e Injeção de Dependências

A principal mudança estrutural nos controllers foi substituir **field injection com `@Autowired`** por **constructor injection**, tornando as dependências explícitas, finais e mais fáceis de testar.

Foram tratados:

- `AulaController`
- `ComisionUnidadPosgradoController`
- `FacultadController`
- `MencionController`
- `PeriodoAcademicoController`
- `ProgramaEstudioController`
- `SedeController`
- `TasaPagoController`
- `TurnoMatriculaController`

As rotas, payloads e respostas HTTP foram preservados.

Também foram realizadas melhorias em:
- `EncuestaController`
- `UsuarioController`
- `CalificacionController`
- `RootController`
- `AuthController`

Nesses controllers, houve redução de responsabilidades, remoção de `try/catch` repetidos e simplificação da construção das respostas.

---

# 🛡️ Segurança e autenticação

### `JwtUtils`
- Centralização da criação da `signingKey`.
- Definição explícita do charset UTF-8.
- Redução de duplicação.
- Validação de subject e expiração preservada.

### `AuthTokenFilter`
- Extração do parsing do token.
- Organização do fluxo de autenticação.
- Constructor injection.
- Redução de duplicação.
- A proposta de adicionar novas authorities/`ROLE_USER` foi rejeitada para evitar alteração de permissões.

### `AuthEntryPointJwt`
- Organização dos imports e documentação.
- Preservação exata do JSON de erro 401, status HTTP e contrato público.

### `UserPrincipal`
- Separação do mapeamento de roles/authorities.
- Redução da responsabilidade da classe de segurança.

### `Role`
- Criação de `asAuthority()`.
- Centralização do prefixo `ROLE_`.
- Remoção da concatenação repetida de authorities.

### `UserDetailsServiceImpl`
- Simplificação do `loadUserByUsername()`.
- Separação da busca e tratamento do usuário não encontrado.

### `SecurityConfig`
- Separação de configurações de CORS, autorização, sessão e autenticação em métodos auxiliares.
- `filterChain()` passou a atuar principalmente como orquestrador.

---

# ⚠️ Tratamento global de exceções

Foi criado/estruturado o `GlobalExceptionHandler` para centralizar o tratamento de exceções.

A mudança foi aplicada principalmente em:
- `EncuestaController`
- `UsuarioController`
- `CalificacionController`

Com isso:
- foram removidos `try/catch` repetidos;
- os controllers ficaram focados no fluxo HTTP;
- regras de negócio permaneceram nos services;
- o tratamento de erros ficou centralizado e mais fácil de manter.

---

# 📝 DTOs, Validators e documentação OpenAPI

Também foram realizadas melhorias em classes de entrada e saída, especialmente na camada de autenticação e usuários.

### DTOs tratados
- `ActualizarPerfilRequest`
- `ActualizarUsuarioAdminRequest`
- `CambiarPasswordRequest`
- `GoogleLoginRequest`
- `LoginRequest`
- `RegistroRequest`
- `AuthResponse`
- `MessageResponse`
- `UsuarioResponse`
- `GoogleUserInfo`

Principais mudanças:
- `requiredMode` explícito nas anotações `@Schema`.
- Organização das validações.
- Extração de helpers como `hasText()`.
- Substituição de *magic strings* por constantes.
- Simplificação de construtores.
- Preservação dos payloads e contratos públicos.

---

# 👤 Usuário, roles e validações

### `Usuario`
- Centralização de estados e separadores.
- Criação de `isActive()`.
- Uso de `Role.asAuthority()`.
- Regras de usuário ativo/inativo preservadas.

### `UsuarioRepository`
- Organização e formatação das consultas JPQL e derived queries.
- Assinaturas e contratos mantidos.

### `UsuarioUniquenessValidator`
- Decomposição de `validateForRegistro()`.
- Extração das validações de código de estudante, código docente e DNI.
- Criação de helpers para conflitos.
- Resultado registrado: **CCN máximo reduzido para 4**.

### `UsuarioRoleFieldsApplier`
- Extração de helpers `setIfPresent()` e `hasText()`.
- Redução de repetição no preenchimento de campos por role.

### `UsuarioDtoMapper`
- Extração de `copyRoleSpecificFields()`.
- Eliminação da duplicação entre `toUsuarioResponse()` e `toAuthResponse()`.

---

# 🔐 OAuth / Google

Foram realizadas refatorações menores e controladas em:

- `GoogleTokenVerifier`
- `GoogleUserProvisioning`
- `GoogleUserInfo`

Principais ações:
- Centralização de URLs, paths e parâmetros.
- Extração de `fetchUserInfo()`.
- Extração de `emailLocalPart()`.
- Centralização de prefixos.
- Criação de `isEmailVerified()`.
- Documentação OpenAPI com `@Schema`.

A grande proposta de dividir `GoogleOAuthService` em muitas classes foi **rejeitada**, pois a alteração aumentaria excessivamente a complexidade estrutural. A decisão demonstra que as métricas não foram tratadas como objetivo isolado.

---

# 📄 Outras melhorias

### `HealthController`
- Substituição da construção manual de `Map` por DTO específico.

### `RootController`
- Extração das informações estáticas para `ApiInfoResponse`.
- Simplificação do endpoint `getApiInfo()`.

### `ExcelProcessingException`
- Criação das factories `withMessage()` e `withCause()`.
- API de exceção mais clara.

### `RoleFieldsData`
- Criação de factory privada compartilhada para reduzir duplicação entre `fromRegistro()` e `fromAdmin()`.

---

# 📈 Resultados após as refatorações

A comparação antes/depois mostrou resultados relevantes principalmente no conjunto de classes de Matrícula:

| Indicador | Antes | Depois | Variação |
|---|---:|---:|---:|
| Code Smells — 20 classes | 87 | 39 | **-55,2%** |
| Dívida técnica — 20 classes | 611 min | 305 min | **-50,1%** |
| Complexidade cognitiva — 20 classes | 98 | 61 | **-37,8%** |
| Maior CCN Lizard — 20 classes | 8 | 6 | **-25%** |
| Duplicação ExcelService | ~89,8% | 0% | **eliminada** |
| Duplicação Periodo/TasaPago | 8,7% / 8,5% | 0% | **eliminada** |

Bugs e vulnerabilidades permaneceram em **0**, indicando que as refatorações não introduziram problemas detectados pelo SonarQube.

A cobertura automatizada registrada permaneceu em **0%**, portanto a melhoria estrutural não deve ser confundida com aumento de cobertura de testes.

---

# 🤖 Uso da Inteligência Artificial

A IA foi utilizada como apoio para:

- identificar code smells;
- sugerir extração de métodos e classes;
- propor melhorias de SOLID e Clean Code;
- identificar duplicações;
- sugerir melhorias de injeção de dependências;
- auxiliar na organização de DTOs e validações;
- comparar métricas antes e depois.

As sugestões **não foram aceitas automaticamente**.

A dupla analisou cada proposta considerando:
- regras de negócio;
- contratos REST;
- mensagens de erro;
- permissões e segurança JWT;
- fusos horários;
- mutabilidade de listas;
- compatibilidade com clientes;
- complexidade introduzida pela própria refatoração.

Por exemplo, a grande refatoração proposta para `GoogleOAuthService` foi rejeitada, assim como alterações que poderiam ampliar permissões JWT ou modificar comportamentos existentes.

---

# 🧪 Relação com Testes e Qualidade de Software

As refatorações foram orientadas por princípios e métricas diretamente relacionados à qualidade de software:

- **Clean Code**
- **SOLID**, principalmente SRP e DIP
- **redução de duplicação**
- **redução de complexidade ciclomática**
- **redução de complexidade cognitiva**
- **melhoria de coesão**
- **redução de acoplamento**
- **melhoria de manutenibilidade**
- **tratamento adequado de exceções**
- **testabilidade por constructor injection**
- **preservação de contratos e comportamento**

A troca de field injection por constructor injection, por exemplo, tornou dependências explícitas e facilitou a criação de testes unitários.

---

# 📋 Registro resumido das 59 refatorações

| # | Área | Principal ação |
|---:|---|---|
| 1 | UsuarioService | Unificação de APIs e extração de validações |
| 2 | EncuestaService | Separação de responsabilidades, DTOs e exceção |
| 3 | GoogleOAuthService | Proposta de grande divisão **rejeitada** |
| 4 | AdminController | Extração de Excel e limpeza |
| 5 | AuthService | Separação de validação e regras por Role |
| 6 | ExcelService | Remoção de código duplicado/morto |
| 7 | CalificacionService | DTOs, métodos menores e exceções tipadas |
| 8 | AsistenciaService | Unificação de create/update e DTO |
| 9 | TurnoMatriculaService | Validação compartilhada e query específica |
| 10 | AulaService | Helpers, DI e separação de responsabilidades |
| 11 | TasaPagoService | Helpers e remoção de duplicação |
| 12 | ProgramaEstudioService | Centralização de validações |
| 13 | MencionService | Extração de buscas e mapeamentos |
| 14 | FacultadService | Helpers e constructor injection |
| 15 | ComisionUnidadPosgradoService | Extração de buscas e conversões |
| 16 | SedeService | Validações, buscas e conversões |
| 17 | PeriodoAcademicoService | Validações de datas e código |
| 18 | AulaController | Constructor injection |
| 19 | ComisionUnidadPosgradoController | Constructor injection |
| 20 | FacultadController | Constructor injection |
| 21 | MencionController | Constructor injection |
| 22 | PeriodoAcademicoController | Constructor injection |
| 23 | ProgramaEstudioController | Constructor injection e limpeza |
| 24 | SedeController | Constructor injection |
| 25 | TasaPagoController | Constructor injection |
| 26 | TurnoMatriculaController | Constructor injection |
| 27 | JwtUtils | Centralização da chave e UTF-8 |
| 28 | AuthTokenFilter | Parsing e autenticação organizados |
| 29 | AuthEntryPointJwt | Organização sem alterar resposta 401 |
| 30 | EncuestaController | GlobalExceptionHandler |
| 31 | UsuarioController | GlobalExceptionHandler e delegação |
| 32 | HealthController | DTO para status |
| 33 | CalificacionController | GlobalExceptionHandler |
| 34 | RootController | ApiInfoResponse |
| 35 | AuthController | Delegação das operações de autenticação |
| 36 | UserPrincipal | Separação do mapeamento de authorities |
| 37 | AuthTokenFilter | Extração da criação da autenticação |
| 38 | UserDetailsServiceImpl | Simplificação da busca de usuário |
| 39 | SecurityConfig | Separação das configurações de segurança |
| 40 | ActualizarPerfilRequest | Helpers e OpenAPI |
| 41 | ActualizarUsuarioAdminRequest | Schema explícito |
| 42 | CambiarPasswordRequest | Limpeza de schema e literais |
| 43 | GoogleLoginRequest | Organização e documentação |
| 44 | LoginRequest | `requiredMode` explícito |
| 45 | RegistroRequest | Schema requerido explícito |
| 46 | AuthResponse | Constantes para literais |
| 47 | MessageResponse | Simplificação dos construtores |
| 48 | UsuarioResponse | Constante para separador de nome |
| 49 | Usuario | Estados, apellidos e authorities |
| 50 | Role | `asAuthority()` e prefixo centralizado |
| 51 | UsuarioRepository | Legibilidade das queries |
| 52 | UsuarioUniquenessValidator | Extração de validações |
| 53 | UsuarioRoleFieldsApplier | Helpers para campos por role |
| 54 | UsuarioDtoMapper | Extração de campos por role |
| 55 | GoogleTokenVerifier | Constantes e fetch compartilhado |
| 56 | GoogleUserProvisioning | Helpers e constantes de e-mail |
| 57 | GoogleUserInfo | `@Schema` e verificação de e-mail |
| 58 | ExcelProcessingException | Factories de exceção |
| 59 | RoleFieldsData | Factory compartilhada |

---

# ✅ Conclusão

As refatorações melhoraram principalmente a **organização, legibilidade, coesão, testabilidade e manutenibilidade** do backend. Os resultados mostram redução significativa de code smells, dívida técnica, duplicação e complexidade em partes críticas do sistema.

O principal aprendizado do projeto foi que **qualidade de software não significa apenas reduzir métricas**. Cada sugestão da IA precisou ser validada pelas integrantes para garantir que a melhoria não alterasse regras de negócio, contratos, segurança ou comportamento existente.

Assim, a IA foi utilizada como ferramenta de apoio à análise, enquanto a decisão de refatorar, adaptar ou rejeitar cada proposta permaneceu sob responsabilidade da equipe.

---

## 👩‍💻 Participantes

**Alice e Halina**

**Disciplina:** Testes e Qualidade de Software
