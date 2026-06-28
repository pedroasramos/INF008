package br.edu.ifba.inf008.shell;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AuthenticationControllerTest {

    @Test
    void shouldAcceptBasicAuthenticationOperations() {
        AuthenticationController controller = new AuthenticationController();

        assertAll("authentication operations",
                () -> assertTrue(controller.signIn("student", "secret")),
                () -> assertTrue(controller.signUp("student", "secret")),
                () -> assertTrue(controller.signOut()));
    }

    @Test
    void shouldNotThrowWhenCredentialsAreEmpty() {
        AuthenticationController controller = new AuthenticationController();

        assertDoesNotThrow(() -> controller.signIn("", ""));
    }
}
