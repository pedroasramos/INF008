# Relatório — Interface JavaFX do Plugin de E-commerce

Este relatório documenta a construção da interface gráfica JavaFX que estava completamente ausente: `MyPlugin.java` continuava sendo o esqueleto de exemplo do projeto-base (um menu que só imprimia no console e uma aba com um retângulo azul), sem nenhuma tela chamando o código de domínio/serviço/repositório já implementado.

## 1. Arquitetura adotada

Foi criado o pacote `br.edu.ifba.inf008.plugins.ecommerce.view`, respeitando a separação de camadas exigida na seção 3 da especificação:

```
view (JavaFX, novo) → controller (integração) → service (regras de negócio) → repository (persistência) → model (domínio)
```

Os manipuladores de evento das telas **apenas** chamam métodos dos `Controller`s existentes, convertem/validam entrada de formulário e atualizam widgets — nenhuma regra de negócio foi escrita nos manipuladores, conforme exigido ("Evitar regras de negócio diretamente em manipuladores de eventos do JavaFX").

### Arquivos criados

| Arquivo | Responsabilidade |
|---|---|
| `view/AlertUtils.java` | Utilitário para exibir `Alert` de erro/informação de forma consistente em todas as telas. |
| `view/ProductView.java` | Aba "Products": tabela de produtos (SKU, nome, descrição, preço, estoque) + formulário de cadastro + exclusão. |
| `view/CustomerView.java` | Aba "Customers": tabela de clientes (id, nome, email, tipo) + formulário de cadastro + exclusão. |
| `view/OrderView.java` | Aba "Orders": tabela de pedidos (id, cliente, status, subtotal, desconto, frete, total) + cancelar/excluir pedido selecionado. |
| `view/CheckoutView.java` | Aba "Checkout" — o fluxo mínimo exigido pela especificação (item 2). Ver detalhamento abaixo. |

### Arquivos modificados para suportar a UI

- **`MyPlugin.java`**: reescrito por completo. Monta manualmente as instâncias de repositório → serviço → controller → view (o projeto não usa nenhum framework de injeção de dependência) e registra 4 abas mais 1 item de menu ("E-commerce → Refresh data") **exclusivamente através do `IUIController`** exposto pelo core (`createTab`, `createMenuItem`), sem nenhuma alteração no core/interfaces — não foi necessária nenhuma extensão do kernel para isso.
- **`controller/OrderController.java`**: os dois métodos redundantes `createOrderWithCoupon`/`createOrderWithStudentDiscount` foram substituídos por um único `placeOrder(cartId, discountType, couponCode, shippingType, paymentMethod)`, necessário para a tela oferecer também a opção "sem desconto" (que não existia antes).
- **`discount/NoDiscountPolicy.java`** (novo): terceira implementação de `DiscountPolicy`, retornando sempre 0 — permite ao usuário finalizar uma compra sem cupom nem desconto de estudante sem precisar de um `if` na tela para "pular" o desconto (mantém o polimorfismo em vez de um caso especial).
- **`OrderService.createOrder`** passou a **retornar** o `Order` criado (antes era `void`), para a tela poder exibir o número do pedido, o status final e o total ao usuário após a confirmação.
- **`repository/ConnectionFactory.java`**: adicionado `config.setInitializationFailTimeout(-1)`. Por padrão o HikariCP tenta obter uma conexão já na inicialização e falha rápido (lançando erro na carga estática da classe) se o banco não estiver acessível — isso derrubaria a aplicação inteira na primeira tela que consultasse dados, antes mesmo de qualquer `try/catch` da UI conseguir capturar algo. Com essa configuração, a falha de conexão só aparece no primeiro uso real (`getConnection()`), momento em que já existe tratamento (`RepositoryException` → `Alert` de erro) em todas as views.

## 2. Fluxo da aba "Checkout" (requisito mínimo da seção 2)

Implementa exatamente a sequência pedida: *selecionar produtos, montar carrinho, escolher desconto, escolher frete, escolher pagamento e visualizar o total final*.

1. **Cliente**: `ComboBox` com os clientes cadastrados + botão "New Cart" (chama `CartController.createCart`, que não existia antes desta etapa — ver relatório de correções).
2. **Produtos**: `ComboBox` de produtos (mostrando nome, SKU e estoque) + campo de quantidade + "Add to Cart" (`CartController.addProductToCart`). Estoque insuficiente aparece como `Alert` com a mensagem da `InsufficientStockException`, não como um crash.
3. **Tabela do carrinho**: itens atuais com quantidade, preço unitário e subtotal; botão para remover item selecionado.
4. **Desconto**: `ComboBox` (`NONE`/`COUPON`/`STUDENT`) + campo de código de cupom (habilitado só quando `COUPON` é selecionado).
5. **Frete**: `ComboBox` (`STANDARD`/`EXPRESS`/`PICKUP`).
6. **Pagamento**: `ComboBox` (`CREDIT_CARD`/`PIX`/`BOLETO`) com os campos específicos de cada meio de pagamento aparecendo/desaparecendo dinamicamente (número/titular/CVV/validade do cartão; chave Pix; código de barras/vencimento do boleto).
7. **Totais em tempo real**: a cada mudança de desconto, cupom ou frete, os labels de subtotal/desconto/frete/total são recalculados chamando diretamente `Order.calculateSubtotal()/calculateDiscount()/calculateShipping()/calculateTotal()` sobre um pedido "prévia" (não persistido) — ou seja, a tela demonstra o cálculo polimórfico exigido na seção 7 (`DiscountPolicy`/`ShippingPolicy` reais são usados, não uma fórmula reescrita na view).
8. **Confirmar pedido**: monta o `Payable` concreto via `OrderController.buildCreditCardPayment/buildPixPayment/buildBoletoPayment` e chama `OrderController.placeOrder(...)`. O resultado é tratado assim:
   - Sucesso (`PAID`/`PENDING`/`CANCELLED`): `Alert` informativo com número do pedido, status e total; carrinho é limpo na tela; produtos e pedidos são recarregados (estoque pode ter mudado).
   - `InvalidPaymentException`: `Alert` de erro explicando que o pagamento não foi confirmado; o carrinho **é mantido** na tela (o pedido já foi salvo com status `INVALID_PAYMENT` no banco, mas os itens continuam disponíveis para o usuário tentar outro meio de pagamento).
   - Qualquer outra `DomainException`/`RuntimeException`: capturada e exibida como mensagem compreensível, sem encerrar a aplicação — atendendo o requisito da seção 4.

## 3. Problema de empacotamento descoberto e corrigido

Ao testar a aplicação de fato (não apenas compilar), a UI subiu, mas o carregamento de qualquer tela que tocasse o banco falhava com `NoClassDefFoundError: com/zaxxer/hikari/HikariConfig`.

**Causa raiz:** `PluginController.init()` (código do core, não alterado) carrega cada plugin com um `URLClassLoader` isolado, apontando só para o `.jar` do próprio plugin em `./plugins/` — ele não resolve as dependências Maven do plugin. Como `MyPlugin.jar` era um jar "fino" (só as classes do próprio módulo), `HikariCP` e o driver do MariaDB, declarados como dependências no `pom.xml`, nunca eram incluídos no artefato final. Isso já era um problema antes desta etapa — só não havia sido descoberto porque `MyPlugin.java` nunca chamava nenhum código que tocasse o banco.

**Correção:** `plugins/myplugin/pom.xml` passou a usar o `maven-shade-plugin` para gerar um jar "fat" (`MyPlugin.jar` já inclui `HikariCP`, `mariadb-java-client` e `slf4j-api`), mantendo a saída no mesmo caminho (`plugins/MyPlugin.jar`) esperado pelo `PluginController`. A dependência `interfaces` foi marcada como `provided`, já que suas classes (`IPlugin`, `ICore`, etc.) já estão no classloader pai (compartilhado com a aplicação principal) — embutir uma segunda cópia geraria duplicidade de classes sem necessidade.

Essa é uma mudança **apenas no build do plugin**, sem tocar a arquitetura do core.

## 4. Verificação realizada

- `mvn install` na raiz do `microkernel` — **BUILD SUCCESS**, 4/4 testes existentes passando, `MyPlugin.jar` gerado com as ~65 classes do plugin (antes só continha o esqueleto de 2 classes).
- `mvn exec:java -pl app` foi executado de fato (display X11 disponível no ambiente). A aplicação **iniciou sem exceções no console** e sem crash — não houve nenhum stack trace no log, condizente com o caminho esperado de erro tratado (não havia MariaDB acessível neste ambiente — porta 3306 fechada, Docker sem permissão —, então a tentativa de carregar produtos deve ter caído no `catch (RuntimeException)` de `ProductView.refresh()`, exibindo um `Alert` modal em vez de travar a aplicação).
- **Não foi possível tirar um screenshot** ou confirmar visualmente o layout renderizado nesta sessão (não há ferramenta de captura de tela disponível neste ambiente de execução). A verificação ficou limitada a: build limpo, ausência de exceções no log de inicialização, e revisão manual do código de cada tela.

### Pendências recomendadas antes da entrega

1. Rodar `docker compose up -d` e testar o fluxo completo manualmente (cadastrar/listar produto, criar cliente, montar carrinho, aplicar cada tipo de desconto/frete/pagamento, confirmar um pedido com sucesso e um com pagamento inválido) para confirmar visualmente que as telas e o layout ficaram como esperado.
2. Testar especificamente o caso de estoque insuficiente e o caso de cartão de crédito com data expirada, para ver as mensagens de erro na tela.
3. Considerar gravar o vídeo de entrega já com o banco no ar, mostrando o fluxo ponta a ponta.
