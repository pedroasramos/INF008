package br.edu.ifba.inf008.shell;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import br.edu.ifba.inf008.interfaces.IPluginController;
import org.junit.jupiter.api.Test;

class PluginControllerTest {

    @Test
    void shouldReturnFalseWhenPluginDirectoryIsUnavailable() {
        PluginController controller = new PluginController();

        boolean initialized = assertDoesNotThrow(controller::init);

        assertAll("plugin controller",
                () -> assertNotNull(controller),
                () -> assertInstanceOf(IPluginController.class, controller),
                () -> assertFalse(initialized));
    }
}
