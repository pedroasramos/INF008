package br.edu.ifba.inf008.interfaces;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import br.edu.ifba.inf008.shell.AuthenticationController;
import br.edu.ifba.inf008.shell.IOController;
import br.edu.ifba.inf008.shell.PluginController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ICoreTest {

    @BeforeEach
    void resetBeforeTest() {
        TestCore.reset();
    }

    @AfterEach
    void resetAfterTest() {
        TestCore.reset();
    }

    @Test
    void shouldExposeConfiguredSingletonInstance() {
        assertNull(ICore.getInstance());

        TestCore core = new TestCore();
        TestCore.install(core);

        assertAll("core singleton",
                () -> assertSame(core, ICore.getInstance()),
                () -> assertNull(core.getUIController()),
                () -> assertNotNull(core.getAuthenticationController()),
                () -> assertInstanceOf(IAuthenticationController.class, core.getAuthenticationController()),
                () -> assertInstanceOf(IIOController.class, core.getIOController()),
                () -> assertInstanceOf(IPluginController.class, core.getPluginController()));
    }

    private static class TestCore extends ICore {
        private final IAuthenticationController authenticationController = new AuthenticationController();
        private final IIOController ioController = new IOController();
        private final IPluginController pluginController = new PluginController();

        static void install(ICore core) {
            instance = core;
        }

        static void reset() {
            instance = null;
        }

        @Override
        public IUIController getUIController() {
            return null;
        }

        @Override
        public IAuthenticationController getAuthenticationController() {
            return authenticationController;
        }

        @Override
        public IIOController getIOController() {
            return ioController;
        }

        @Override
        public IPluginController getPluginController() {
            return pluginController;
        }
    }
}
