# 🚀 LTI 1.3 Tool com Spring Boot

> 🎓 Integração com Blackboard Learn (LTI 1.3)

Aplicação de exemplo para integração **LTI 1.3 com Blackboard Learn** utilizando Spring Boot.

O projeto demonstra como implementar o fluxo completo de autenticação **OIDC (OpenID Connect)** do Blackboard, processar o `id_token` e extrair informações do usuário autenticado.

---

## 🧠 Sobre o projeto

Este projeto tem como objetivo:

- Implementar o fluxo **LTI 1.3 (OIDC Login + Launch)**
- Integrar com o Blackboard Learn
- Receber e processar o `id_token`
- Extrair dados do usuário autenticado (nome, email e roles)
- Expor os dados via API para integração com outras aplicações

---

## 🎓 Compatibilidade

- Blackboard Learn (Anthology)
- LTI 1.3 (OIDC + JWT)

---

## 🔐 Fluxo LTI implementado

1. LMS (Blackboard) chama `/lti/login`
2. Aplicação redireciona para o endpoint OIDC
3. Blackboard autentica o usuário
4. Blackboard envia `id_token` para `/lti/launch`
5. Aplicação processa o token e salva os dados
6. Usuário é redirecionado para `/me`

---

## 📦 Tecnologias

- Java 17+
- Spring Boot
- Spring Security
- Lombok
- Nimbus JOSE JWT

---

## ⚙️ Configuração

```yaml
spring:
  application:
    name: lti

lti:
  platform:
    issuer: https://blackboard.com
    client-id: ${LTI_CLIENT_ID}
    jwks-url: https://developer.blackboard.com/api/v1/management/applications/${LTI_CLIENT_ID}/jwks.json
    oidc-auth-url: https://developer.blackboard.com/api/v1/gateway/oidcauth
    access-token-url: https://developer.blackboard.com/api/v1/gateway/oauth2/jwttoken
    signature-alg: RS256
```

---

## 🔑 Variáveis de ambiente

```bash
export LTI_CLIENT_ID=SEU_CLIENT_ID
export APP_BASE_URL=https://seu-dominio
```

---

## 🔗 Endpoints

- `/lti/login`
- `/lti/launch`
- `/me`
- `/debug/lti`

---

## 🚀 Executando

```bash
mvn spring-boot:run
```

---

## 👨‍💻 Autor

Fabio RVS

---

## 📄 Licença

MIT
