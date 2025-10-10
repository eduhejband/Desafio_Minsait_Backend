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



▶️ Como Rodar

Clone o repositório:

git clone https://github.com/seu-repo/desafio-cqrs.git
cd desafio-cqrs


Suba os containers:

docker compose up -d --build


Verifique se os serviços estão rodando:

docker ps


Acesse os serviços:

API Backend → http://localhost:8080

PgAdmin → http://localhost:5050

Redis Commander → http://localhost:8081

🔥 Endpoints Principais
🔐 Autenticação

POST /auth/register – Criar usuário

{
  "completeName": "John Doe",
  "cpf": "52998224725",
  "login": "jdoe",
  "password": "123456"
}


POST /auth/login – Login

{
  "login": "jdoe",
  "password": "123456"
}


Resposta:

{ "token": "JWT_TOKEN_AQUI" }

💰 Operações Bancárias

(necessário JWT no header Authorization: Bearer ...)

POST /api/deposits

{ "value": 100.00 }


POST /api/payments

{ "value": 50.00 }


GET /api/balance

{
  "totalBalance": 50.00,
  "historic": [
    { "type": "payment", "value": 50.00, "date": "10-10-2025 12:00:00" },
    { "type": "deposit", "value": 100.00, "date": "10-10-2025 11:59:00" }
  ]
}

🛠️ Desenvolvimento Local (sem Docker)

Suba manualmente Postgres e Redis.

Ajuste application.properties ou use variáveis de ambiente.

Rode com Maven:

mvn spring-boot:run

✅ Testes

Para rodar os testes unitários:

mvn test


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
