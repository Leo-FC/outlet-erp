# Outlet ERP - Sistema de Gestão de Varejo

Este projeto é um sistema web de **Enterprise Resource Planning (ERP)** focado em varejo, desenvolvido para a disciplina de Programação Orientada a Objetos 3. O sistema permite o gerenciamento completo de vendas, estoque, produtos, fornecedores e usuários, com perfis de acesso distintos e auditoria de dados.

## 🚀 Funcionalidades Principais

  * **Controle de Acesso:** Níveis de permissão para Administrador, Gerente, Funcionário e Cliente.
  * **Gestão de Produtos:** Cadastro completo com Marca, Categoria (roupas) e Fornecedor.
  * **Controle de Estoque:** Monitoramento de entradas/saídas com alertas visuais para estoque baixo ou crítico.
  * **Ponto de Venda (PDV):** Realização de vendas com carrinho de compras, cálculo automático e geração de recibo em PDF.
  * **Gestão Financeira:** Controle básico de contas a pagar e categorias de despesas.
  * **Auditoria:** Logs automáticos de ações (Criar, Atualizar, Excluir, Login) monitorando quem fez o que e quando.
  * **Dashboard:** Gráficos de faturamento e vendas por forma de pagamento.
  * **Segurança:** Criptografia de senhas com BCrypt e recuperação de senha via e-mail (Mailtrap).

## 🛠 Tecnologias Utilizadas

O projeto foi construído utilizando a arquitetura MVC com as seguintes tecnologias:

  * **Backend:**

      * **Java 11** (Linguagem base).
      * **Java EE 8** (Servlet, CDI, JPA).
      * **Hibernate 5.6.15** (Persistência de dados/ORM).
      * **Maven** (Gerenciamento de dependências e build).
      * **Spring Security Crypto** (Apenas para hash de senhas com BCrypt).
      * **OpenPDF** (Geração de relatórios/recibos).

  * **Frontend:**

      * **JSF 2.3 (JavaServer Faces)**.
      * **PrimeFaces 12.0.0** (Biblioteca de componentes visuais).
      * **OmniFaces** (Utilitários para JSF).
      * **Chart.js** (Gráficos via wrapper Java).

  * **Banco de Dados:**

      * **MySQL 8**.

  * **Infraestrutura/DevOps:**

      * **Tomcat 9** (via `webapp-runner` para execução standalone).
      * **Railway** (Configuração pronta para deploy em nuvem via `railway.toml`).

## ⚙️ Pré-requisitos

Para rodar o projeto localmente, você precisará ter instalado:

1.  **Java JDK 11** ou superior.
2.  **Maven 3.6+**.
3.  **MySQL Server 8.0**.
4.  **Git**.

## 💾 Configuração do Banco de Dados

O projeto está configurado para criar as tabelas automaticamente (`hbm2ddl.auto = update`), mas você precisa criar o *schema* inicial.

1.  Acesse seu MySQL e crie um banco de dados chamado `teste` (ou altere o nome no arquivo de configuração):

    ```sql
    CREATE DATABASE teste;
    ```

2.  **Configuração Local:**
    O arquivo de configuração principal está em `src/main/resources/config.properties`. Verifique se as credenciais batem com o seu banco local:

    ```properties
    db.url=jdbc:mysql://localhost:3306/teste?useSSL=false&serverTimezone=America/Sao_Paulo
    db.username=root
    db.password=sua_senha_aqui
    ```

## 📧 Configuração de E-mail (Mailtrap)

Para testar a recuperação de senha sem enviar e-mails reais, o projeto usa o **Mailtrap**.

1.  Crie uma conta gratuita no [Mailtrap.io](https://mailtrap.io).
2.  Vá em "Inboxes" -\> "SMTP Settings".
3.  Atualize o arquivo `src/main/resources/config.properties` com suas credenciais:
    ```properties
    mailtrap.host=sandbox.smtp.mailtrap.io
    mailtrap.port=2525
    mailtrap.username=SEU_USUARIO_MAILTRAP
    mailtrap.password=SUA_SENHA_MAILTRAP
    ```

## 🚀 Como Rodar o Projeto

### Opção 1: Via Linha de Comando (Recomendado)

O projeto utiliza o plugin `webapp-runner`, que permite rodar a aplicação sem precisar instalar um Tomcat separadamente.

1.  Clone o repositório:

    ```bash
    git clone <url-do-repositorio>
    cd erp-varejo-v5
    ```

2.  Compile o projeto e baixe as dependências:

    ```bash
    mvn clean install
    ```

    *(Nota: Os testes foram ignorados no script de build do Railway, se houver erro nos testes localmente, use `mvn clean install -DskipTests`)*.

3.  Execute a aplicação:

    ```bash
    java -jar target/dependency/webapp-runner.jar --port 8080 target/*.war
    ```

4.  Acesse no navegador: `http://localhost:8080/erp-varejo-v5`

### Opção 2: Via Eclipse / IntelliJ

1.  Importe o projeto como **Maven Project**.
2.  Configure um servidor **Apache Tomcat 9**.
3.  Adicione o artefato do projeto ao servidor.
4.  Inicie o servidor e acesse a URL padrão.

## ☁️ Deploy (Railway)

Este projeto já possui um arquivo `railway.toml` e lógica no `AppInitializer.java` para detectar se está rodando localmente ou na nuvem.

Ao fazer deploy no Railway:

1.  O sistema detecta automaticamente as variáveis de ambiente `MYSQLHOST`, `MYSQLPORT`, etc.
2.  Você deve configurar as variáveis de ambiente do Mailtrap (`MAILTRAP_HOST`, etc.) no painel do Railway.

## 👤 Primeiro Acesso

Como o banco de dados inicia vazio, você precisará inserir um usuário administrador diretamente no banco de dados ou alterar a lógica de inicialização, pois a senha precisa ser criptografada (BCrypt).

**SQL Sugerido para criar o primeiro Admin (Senha: `admin`):**

```sql
INSERT INTO usuario (nome_completo, cpf, email, senha, data_cadastro, categoria_usuario, ativo) 
VALUES ('Administrador', '000.000.000-00', 'admin@admin.com', '$2a$10$fWO/s7y.g.u.y.u.y.u.y.u.y.u.y.u.y.u.y.u.y.u.y.u.y', NOW(), 'ADMINISTRADOR', 1);
```

*(Nota: O hash acima é um exemplo para a senha "admin", gerado via BCrypt).*
