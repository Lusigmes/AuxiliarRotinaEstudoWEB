# 📚 Sistema de Gerenciamento de Estudos

- Sistema completo para organização de estudos com cronograma semanal e revisões espaçadas.
- Projeto em desenvolvimento referente ao trabalho da disciplina de Linguagens de Programação com prof. Lucas Ismaily

## 🎯 **Funcionalidades Principais**

- ✅ **Autenticação de usuários**
- ✅ **Cronograma semanal** de estudos
- ✅ **Registro diário** de conteúdos estudados
- ✅ **Revisões automáticas** (D+1, D+7, D+14)
- ✅ **Relatórios** de desempenho
- ✅ **Calendário interativo**

## 🔄 **Fluxo do Sistema**

1. **Usuário se registra** → Cria conta
2. **Cria cronograma** → Define disciplinas por dia da semana
3. **Registra estudos** → O que estudou cada dia
4. **Sistema cria revisões** → Automaticamente (D+1, D+7, D+14)
5. **Gerencia revisões** → Marca como concluídas
6. **Consulta relatórios** → Visualiza desempenho


## 🛠️ **Tecnologias**

### **Backend**
- Java + Spring Boot 
- Spring Data JPA + Hibernate
- Spring Security + JWT
- PostgreSQL

### **Frontend**
- Vue 3 + Composition API
- TypeScript
- Pinia (gerenciamento de estado)
- Vue Router
- Axios
- Tailwind CSS
- vue-the-mask
- Vuetify
- VeeValidade
- Yup

## 📈 **Método de Revisões**

O sistema implementa **revisões espaçadas** padrões baseadas no método:
- **D+1**: 1 dia após o estudo
- **D+7**: 7 dias após o estudo  
- **D+14**: 14 dias após o estudo

- Caso desejado, o usuário poderá alterar o dia da revisão conforme o desejado
  (não pode ser datas passadas, nem uma data aanterior ao estudo pai da revisão de escolha)

## 🔒 **Segurança**

- Autenticação JWT
- Controle de acesso por usuário
- Validação de dados de entrada
  
---
