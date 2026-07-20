# Relatório Geral do Projeto — E-commerce sobre Microkernel (INF008)

Este relatório consolida tudo que foi feito no projeto desde o primeiro commit, quem fez o quê,
e onde cada requisito da especificação (`INF008-trabalho-pratico.pdf`) foi implementado no
código. Os relatórios anteriores (`RELATORIO_CORRECOES_DOMINIO_BANCO.md` e
`RELATORIO_INTERFACE_JAVAFX.md`) continuam no repositório com o detalhe completo das respectivas
etapas; este arquivo é o resumo geral e o índice de "onde está cada coisa".

- **Tema**: processamento de pedidos em e-commerce, implementado como plugin do microkernel
  fornecido pela disciplina.
- **Dupla**: Pedro Ramos e valdersluz.
- **Prazo de entrega**: 23/07/2026.

## 1. Linha do tempo do desenvolvimento

### Fase 1 — Estruturação e domínio inicial (Pedro Ramos, 28/06 a 16/07/2026)

Commits `13b28bc` → `74bc4e4`. Construção da base do plugin de e-commerce em cima do microkernel:

| Commit | Data | O que foi feito |
|---|---|---|
| `13b28bc` | 28/06 | Primeiro commit do projeto. |
| `cdc4545` | 06/07 | Criação inicial de `Cart` e `Product`. |
| `9e44e91` | 07/07 | Criação inicial de `Order` e `Cart`. |
| `abef6f1` | 10/07 | Estruturação correta do projeto (pacotes); reescrita de `Order`/`OrderItem`/`Cart`/`CartItem`/`Product`/`OrderStatus` e das interfaces `DiscountPolicy`/`Payable`/`ShippingPolicy`. |
| `42d0ddc` | 12/07 | Lógica de `validate()`/`pay()` de `CreditCardPayment`/`PixPayment`/`BankSlipPayment` (mais tarde renomeada para `BoletoPayment`). |
| `2280a4e` | 15/07 | Model finalizado, service quase pronto. |
| `e7c6260` | 16/07 | Services e `ProductRepository` finalizados. |
| `5863a45` | 16/07 | `OrderRepository` e `CartRepository` finalizados. |
| `74bc4e4` | 16/07 | Controllers implementados. |

Ao final desta fase, o projeto tinha o domínio, os services, os repositories e os controllers
implementados, mas **nenhuma tela JavaFX própria** (o plugin ainda era o esqueleto de exemplo do
projeto-base) e a camada de persistência assumia um schema de banco próprio, diferente do banco
de apoio fornecido pelo professor.

### Fase 2 — Correções de domínio + interface JavaFX (sessão com Claude, commit `ea3208d`, 18/07/2026)

Todo o trabalho desta fase foi feito em sessões de par-programação com o Claude Code e consolidado
num único commit (`ea3208d`, autor valdersluz). Duas frentes, cada uma com relatório próprio:

**a) Correções de domínio, exceções e persistência** — detalhe completo em
`RELATORIO_CORRECOES_DOMINIO_BANCO.md`. Resumo:
- Corrigidos bugs de domínio: `Cart.calculateSubtotal()` (ignorava preço unitário),
  `Cart.removeProduct()` (comparação por referência nunca removia nada),
  `Product.hasStock()` (off-by-one), `CartService.addProduct` (duplicava linha em vez de
  atualizar o carrinho), `CreditCardPayment.validate()` (exceção mal tratada, inconsistência de
  status entre formas de pagamento).
- Criadas as exceptions obrigatórias pela especificação: `InsufficientStockException` e
  `InvalidPaymentException`, agora efetivamente lançadas por `CartService`/`OrderService`.
- `BankSlipPayment` renomeada para `BoletoPayment` (nome exigido literalmente pela
  especificação); mensagens e comentários traduzidos de português para inglês; classes mortas
  removidas.
- Camada de persistência **reescrita para usar o schema real do banco de apoio**
  (`db/init/01_schema_seed.sql`) em vez de um schema inventado: estoque derivado de
  `stock_movements`, pagamento gravado em tabela `payments` própria, vínculo opcional com
  `discounts`/`order_discounts`, credenciais de conexão corrigidas.

**b) Interface JavaFX** — detalhe completo em `RELATORIO_INTERFACE_JAVAFX.md`. Resumo:
- Criado o pacote `view/` com `ProductView`, `CustomerView`, `OrderView`, `CheckoutView` e o
  utilitário `AlertUtils`, seguindo a separação de camadas exigida (view → controller → service →
  repository → model), sem regra de negócio nos manipuladores de evento.
- `MyPlugin.java` reescrito para montar manualmente repository → service → controller → view e
  registrar 4 abas + 1 item de menu **só através do `IUIController`** do core, sem nenhuma
  alteração no kernel.
- Aba "Checkout" implementa o fluxo mínimo exigido pela seção 2 da especificação: selecionar
  cliente/produtos, montar carrinho, escolher desconto/frete/pagamento, ver o total calculado em
  tempo real, confirmar pedido.
- Adicionada `NoDiscountPolicy` (terceira implementação de `DiscountPolicy`, desconto zero) para
  permitir finalizar a compra sem cupom nem desconto de estudante sem precisar de `if` na tela.
- Corrigido o empacotamento do plugin: `plugins/myplugin/pom.xml` passou a usar
  `maven-shade-plugin` para gerar um jar "fat" (`MyPlugin.jar`) com HikariCP e o driver MariaDB,
  já que o `PluginController` do core carrega cada plugin isoladamente e não resolve
  dependências Maven.
- Também nesta fase: `docker-compose.yml` e `db/init/01_schema_seed.sql` foram adicionados,
  subindo um MariaDB com os dados de exemplo do domínio (customers, products, stock_movements,
  carts, orders, payments, discounts, shipping_methods).

Ao final desta fase, o build (`mvn install`) passava e a aplicação subia sem exceções, mas
**nunca tinha sido testada de fato contra uma instância real do banco** (Docker não estava
acessível nas sessões anteriores).

### Fase 3 — Verificação end-to-end contra o banco real (sessão atual, 19–20/07/2026, pendente de commit)

Nesta sessão o usuário subiu o container MariaDB (`sudo docker compose up -d`) e o Claude testou
o fluxo de verdade — rodando a aplicação, consultando o banco diretamente e, como não havia
ferramenta de clique automatizado disponível neste ambiente, exercitando o mesmo caminho de
código da UI (`Controller` → `Service` → `Repository`) via pequenos programas Java descartáveis
em `/tmp`. Isso revelou **3 bugs reais que nunca tinham sido pegos**, porque as fases anteriores
nunca chegaram a rodar contra dados reais:

1. **Aba "Orders" não carregava os pedidos de exemplo.** `OrderRepositoryImp.mapRow()`
   fazia `OrderStatus.valueOf(rs.getString("status"))`, mas o banco de apoio usa os literais
   `PAID`/`PAYMENT_FAILED`/`PENDING_PAYMENT`, que não existem no enum `OrderStatus`
   (`PENDING`/`PAID`/`CANCELLED`/`INVALID_PAYMENT`) — `valueOf` lançava
   `IllegalArgumentException` e a lista de pedidos ficava vazia.
   **Correção**: `microkernel/plugins/myplugin/.../repository/OrderRepositoryImp.java` ganhou os
   métodos `orderStatusToDb`/`orderStatusFromDb`, mapeando simetricamente entre o vocabulário do
   banco e o enum, usados em `save()`, `update()` e `mapRow()`.
2. **Um dos 3 pedidos de exemplo quebrava ao carregar.** A tabela `shipping_methods` do banco de
   apoio tem uma 4ª linha (`ECONOMY`) referenciada por um pedido de exemplo, mas só existiam
   classes para `STANDARD`/`EXPRESS`/`PICKUP`.
   **Correção**: nova classe `microkernel/plugins/myplugin/.../shipping/EconomyShippingPolicy.java`
   (mesmo padrão das demais), registrada em `ShippingPolicyFactory.fromType` e
   `PolicyTypeResolver.resolveShippingType`.
3. **Pagamento por cartão de crédito e por boleto nunca funcionavam, com nenhum dado.**
   `CreditCardPayment.validate()` e `BoletoPayment.validate()` usavam
   `DateTimeFormatter.ofPattern(...).withResolverStyle(ResolverStyle.STRICT)` com padrões que
   usam a letra `y` (`"MM/yy"`, `"dd/MM/yyyy"`). Em `STRICT`, o `java.time` resolve esse campo
   como `YearOfEra`, que `YearMonth`/`LocalDate` não conseguem consumir (só aceitam o campo
   proleptic `YEAR`) — **todo** parse lançava `DateTimeParseException`, então `validate()`
   sempre retornava `false`, para qualquer data. Ou seja, de 3 formas de pagamento exigidas pela
   especificação, só `PixPayment` funcionava de fato antes desta correção.
   **Correção**: troca de `ResolverStyle.STRICT` para `ResolverStyle.SMART` em
   `microkernel/plugins/myplugin/.../payment/CreditCardPayment.java` e
   `.../payment/BoletoPayment.java` (SMART faz a conversão YearOfEra→YEAR mas continua rejeitando
   mês/ano fora do intervalo válido).

Todas as três correções foram verificadas contra o banco real (não só compiladas): fluxo
completo de criar carrinho → adicionar produto → aplicar desconto de estudante → frete expresso
→ pagar com cartão de crédito válido → pedido `PAID` com subtotal/desconto/frete/total corretos e
**estoque decrementado de fato no banco**; boleto válido e Pix válido também confirmados `PAID`;
cartão expirado confirmado `INVALID` sem derrubar a aplicação; estoque insuficiente lança
`InsufficientStockException`; pagamento inválido salva o pedido com status de auditoria e lança
`InvalidPaymentException`; cancelamento de um pedido pago devolve o estoque
(`stock_movements` tipo `INBOUND`). A aba "Orders" foi conferida visualmente (screenshot) mostrando
os pedidos com status corretos.

Além dos 3 bugs, nesta fase também foram feitos dois ajustes de higiene/conformidade com a
especificação, sem relação com os bugs acima:
- **`.gitignore` + remoção de artefatos de build do controle de versão**: o commit `ea3208d`
  tinha commitado por engano os diretórios `target/` de todos os módulos e o
  `plugins/MyPlugin.jar`. A especificação exige entrega "sem arquivos `.class`" — foi criado um
  `.gitignore` na raiz (`target/`, `*.class`, `MyPlugin.jar`) e esses arquivos foram removidos do
  índice do git (continuam no disco, e o `MyPlugin.jar` é regerado automaticamente por
  `mvn install`).
- **Typo de nome de classe**: `StandardShinppingPolicy` renomeada para `StandardShippingPolicy`,
  já que a seção 3 da especificação exige esse nome literalmente. Referências atualizadas em
  `ShippingPolicyFactory.java`, `PolicyTypeResolver.java` e `OrderController.java`.

**Nada da Fase 3 foi commitado ainda** — as mudanças estão no working tree aguardando revisão.

## 2. Onde está cada requisito da especificação no código

Todos os caminhos abaixo são relativos a
`microkernel/plugins/myplugin/src/main/java/br/edu/ifba/inf008/plugins/ecommerce/`.

| Requisito da especificação | Onde está |
|---|---|
| Interface `Payable` | `payment/Payable.java` |
| `CreditCardPayment`, `PixPayment`, `BoletoPayment` | `payment/CreditCardPayment.java`, `payment/PixPayment.java`, `payment/BoletoPayment.java` |
| Interface `DiscountPolicy` | `discount/DiscountPolicy.java` |
| `CouponDiscountPolicy`, `StudentDiscountPolicy` (+ `NoDiscountPolicy`, extra p/ "sem desconto") | `discount/CouponDiscountPolicy.java`, `discount/StudentDiscountPolicy.java`, `discount/NoDiscountPolicy.java` |
| Interface `ShippingPolicy` | `shipping/ShippingPolicy.java` |
| `StandardShippingPolicy`, `ExpressShippingPolicy` (+ `PickupShippingPolicy`, `EconomyShippingPolicy`, extras) | `shipping/StandardShippingPolicy.java`, `shipping/ExpressShippingPolicy.java`, `shipping/PickupShippingPolicy.java`, `shipping/EconomyShippingPolicy.java` |
| Composição `Order` possui coleção de `OrderItem` | `model/Order.java`, `model/OrderItem.java` |
| Produto (código/nome/descrição/preço/estoque) | `model/Product.java`; estoque real calculado em `repository/ProductRepositoryImp.java` a partir de `stock_movements` |
| Carrinho, adicionar/remover produto com validação de estoque | `model/Cart.java`, `model/CartItem.java`, `service/CartService.java` |
| Exception de estoque insuficiente | `exception/InsufficientStockException.java` |
| Exception de pagamento inválido | `exception/InvalidPaymentException.java` |
| Hierarquia de exceptions / demais exceptions de apoio | `exception/DomainException.java`, `exception/EntityNotFoundException.java`, `exception/DuplicateEntityException.java`, `exception/RepositoryException.java` |
| Status do pedido (pendente/pago/cancelado/pagamento inválido) | `model/OrderStatus.java`; transição em `model/Order.java#processPayment/convert` e `service/OrderService.java` |
| Redução de estoque só quando o pedido é confirmado | `service/OrderService.java#createOrder` (grava `stock_movements` tipo `OUTBOUND` só no caso `PAID`) |
| Cálculo do total (subtotal − desconto + frete) | `model/Order.java` (`calculateSubtotal`/`calculateDiscount`/`calculateShipping`/`calculateTotal`) |
| Camada de serviços de aplicação | `service/CartService.java`, `service/CustomerService.java`, `service/OrderService.java`, `service/ProductService.java` |
| Camada de persistência/repositório | `repository/*.java` (um `Repository` + `RepositoryImp` por entidade) |
| Camada de controllers (ponte view ↔ service) | `controller/CartController.java`, `controller/CustomerController.java`, `controller/OrderController.java`, `controller/ProductController.java` |
| Camada de interface JavaFX | `view/ProductView.java`, `view/CustomerView.java`, `view/OrderView.java`, `view/CheckoutView.java`, `view/AlertUtils.java` |
| Integração com o microkernel (registro de abas/menus via `IUIController`) | `microkernel/plugins/myplugin/src/main/java/br/edu/ifba/inf008/plugins/MyPlugin.java` |
| Fluxo mínimo exigido (selecionar produtos → carrinho → desconto → frete → pagamento → total) | Aba "Checkout", implementada em `view/CheckoutView.java` |
| Banco de apoio (schema + dados de exemplo) | `db/init/01_schema_seed.sql`, subido via `docker-compose.yml` (raiz do projeto) |
| Conexão com o banco | `repository/ConnectionFactory.java` |

## 3. Estado atual e pendências antes da entrega

- As correções da Fase 3 (bugs de status/frete/pagamento, `.gitignore`, rename do typo) estão no
  working tree, **ainda não commitadas** — revisar (`git status`/`git diff`) e commitar.
- Os testes desta sessão criaram pedidos extras no banco e reduziram estoque de alguns produtos.
  Para gravar o vídeo com os dados de exemplo originais, resetar o banco:
  `sudo docker compose down -v && sudo docker compose up -d` (apaga o volume e recarrega o seed
  do zero).
- Testar manualmente pela interface (a verificação desta sessão foi feita majoritariamente por
  chamadas diretas ao código de serviço/repositório contra o banco real, já que este ambiente não
  tem ferramenta de clique automatizado para dirigir a UI JavaFX).
- Depois: zipar o código-fonte (sem `.class` — já resolvido pelo `.gitignore`), gravar o vídeo de
  até 10 minutos explicando decisões/classes/interfaces/polimorfismo/exceptions/composição, e
  enviar para `sandroandrade@ifba.edu.br` com o assunto "INF008 – <nomes dos alunos>" até
  23/07/2026.
- Ver `COMO_EXECUTAR_O_PROJETO.md` para o passo a passo de como rodar o projeto do zero em outra
  máquina.
