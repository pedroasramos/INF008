# Instruções — Subir o banco de apoio com Docker Compose

O ambiente onde o Claude Code está rodando não tem acesso ao Docker: o usuário não está no grupo `docker`, e `sudo` exige senha interativa, que não pode ser fornecida por essa via. Por isso este passo precisa ser feito manualmente.

## 1. Subir o banco

Na raiz do projeto (`/home/valder/Trabalho POO/INF008`, onde está o `docker-compose.yml`), rode:

```bash
sudo docker compose up -d
```

Isso sobe um container MariaDB (`inf008-ecommerce-mariadb`) na porta `3306`, já inicializado com o schema e os dados de exemplo em `db/init/01_schema_seed.sql` (banco `ecommerce_inf008`, usuário `inf008`, senha `inf008`).

No Claude Code, esse mesmo comando pode ser disparado direto na conversa digitando (o prefixo `!` executa o comando na sessão do terminal e devolve a saída para o chat):

```
! cd "/home/valder/Trabalho POO/INF008" && sudo docker compose up -d
```

## 2. Confirmar que subiu

```bash
sudo docker compose ps
```

Deve mostrar o container `inf008-ecommerce-mariadb` com status `running`/`healthy`. Se quiser confirmar que os dados de exemplo foram carregados:

```bash
sudo docker exec -it inf008-ecommerce-mariadb mariadb -uinf008 -pinf008 ecommerce_inf008 -e "SELECT COUNT(*) FROM products;"
```

## 3. Depois de confirmado

Volte para a conversa com o Claude Code e avise que o banco está no ar (ou simplesmente peça para continuar). A partir daí o teste do fluxo completo pode ser feito diretamente por aqui — rodando a aplicação (`mvn exec:java -pl app`), navegando pelas abas Products/Customers/Checkout/Orders e conferindo os dados no banco pelas mesmas queries acima.

## 4. Para derrubar o banco depois

```bash
sudo docker compose down
```

(Use `sudo docker compose down -v` se quiser também apagar o volume `mariadb_data` e recomeçar do zero com os dados de exemplo originais.)
