# 📖 CronoStudy - Sistema de Gerenciamento de Estudos

## 📌 Sobre o Projeto
Sistema web para organização de estudos com revisão espaçada. Desenvolvido para a disciplina de **Linguagens de Programação** - Professor Lucas Ismaily.

## ✨ Funcionalidades
- 🔐 Autenticação JWT com renovação de token
- 📅 Cronograma semanal de estudos
- 📝 Registro diário de disciplinas estudadas
- 🔄 Revisões automáticas (D+1, D+7, D+14)
- 📊 Dashboard com estatísticas e progresso
- 📄 Exportação de cronogramas e relatórios em PDF
- 🔔 Notificações de revisões pendentes
- 📱 Design responsivo

## 🚀 Começando

### Pré-requisitos
- Java 17+
- Node.js 20+
- PostgreSQL 14+
- Maven 3.8+

### Configuração

#### 1. Banco de Dados
```sql
CREATE DATABASE lip_projeto;
```

#### 2. Backend (Spring Boot)
```bash
cd aplicacao/backend/AuxiliarRotinaEstudo
mvn clean install
mvn spring-boot:run
```
**API:** `http://localhost:8080`

#### 3. Frontend (Vue.js)
```bash
cd aplicacao/frontend/AuxiliarRotinaEstudo
npm install
npm run dev
```
**Frontend:** `http://localhost:5173`

## 🛠 Stack Tecnológica

### Backend
- **Java 17** + Spring Boot 3.5.7
- **Spring Security** + JWT Authentication
- **PostgreSQL** + Spring Data JPA/Hibernate
- **Spring Validation** + Lombok
- **Maven** para gerenciamento de dependências
- **Arquitetura MVC**
- **Uso de DTO's**
- **Criação, Testes e Consumo de API**

### Frontend
- **Vue 3** + TypeScript + Composition API
- **Vuetify** para componentes UI
- **Pinia** para gerenciamento de estado
- **Vue Router** para navegação
- **Axios** para requisições HTTP
- **Chart.js** para visualização de dados
- **Date-fns** para manipulação de datas
- **Yup** para validação de formulários
- **jsPDF** + **jsPDF-autotable** para exportação em PDF



## 🔄 Sistema de Revisões

### Algoritmo de Revisão Espaçada
- **D+1**: Revisão 24 horas após o estudo
- **D+7**: Revisão após 7 dias
- **D+14**: Revisão após 14 dias

### Regras de Negócio
- Revisões são criadas automaticamente após registro de estudo
- Datas de revisão são ajustáveis (não podem ser retroativas)
- Notificações são disparadas para revisões pendentes
- Progresso é calculado com base em revisões concluídas





## 📊 Fluxo de Trabalho

1. **Autenticação** → Login/Cadastro e renovação de token com JWT
2. **Cronograma** → Cadastro de disciplinas semanais
3. **Estudos** → Cadastro de estudos diários
4. **Revisão** → Sistema agenda revisões automáticas para os estudos
  (É possível reagendar e concluir quando desejado)

5. **Acompanhamento** → Dashboard com métricas de desempenho de estudo
6. **Exportação** → Geração de PDFs para: Cronograma e Relatório

## 🔐 Segurança

- Autenticação baseada em JWT
- Tokens com expiração e aviso para renovação
- Validação de dados em backend e frontend
- Proteção contra injeção SQL via JPA
- CORS configurado para ambiente local

---

