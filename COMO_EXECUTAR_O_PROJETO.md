# Como Executar o Projeto

Guia para rodar o projeto do zero em outra máquina (professor, colega de equipe, etc.).

## Pré-requisitos

- **JDK 11 ou superior** (testado com JDK 21). Verifique com `java -version`.
- **Maven 3.6+**. Verifique com `mvn -version`.
- **Docker + Docker Compose** para subir o banco de apoio (ou uma instância MariaDB própria —
  veja a alternativa na seção 1).
- **Ambiente gráfico** (X11/Wayland no Linux, ou o desktop normal no Windows/macOS). A aplicação
  é JavaFX e abre uma janela — não funciona em SSH puro sem encaminhamento de X (`ssh -X`) ou
  similar.
- Não é preciso instalar o JavaFX separadamente: ele é resolvido automaticamente pelo Maven
  (`org.openjfx:javafx-controls:17`, declarado em `microkernel/interfaces/pom.xml`), já com os
  binários nativos corretos para Windows/macOS/Linux.

## Passo 1 — Subir o banco de dados de apoio

Na raiz do projeto (onde está o `docker-compose.yml`):

```bash
docker compose up -d
```

(em algumas instalações Linux é preciso `sudo docker compose up -d`, dependendo de o usuário
estar ou não no grupo `docker`).

Isso sobe um container MariaDB (`inf008-ecommerce-mariadb`) na porta `3306`, já inicializado com
o schema e os dados de exemplo em `db/init/01_schema_seed.sql` (banco `ecommerce_inf008`,
usuário `inf008`, senha `inf008`).

Confirme que subiu:

```bash
docker compose ps
```

Deve mostrar o container com status `running`/`healthy`.

### Alternativa sem Docker

Se não puder usar Docker, suba um MariaDB (ou MySQL compatível) manualmente e importe o schema:

```bash
mysql -u root -p < db/init/01_schema_seed.sql
```

Crie um banco `ecommerce_inf008` com um usuário `inf008`/senha `inf008` com permissão sobre ele
(ou ajuste `microkernel/plugins/myplugin/src/main/java/br/edu/ifba/inf008/plugins/ecommerce/repository/ConnectionFactory.java`
com as credenciais que preferir). A aplicação espera a porta `3306` por padrão.

## Passo 2 — Compilar o projeto

```bash
cd microkernel
mvn install
```

Isso compila os três módulos (`interfaces`, `app`, `plugins/myplugin`), roda os testes
automatizados existentes e empacota o plugin como `plugins/MyPlugin.jar` (um jar "fat", com o
driver do MariaDB e o HikariCP embutidos — necessário porque o microkernel carrega cada plugin em
um classloader isolado, sem resolver dependências Maven). Deve terminar com `BUILD SUCCESS`.

## Passo 3 — Executar a aplicação

Ainda dentro de `microkernel/`:

```bash
mvn exec:java -pl app
```

Abre a janela JavaFX "Hello World!" com o menu "E-commerce" e quatro abas: **Checkout**,
**Products**, **Customers** e **Orders**.

Se o banco não estiver acessível, a aplicação não trava: as telas mostram uma caixa de erro
(`Alert`) explicando o problema em vez de derrubar o processo.

## Passo 4 — Testar o fluxo mínimo

Pela aba **Checkout**:

1. Escolher um cliente existente no combo (ou "New Cart" para criar um carrinho novo).
2. Escolher um produto e quantidade, "Add to Cart".
3. Escolher desconto (nenhum / cupom / estudante), frete (padrão / expresso / retirada) e forma
   de pagamento (cartão de crédito / Pix / boleto), preenchendo os campos específicos de cada
   forma de pagamento.
4. Conferir o total calculado em tempo real e clicar em "Confirm Order".
5. Ver o resultado (pedido pago, pendente ou pagamento inválido) e conferir o pedido criado na
   aba **Orders**.

Para ver os dados de exemplo já carregados pelo banco de apoio, basta abrir as abas **Products**,
**Customers** e **Orders** sem fazer nada — elas já vêm populadas.

## Problemas comuns

| Sintoma | Causa provável |
|---|---|
| Tela mostra `Alert` de erro ao abrir Products/Customers/Orders | Banco não está no ar — confira `docker compose ps` e a porta 3306. |
| `mvn exec:java` falha ao subir | Verifique se o `mvn install` anterior terminou com `BUILD SUCCESS` (o plugin depende do `plugins/MyPlugin.jar` gerado nesse passo). |
| Porta 3306 já em uso | Outro MySQL/MariaDB já está rodando na máquina — pare-o ou ajuste a porta no `docker-compose.yml` e em `ConnectionFactory.java`. |
| Janela não abre / erro relacionado a display | Ambiente sem interface gráfica (por exemplo, uma sessão SSH sem `-X`). É preciso um ambiente desktop de verdade. |

## Para derrubar o banco depois

```bash
docker compose down
```

Use `docker compose down -v` para também apagar o volume e recomeçar do zero com os dados de
exemplo originais (útil se os dados tiverem sido alterados por testes).

## Mais detalhes

Para saber onde cada requisito da especificação foi implementado no código, e o histórico
completo do que foi feito no projeto, veja `RELATORIO_GERAL_PROJETO.md`.
