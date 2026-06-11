package cn.tx.aiagent.login.domain;

public sealed interface AuthenticationResult {

    record Authenticated(long userId, String username, String name) implements AuthenticationResult {
    }

    record InvalidCredentials() implements AuthenticationResult {
    }

    record AccountDisabled() implements AuthenticationResult {
    }
}
