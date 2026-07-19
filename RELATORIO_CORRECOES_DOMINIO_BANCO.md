# Relatório — Correções de Domínio, Exceções e Persistência

Este relatório documenta o trabalho realizado para corrigir os problemas identificados na análise comparativa entre a implementação existente em `microkernel/plugins/myplugin` e a especificação `INF008-trabalho-pratico.pdf`.

## 1. Bugs de domínio corrigidos

| Arquivo | Problema | Correção |
|---|---|---|
| `model/Cart.java` | `calculateSubtotal()` somava apenas as quantidades dos itens, ignorando o preço unitário. | Passou a somar `cartItem.calculateSubtotal()` (preço × quantidade) de cada item. |
| `model/Cart.java` | `removeProduct(Product)` chamava `items.remove(product)` numa `List<CartItem>`; como `CartItem` não sobrescreve `equals`, a comparação nunca era verdadeira e nada era removido. | Passou a usar `items.removeIf(item -> item.getProduct().getProduct_id() == product.getProduct_id())`. |
| `model/Product.java` | `hasStock(quantity)` usava `quantity < getStock()`, recusando a compra exata da quantidade disponível em estoque (off-by-one). | Corrigido para `quantity <= getStock()`. |
| `service/CartService.java` | `addProduct` chamava `cartRepository.save(cart)` em um carrinho que já existia (carregado via `findById`), criando uma nova linha em `carts` a cada produto adicionado, em vez de atualizar o carrinho existente. | Corrigido para `cartRepository.update(cart)`. |
| `payment/CreditCardPayment.java` | `validate()` capturava `RuntimeException` apenas para relançá-la como `RuntimeException`, então uma data de expiração mal formatada explodia sem tratamento em vez de invalidar o pagamento. | Passou a capturar especificamente `DateTimeParseException` e retornar `false`; também passou a verificar se o cartão está expirado (antes só validava o formato da data). |
| `payment/CreditCardPayment.java` | Cartão com dados inválidos resultava em `PaymentStatus.FAILED` (→ `OrderStatus.CANCELLED`), inconsistente com `PixPayment`, que usa `PaymentStatus.INVALID` (→ `OrderStatus.INVALID_PAYMENT`) para o mesmo tipo de problema. | Alinhado para retornar `PaymentStatus.INVALID`. |

## 2. Exceções obrigatórias (ausentes na versão anterior)

A especificação exige, na seção 4, uma exception para estoque insuficiente e uma para pagamento inválido. Nenhuma das duas existia; `CartService.addProduct` simplesmente não fazia nada quando o estoque era insuficiente, e não havia nenhum sinal de erro para pagamento inválido.

- **Criadas:** `exception/InsufficientStockException.java` e `exception/InvalidPaymentException.java` (ambas estendendo `DomainException`).
- **`CartService.addProduct`** agora lança `InsufficientStockException` quando `product.hasStock(quantity)` é falso, em vez de ignorar silenciosamente.
- **`OrderService.createOrder`** agora lança `InvalidPaymentException` quando o processamento do pagamento resulta em `OrderStatus.INVALID_PAYMENT`. O pedido ainda é salvo (com esse status, para efeito de auditoria/histórico) antes da exceção ser lançada.

## 3. Nomenclatura e idioma

- `BankSlipPayment` foi renomeada para **`BoletoPayment`**, conforme exigido literalmente pela especificação (seção 3: "CreditCardPayment, PixPayment e BoletoPayment"). Todas as referências foram atualizadas (`PayableFactory`, `PolicyTypeResolver`, `OrderRepositoryImp`, `OrderController`), incluindo o valor técnico armazenado no banco (`"BANK_SLIP"` → `"BOLETO"`, que também é o valor usado nos dados de exemplo em `payments.payment_method`).
- Mensagens de exceção em português em `PolicyTypeResolver` ("Tipo de desconto não mapeado", etc.) foram traduzidas para inglês.
- Comentário em português em `PayableFactory` (`// cvv=0, não persistido`) foi traduzido.
- Classes vazias e não utilizadas `ecommerce.database.ConnectionFactory`, `DatabaseConfig` e `HikariCPConfig` (sobras de um refactor incompleto, sem nenhuma referência no restante do código) foram removidas.

## 4. Alinhamento com o banco de apoio fornecido

Este foi o ponto mais crítico encontrado na análise: a camada de persistência assumia um schema próprio (`products.product_id/price/stock`, `customers.customer_id/name`, `carts` sem colunas, `orders.discount_policy_type/payment_card_number/...`) que **não existe** em `db/init/01_schema_seed.sql` — o banco de apoio real, cujas tabelas batem exatamente com as citadas na seção 6 da especificação. Além disso, as credenciais estavam hardcoded incorretamente (`bancoMDB`/`root`/`INF008`, em vez de `ecommerce_inf008`/`inf008`/`inf008` do `docker-compose.yml`).

Diante disso ([decisão confirmada com o usuário](#decisão) antes da implementação), a camada Java foi reescrita para se adequar ao schema fornecido, em vez de inventar um schema próprio.

### 4.1 Produtos (`products` + `stock_movements`)

- `Product` ganhou o campo `sku` (coluna obrigatória e única em `products`).
- O schema fornecido **não tem coluna de estoque** em `products`; o estoque é derivado de `stock_movements` (tipos `INBOUND`, `OUTBOUND`, `RESERVED`). `ProductRepositoryImp` agora calcula o estoque disponível via `SUM(INBOUND) - SUM(OUTBOUND) - SUM(RESERVED)` em todas as consultas (`findById`, `findByName`, `findAll`).
- Novo método `ProductRepository.registerStockMovement(productId, movementType, quantity, reason)`, que insere uma linha em `stock_movements` em vez de tentar sobrescrever uma coluna inexistente.
- `OrderService` foi ajustado: confirmar um pedido agora grava um movimento `OUTBOUND` ("Order confirmed"); cancelar um pedido pago grava um movimento `INBOUND` ("Order cancelled").
- `update(Product)` agora só atualiza `sku`/`name`/`description`/`unit_price` — estoque nunca é escrito diretamente, apenas via movimentos, coerente com a regra "reduzir o estoque apenas quando o pedido for confirmado".
- `delete(Product)` agora remove os `stock_movements` do produto na mesma transação antes de remover o produto (senão a FK impediria a exclusão).

### 4.2 Clientes (`customers`)

- `Customer` ganhou o campo `customerType` (coluna obrigatória `customer_type`).
- SQL migrado para as colunas reais (`id`, `full_name`, `email`, `customer_type`).

### 4.3 Carrinho (`carts` + `cart_items`)

- `Cart` ganhou `customerId` (FK obrigatória `customer_id`) e `status` (`OPEN`/`CONVERTED`).
- **Não existia nenhuma forma de criar um carrinho** na versão anterior (só havia operações sobre um `cart_id` já existente). Foi adicionado `CartService.createCart(customerId)` / `CartController.createCart(customerId)`.
- Ao confirmar um pedido, o carrinho de origem agora é marcado como `CONVERTED` além de esvaziado.

### 4.4 Pedidos (`orders` + `order_items` + `payments` + `order_discounts`)

Reescrita completa de `OrderRepositoryImp`:

- `Order` ganhou `customerId` e `cartId`, preenchidos a partir do carrinho de origem em `OrderService.createOrder`.
- `shipping_method_id` (FK obrigatória em `orders`) é resolvido em tempo de gravação a partir do código da política de frete (`STANDARD`/`EXPRESS`/`PICKUP`) via consulta a `shipping_methods`.
- O pagamento passou a ser gravado na tabela **separada** `payments` (`payment_method`, `status`, `amount`, `failure_reason`, `paid_at`), como no schema fornecido — antes esses dados eram gravados (incorretamente) em colunas inexistentes na própria tabela `orders`.
- Vínculo best-effort com `order_discounts`/`discounts`: quando o desconto aplicado corresponde a um `code` conhecido na tabela `discounts` (`WELCOME10` para cupom, `STUDENT15` para desconto de estudante), uma linha é gravada em `order_discounts` para fins de auditoria. Quando não há correspondência (ex.: um código de cupom arbitrário digitado pelo usuário), o desconto continua sendo aplicado normalmente — ele já está persistido em `orders.discount_total` — apenas o vínculo auditável extra é omitido.

**Limitação inerente a essa escolha de schema (documentada no Javadoc da classe):** o banco fornecido não guarda os detalhes do meio de pagamento (número de cartão, chave Pix, código de barras) nem qual `DiscountPolicy` exata foi usada — apenas os totais já calculados. Por isso, ao carregar um pedido do banco (`find`/`findAll`), os campos `discountPolicy` e `paymentMethod` do objeto `Order` voltam como `null`; os valores de `subtotal`/`discount`/`shippingCost`/`total`/`status` persistidos são a fonte de verdade para pedidos históricos. Isso significa que **a tela deve usar os getters (`getTotal()`, `getDiscount()`, etc.), nunca os métodos `calculateX()`, ao exibir um pedido já salvo** — usar `calculateX()` em um pedido recarregado causaria `NullPointerException`, pois não há uma política viva para chamar. A `ShippingPolicy`, por outro lado, é reconstruída corretamente a partir do código salvo em `shipping_methods`.

### 4.5 Credenciais de conexão

`ConnectionFactory` corrigida para apontar para `ecommerce_inf008` com usuário/senha `inf008`/`inf008`, batendo com o `docker-compose.yml`.

## 5. Verificação

- `mvn install` na raiz do `microkernel` (todos os módulos: `interfaces`, `app`, `plugins/myplugin`) — **BUILD SUCCESS**, 4/4 testes existentes passando.
- Nenhum teste automatizado cobre especificamente a camada de e-commerce (não havia testes antes desta correção e nenhum foi solicitado neste momento).
- Não foi possível validar as consultas SQL contra uma instância real do MariaDB nesta sessão (Docker não estava acessível no ambiente). **Recomenda-se rodar `docker compose up -d` e exercitar o fluxo manualmente antes da gravação do vídeo de entrega**, para confirmar as queries linha a linha contra os dados de exemplo.

## Decisão

A abordagem de adequar o código Java ao schema fornecido (em vez de substituir o schema por um próprio, ou apenas corrigir as credenciais e deixar as queries quebradas) foi confirmada explicitamente com o usuário antes da implementação, por ser a mais fiel ao "banco de apoio" fornecido pelo professor e ao que provavelmente será testado na avaliação.
