# Outlet ERP - Sistema de Gestão de Varejo

Este projeto é um sistema web de **Enterprise Resource Planning (ERP)** focado em varejo, desenvolvido para a disciplina de Programação Orientada a Objetos 3. O sistema permite o gerenciamento completo de vendas, estoque, produtos, fornecedores e usuários, com perfis de acesso distintos e auditoria de dados.

## 🚀 Funcionalidades Principais

* **Controle de Acesso:** Níveis de permissão para Administrador, Gerente, Funcionário e Cliente.
* **Gestão de Produtos:** Cadastro completo com Marca, Categoria (roupas) e Fornecedor.
* **Controle de Estoque:** Monitoramento de entradas/saídas com alertas visuais para estoque baixo ou crítico.
* **Ponto de Venda (PDV):** Realização de vendas com carrinho de compras, cálculo automático e geração de recibo em PDF.
* **Gestão Financeira:** Controle básico de contas a pagar e categorias de despesas.
* **Auditoria:** Logs automáticos de ações (Criar, Atualizar, Excluir, Login) monitorando quem fez o que e quando.
* **Dashboard:** Gráficos interativos de faturamento e vendas por forma de pagamento.
* **Segurança:** Criptografia de senhas com BCrypt e recuperação de senha via e-mail.

## 🛠 Tecnologias Utilizadas

O projeto foi construído utilizando a arquitetura MVC com as seguintes tecnologias:

* **Backend:**
    * **Java 11** (Linguagem base).
    * **Java EE 8** (Servlet, CDI, JPA).
    * **Hibernate 5.6.15** (Persistência de dados/ORM).
    * **Maven** (Gerenciamento de dependências e build).
    * **Spring Security Crypto** (Hash de senhas com BCrypt).
    * **OpenPDF** (Geração de relatórios/recibos).

* **Frontend:**
    * **JSF 2.3 (JavaServer Faces)**.
    * **PrimeFaces 12.0.0** (Biblioteca de componentes visuais).
    * **OmniFaces** (Utilitários para JSF).
    * **Chart.js** (Gráficos via wrapper Java).

* **Infraestrutura:**
    * **MySQL 8** (Banco de Dados).
    * **Tomcat 9** (via `webapp-runner` para execução standalone).
    * **Railway** (Configuração pronta para deploy em nuvem).

---

## 📸 Galeria do Sistema

### 1. Acesso e Segurança
**Tela de Login:** Autenticação segura com Spring Security e opção de recuperação de senha.
<div align="center">
   <img src="https://github.com/user-attachments/assets/42dd0fd2-6f1a-4cdd-9a76-e60df8aa2da6" alt="Tela de Login" width="700"/>
</div>

---

### 2. Visão Geral (Dashboard)
**Painel Administrativo:** Gráficos de faturamento, vendas por forma de pagamento e alertas de estoque baixo.
<div align="center">
   <img src="https://github.com/user-attachments/assets/3510d68a-9479-4061-ba77-dbbe5c27c81e" alt="Dashboard" width="700"/>
</div>

---

### 3. Ponto de Venda (PDV)
**Frente de Caixa:** Seleção de produtos, carrinho de compras dinâmico e finalização de venda com recibo.
<div align="center">
   <img src="https://github.com/user-attachments/assets/ae129bf1-6bbb-4773-a533-2d1302b52fc2" alt="Tela de Vendas" width="700"/>
</div>

---

### 4. Controle de Estoque
**Gestão de Produtos:** Visualização rápida de níveis de estoque com indicadores de status (OK, Baixo, Crítico).
<div align="center">
   <img src="https://github.com/user-attachments/assets/a1202c8f-f997-445f-8cfa-28584019136d" alt="Controle de Estoque" width="700"/>
</div>

---

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
    O arquivo de configuração principal está em `src/main/resources/config.properties`. Verifique se as credenciais correspondem ao seu banco local:

    ```properties
    db.url=jdbc:mysql://localhost:3306/teste?useSSL=false&serverTimezone=America/Sao_Paulo
    db.username=root
    db.password=sua_senha_aqui
    ```

## 📧 Configuração de E-mail (Mailtrap)

Para testar a recuperação de senha sem enviar e-mails reais, o projeto usa o **Mailtrap**.

1.  Crie uma conta em [Mailtrap.io](https://mailtrap.io).
2.  Vá em "Inboxes" -> "SMTP Settings".
3.  Atualize o arquivo `src/main/resources/config.properties` com suas credenciais:
    ```properties
    mailtrap.host=sandbox.smtp.mailtrap.io
    mailtrap.port=2525
    mailtrap.username=SEU_USUARIO
    mailtrap.password=SUA_SENHA
    ```

## 🚀 Como Rodar o Projeto

### Via Linha de Comando (Recomendado)

O projeto utiliza o `webapp-runner` configurado no `pom.xml`, dispensando a instalação separada do Tomcat.

1.  **Clone o repositório:**
    ```bash
    git clone <url-do-repositorio>
    cd erp-varejo-v5
    ```

2.  **Compile o projeto:**
    ```bash
    mvn clean install
    ```
    *(Se houver erro nos testes unitários, use `mvn clean install -DskipTests`)*.

3.  **Execute a aplicação:**
    ```bash
    java -jar target/dependency/webapp-runner.jar --port 8080 target/*.war
    ```

4.  **Acesse:** Abra o navegador em `http://localhost:8080/erp-varejo-v5`

---

## 👤 Primeiro Acesso (Admin)

Como o banco inicia vazio, execute o SQL abaixo no seu banco de dados para criar o primeiro usuário administrador (Senha padrão: `admin`):

```sql
INSERT INTO usuario (nome_completo, cpf, email, senha, data_cadastro, categoria_usuario, ativo) 
VALUES ('Administrador', '000.000.000-00', 'admin@admin.com', '$2a$10$fWO/s7y.g.u.y.u.y.u.y.u.y.u.y.u.y.u.y.u.y.u.y.u.y', NOW(), 'ADMINISTRADOR', 1);
