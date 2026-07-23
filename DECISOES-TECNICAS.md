# Decisões Técnicas do Projeto — E-commerce sobre Microkernel

> Documento de apoio ao item 8 do enunciado (INF008-trabalho-pratico.pdf): descreve as
> principais decisões, classes e responsabilidades da solução, e indica interfaces,
> classes polimórficas, exceptions e pontos de composição usados — conteúdo que serve de
> roteiro para o vídeo de entrega.

## 1. Visão geral da arquitetura

O projeto é um **microkernel** com três módulos Maven (`microkernel/pom.xml`):

- **`interfaces`** — contratos do *core* (`ICore`, `IUIController`, `IAuthenticationController`,
  `IIOController`, `IPluginController`, `IPlugin`). Não foi alterado: todos os requisitos do
  trabalho foram atendidos usando exatamente as interfaces originais, sem extensão do core.
- **`app`** — implementação do *shell* (`Core`, `UIController`, `AuthenticationController`,
  `IOController`, `PluginController`) e classe `App` (ponto de entrada). O `PluginController`
  carrega dinamicamente arquivos `.jar` da pasta `plugins/` via `URLClassLoader` e invoca
  `IPlugin.init()` de cada um.
- **`plugins/myplugin`** — o plugin de e-commerce (`MyPlugin`), único plugin entregue,
  concentrando toda a funcionalidade pedida no enunciado dentro do pacote
  `br.edu.ifba.inf008.plugins.ecommerce`.

Como nenhuma extensão do core foi necessária, não há nada a justificar nesse quesito: os
serviços expostos por `IUIController` (`createMenuItem`, `createTab`) já eram suficientes para
registrar as quatro abas e o menu do plugin.

### 1.1 Como o plugin se conecta ao core

`MyPlugin.init()` (`plugins/myplugin/.../plugins/MyPlugin.java`) é o ponto de integração:

1. Obtém `IUIController` via `ICore.getInstance().getUIController()`.
2. Monta manualmente a cadeia de dependências (repository → service → controller → view),
   caracterizando **injeção de dependência via construtor**, sem framework.
3. Registra as abas **Checkout**, **Products**, **Customers** e **Orders** com
   `uiController.createTab(...)`.
4. Registra o item de menu **E-commerce → Refresh data** com `uiController.createMenuItem(...)`.

## 2. Separação de camadas (pacotes)

| Pacote | Responsabilidade |
|---|---|
| `model` | Entidades de domínio: `Product`, `Cart`, `CartItem`, `Order`, `OrderItem`, `Customer`, `OrderStatus`. Sem dependência de UI ou de banco. |
| `discount` | Estratégias de desconto: interface `DiscountPolicy` + implementações + `DiscountPolicyFactory`. |
| `shipping` | Estratégias de frete: interface `ShippingPolicy` + implementações + `ShippingPolicyFactory`. |
| `payment` | Estratégias de pagamento: interface `Payable` + implementações + `PayableFactory`, enum `PaymentStatus`. |
| `exception` | Hierarquia de exceptions de domínio e de persistência. |
| `repository` | Acesso a dados (JDBC/HikariCP) atrás de interfaces (`ProductRepository`, `CustomerRepository`, `CartRepository`, `OrderRepository`) e utilitário `PolicyTypeResolver`. |
| `service` | Regras de negócio/aplicação: `ProductService`, `CustomerService`, `CartService`, `OrderService`. |
| `controller` | Fachada fina entre a view e os services: `ProductController`, `CustomerController`, `CartController`, `OrderController`. |
| `view` | Telas JavaFX: `ProductView`, `CustomerView`, `OrderView`, `CheckoutView`, utilitário `AlertUtils`. |

Essa separação atende diretamente ao requisito "separar classes de domínio, serviços de
aplicação, interface JavaFX, persistência/repositório e integração com o microkernel" (item 3
do enunciado). As views **não contêm regra de negócio**: validam apenas entrada de formulário
(ex.: `NumberFormatException` ao converter texto para número) e delegam tudo mais a
controller → service → domain.

## 3. Interfaces e classes polimórficas (Requisito 3 e 7)

As três interfaces de estratégia exigidas pelo enunciado foram implementadas como
`interface` Java simples, cada uma recebendo o `Order` como parâmetro (permitindo que a regra
consulte itens, subtotal etc.):

### 3.1 `Payable` (pacote `payment`)

```java
public interface Payable {
    PaymentStatus pay(double amount);
    boolean validate();
}
```

| Implementação | Regra de validação | Resultado possível |
|---|---|---|
| `CreditCardPayment` | Verifica validade (`expirationDate` não vencida, formato `MM/yy`) | `PAID`, `FAILED` (valor ≤ 0), `INVALID` |
| `PixPayment` | Valida a chave Pix (CPF, CNPJ, e-mail, telefone ou chave aleatória UUID) | `PAID`, `FAILED`, `INVALID` |
| `BoletoPayment` | Verifica vencimento (`dueDate` futura) e tamanho do código de barras (47/48 dígitos) | `PAID`, `FAILED`, `PENDING` (quando inválido, em vez de `INVALID`) |

Cada classe encapsula sua própria regra de validação — é o polimorfismo pedido para
"processar pagamentos ... usando polimorfismo para encapsular as regras de cada forma de
pagamento". `Order.processPayment()` chama `paymentMethod.pay(...)` sem conhecer o tipo
concreto e converte o `PaymentStatus` resultante em `OrderStatus`.

### 3.2 `DiscountPolicy` (pacote `discount`)

```java
public interface DiscountPolicy {
    double calculateDiscount(Order order);
}
```

| Implementação | Regra |
|---|---|
| `CouponDiscountPolicy` | 10% sobre o subtotal, associada a um código de cupom |
| `StudentDiscountPolicy` | 15% sobre o subtotal |
| `NoDiscountPolicy` | 0 — usada como *null object* quando nenhum desconto é escolhido |

### 3.3 `ShippingPolicy` (pacote `shipping`)

```java
public interface ShippingPolicy {
    double calculateShipping(Order order);
}
```

| Implementação | Custo fixo |
|---|---|
| `StandardShippingPolicy` | R$ 20,00 |
| `ExpressShippingPolicy` | R$ 45,00 |
| `PickupShippingPolicy` | R$ 0,00 (retirada) |
| `EconomyShippingPolicy` | R$ 15,00 (extra, além do mínimo de duas exigido) |

Em todos os três casos, `Order` guarda apenas a **referência à interface** (não ao tipo
concreto), e escolhe dinamicamente em tempo de execução qual objeto injetar — é o ponto
central de polimorfismo do trabalho, evitando cadeias de `if/else` ou `switch` para aplicar a
regra de negócio (o `switch` que existe em `OrderController`/`CheckoutView`/`*Factory` serve
apenas para **traduzir a escolha do usuário/banco na instância concreta**, não para executar a
regra em si — a regra sempre roda por *dispatch* polimórfico dentro da própria classe).

### 3.4 Fábricas (Factory) por estratégia

- `DiscountPolicyFactory.fromRow(type, coupon)`
- `ShippingPolicyFactory.fromType(type)`
- `PayableFactory.fromRow(type, ...)`
- `PolicyTypeResolver` — caminho inverso: dado um objeto `DiscountPolicy`/`ShippingPolicy`/`Payable`,
  resolve o código textual (`instanceof`) usado para persistir no banco.

Essas fábricas concentram o único ponto do código onde o tipo concreto é decidido a partir de
uma string (entrada do usuário ou coluna do banco), mantendo o restante do sistema
programado contra as interfaces.

## 4. Composição (Requisito 3 e critério de avaliação de 1,0 ponto)

| Classe composta | Componentes | Observação |
|---|---|---|
| `Order` | `List<OrderItem>`, `DiscountPolicy`, `ShippingPolicy`, `Payable` | Composição exigida explicitamente pelo enunciado (`Order` possui uma coleção de `OrderItem`); as três estratégias também são compostas por referência de interface (Strategy Pattern). |
| `Cart` | `List<CartItem>` | Espelha a composição de `Order`/`OrderItem`, antes da conversão em pedido. |
| `CartItem` / `OrderItem` | `Product` | Um item referencia o produto e guarda preço/quantidade capturados no momento da adição (evita que uma alteração futura de preço distorça pedidos já feitos). |
| `MyPlugin` | Repositories, Services, Controllers, Views | Composição em nível de módulo: o plugin monta manualmente o grafo de objetos (não há um *DI container*), reforçando a separação de camadas. |
| `CheckoutView` | `ProductController`, `CustomerController`, `CartController`, `OrderController`, `Runnable onOrderPlaced` | View não estende nenhum controller — ela os recebe por composição (injeção via construtor) e delega toda regra de negócio a eles. |

`Order.addItem(CartItem)` é o método que materializa a composição, convertendo cada
`CartItem` do carrinho em um `OrderItem` do pedido (novo objeto, não referência
compartilhada).

## 5. Exceptions (Requisito 4 e critério de 1,0 ponto)

Hierarquia usada:

```
RuntimeException
 ├── DomainException                 (base das exceptions de regra de negócio)
 │    ├── DuplicateEntityException   (nome de produto / e-mail de cliente já existentes)
 │    ├── EntityNotFoundException    (produto/cliente/carrinho/pedido inexistente)
 │    ├── InsufficientStockException (estoque insuficiente — requisito obrigatório)
 │    └── InvalidPaymentException    (pagamento inválido — requisito obrigatório)
 └── RepositoryException             (falhas de infraestrutura/JDBC, camada de persistência)
```

- **`InsufficientStockException`** é lançada em `CartService.addProduct(...)` quando
  `!product.hasStock(quantity)`, comparando a quantidade pedida com o estoque calculado a
  partir dos `stock_movements`.
- **`InvalidPaymentException`** é lançada em `OrderService.createOrder(...)` quando
  `order.getStatus() == OrderStatus.INVALID_PAYMENT` após `order.processPayment()` — ou seja,
  quando o objeto `Payable` concreto reporta `PaymentStatus.INVALID`.
- **`RepositoryException`** encapsula toda `SQLException` das classes `*RepositoryImp`,
  isolando a camada de persistência das camadas superiores (elas nunca precisam tratar
  `SQLException` diretamente).

**Tratamento na camada adequada:** todas as views (`ProductView`, `CustomerView`, `OrderView`,
`CheckoutView`) capturam `DomainException` (mensagem amigável) e, por segurança,
`RuntimeException` genérica (mensagem "Unexpected error: ..."), exibindo o erro com
`AlertUtils.showError(...)` — um `Alert` do JavaFX — em vez de deixar a exceção subir e
encerrar a aplicação. Isso cumpre o requisito "apresentando mensagens compreensíveis na
interface JavaFX sem encerrar a aplicação abruptamente".

## 6. Cálculo do total do pedido (Requisito 7)

Implementado inteiramente em `Order` (`model/Order.java`), delegando a cada estratégia:

```java
public double calculateTotal(){
    total = (calculateSubtotal() - calculateDiscount()) + calculateShipping();
    return total;
}
```

- `calculateSubtotal()` soma `OrderItem.calculateSubtotal()` (`unitPrice * quantity`) de cada item.
- `calculateDiscount()` delega a `discountPolicy.calculateDiscount(this)`.
- `calculateShipping()` delega a `shippingPolicy.calculateShipping(this)`.
- `processPayment()` delega a `paymentMethod.pay(calculateTotal())` e converte o
  `PaymentStatus` retornado em `OrderStatus` (`PAID`, `PENDING`, `CANCELLED`, `INVALID_PAYMENT`).

A tela **Checkout** (`CheckoutView.recalculate()`) usa esse mesmo método para exibir uma
prévia do total em tempo real, conforme o usuário muda desconto/frete — sem duplicar a
fórmula na UI.

## 7. Regra de baixa de estoque

A baixa de estoque só ocorre quando o pedido é confirmado (`OrderStatus.PAID`), dentro de
`OrderService.createOrder(...)`:

```java
case PAID:
    for (CartItem cartItem : cart.getItems()) {
        product.decreaseStock(cartItem.getQuantity());
        productRepository.registerStockMovement(product.getProduct_id(), "OUTBOUND", ...);
    }
    cart.clear();
    cart.setStatus("CONVERTED");
```

O estoque em si não é uma coluna simples: é **derivado** da soma de `stock_movements`
(`INBOUND` soma, `OUTBOUND`/`RESERVED` subtraem — ver `ProductRepositoryImp.SELECT_WITH_STOCK`).
Isso cria um histórico auditável de entradas/saídas e evita problemas de concorrência típicos
de UPDATE direto em contador. `OrderService.cancelOrder(...)` faz o caminho inverso
(`INBOUND`) quando um pedido `PAID` é cancelado.

## 8. Persistência (pasta `db` e pacote `repository`)

- Banco MariaDB (`ConnectionFactory`, pool HikariCP), schema em `db/init/01_schema_seed.sql`
  com as tabelas `customers`, `products`, `stock_movements`, `carts`, `cart_items`,
  `shipping_methods`, `discounts`, `orders`, `order_items`, `order_discounts`, `payments` —
  todas em inglês, conforme exigido.
- Cada agregado de domínio tem uma interface de repositório e uma implementação JDBC
  (`ProductRepository`/`ProductRepositoryImp`, idem para `Customer`, `Cart`, `Order`),
  seguindo o padrão Repository e mantendo o domínio livre de SQL.
- `OrderRepositoryImp` grava a estratégia de frete como código (`shipping_methods.code`) e
  reconstrói `ShippingPolicy` na leitura via `ShippingPolicyFactory`. Como o schema de apoio
  fornecido não guarda o instrumento de pagamento nem o cupom aplicado, `discountPolicy` e
  `paymentMethod` **não são reconstituídos** na leitura de um pedido histórico — os valores
  já calculados (`subtotal`, `discount_total`, `shipping_total`, `grand_total`, `status`)
  são a fonte da verdade para pedidos antigos (documentado no Javadoc da classe).
- `orderStatusToDb`/`orderStatusFromDb` fazem a tradução entre o vocabulário do enum
  `OrderStatus` do domínio e o vocabulário usado no dado de exemplo do banco
  (`PENDING_PAYMENT`, `PAYMENT_FAILED`), mantendo o enum de domínio desacoplado do dado
  seed fornecido pela disciplina.

## 9. Resumo dos pontos pedidos no item 8 do enunciado

| Item pedido | Onde está |
|---|---|
| Interfaces | `Payable`, `DiscountPolicy`, `ShippingPolicy` (domínio); `ICore`, `IUIController`, `IAuthenticationController`, `IIOController`, `IPluginController`, `IPlugin` (microkernel); `ProductRepository`, `CustomerRepository`, `CartRepository`, `OrderRepository` (persistência) |
| Classes polimórficas | `CreditCardPayment`/`PixPayment`/`BoletoPayment`; `CouponDiscountPolicy`/`StudentDiscountPolicy`/`NoDiscountPolicy`; `StandardShippingPolicy`/`ExpressShippingPolicy`/`PickupShippingPolicy`/`EconomyShippingPolicy` |
| Exceptions | `DomainException` (base), `DuplicateEntityException`, `EntityNotFoundException`, `InsufficientStockException`, `InvalidPaymentException`, `RepositoryException` |
| Pontos de composição | `Order` ⟶ `List<OrderItem>` + `DiscountPolicy`/`ShippingPolicy`/`Payable`; `Cart` ⟶ `List<CartItem>`; `CartItem`/`OrderItem` ⟶ `Product`; `MyPlugin` ⟶ grafo repository/service/controller/view |
| Extensão do core | Nenhuma foi necessária — `ICore`/`IUIController` originais bastaram |
