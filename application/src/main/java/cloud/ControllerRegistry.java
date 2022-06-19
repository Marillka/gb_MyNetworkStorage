package cloud;

import javafx.fxml.Initializable;

import java.util.HashMap;
import java.util.Map;

// класс-регистр контроллеров, который сохраняет контроллеры при их инициализаци, а потом спокойно в любом месте программы сможем их достать.
public class ControllerRegistry {

    private final static Map<Class<Initializable>, Initializable> CONTROLLER_REGISTRY = new HashMap<>();

    public static void register(Initializable controller) {
        CONTROLLER_REGISTRY.put((Class<Initializable>) controller.getClass(), controller);
    }

    public static Initializable getControllerObject(Class<? extends Initializable> controller) {
        return CONTROLLER_REGISTRY.get(controller);
    }

}
