# 📌 Desafio CQRS – Backend

Este projeto é um **backend em Spring Boot** que implementa:

- Autenticação com **JWT**
- Operações bancárias (**depósitos, pagamentos e consulta de saldo**)
- **CQRS com Redis** para cache de consultas

Toda a aplicação está **containerizada com Docker Compose**, subindo os seguintes serviços:

- **Postgres 16** – Banco relacional principal  
- **PgAdmin 4** – Interface web para gerenciar o Postgres  
- **Redis 7** – Cache para consultas  
- **Redis Commander** – Interface web para gerenciar o Redis  
- **Backend (Spring Boot)** – API REST  

---

## ⚙️ Tecnologias

- Java 17 (Eclipse Temurin JDK)  
- Spring Boot 3.5.6  
- Spring Data JPA + PostgreSQL  
- Spring Data Redis  
- Spring Security + JWT  
- Lombok  
- Docker & Docker Compose  

---

## 📂 Estrutura dos Containers

| Serviço             | Porta Local | Usuário/Senha               | Observações                   |
|---------------------|------------|------------------------------|--------------------------------|
| **Backend**         | 8080       | —                            | API REST Spring Boot           |
| **Postgres**        | 5432       | admin / senha123             | Banco principal                |
| **PgAdmin**         | 5050       | admin@example.com / admin123 | Interface web para Postgres    |
| **Redis**           | 6379       | redis123                     | Cache para CQRS                |
| **Redis Commander** | 8081       | —                            | Interface web para Redis       |

---

## 🔑 Variáveis de Ambiente (.env)

O projeto já contém um arquivo `.env` com as variáveis necessárias:

## ▶️ Como Rodar

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/seu-repo/desafio-cqrs.git](https://github.com/seu-repo/desafio-cqrs.git)
    cd desafio-cqrs
    ```

2.  **Suba os containers:**
    ```bash
    docker compose up -d --build
    ```

3.  **Verifique se os serviços estão rodando:**
    ```bash
    docker ps
    ```
### 🔐 Autenticação

| Método | Endpoint | Corpo da Requisição (Exemplo) | Descrição |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | ```json\n{ "completeName": "John Doe", "cpf": "52998224725", "login": "jdoe", "password": "123456" }``` | Criar usuário |
| `POST` | `/auth/login` | ```json\n{ "login": "jdoe", "password": "123456" }``` | Login |

**Resposta de Login (Exemplo):**
```json
{ "token": "JWT_TOKEN_AQUI" }
```
### 💰 Operações Bancárias

**(necessário JWT no header `Authorization: Bearer ...`)**

| Método | Endpoint | Corpo da Requisição (Exemplo) | Descrição |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/deposits` | ```json\n{ "value": 100.00 }``` | Depositar valor |
| `POST` | `/api/payments` | ```json\n{ "value": 50.00 }``` | Pagar/Transferir valor |
| `GET` | `/api/balance` | - | Consultar saldo e histórico |

## 🔑 Variáveis de Ambiente (.env)

O projeto já contém um arquivo `.env` com as variáveis necessárias:

```env
POSTGRES_USER=admin
POSTGRES_PASSWORD=senha123
POSTGRES_DB=desafio

PGADMIN_EMAIL=admin@example.com
PGADMIN_PASSWORD=admin123

REDIS_PASSWORD=redis123
TZ=America/Sao_Paulo

JWT_SECRET=kTx3tjjKSIHo6mjbyUo5kOLvAO1YhEocBay3FLEWGPjrwMG5WpxaScgy3M8Ee8Ff0HuYDHV5euhWcBOpeZ7TC929wHbVMFwGv4bkEtTq4RFOLY1lDTs2HMaWOwKqDfeBA
JWT_EXPIRATION=3600000
