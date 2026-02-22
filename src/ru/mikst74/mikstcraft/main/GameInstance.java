package ru.mikst74.mikstcraft.main;

import lombok.Getter;
import lombok.Setter;
import ru.mikst74.mikstcraft.collision.CollisionDetector;
import ru.mikst74.mikstcraft.input.KeyboardInputHandler;
import ru.mikst74.mikstcraft.input.MouseInputHandler;
import ru.mikst74.mikstcraft.model.Person;
import ru.mikst74.mikstcraft.model.camera.Camera;
import ru.mikst74.mikstcraft.render.RenderedWorldArea;
import ru.mikst74.mikstcraft.storage.WorldSaver;
import ru.mikst74.mikstcraft.world.WorldMap;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GameInstance {
    private WindowManager        windowManager;
    private MouseInputHandler    mouseInputHandler;
    private KeyboardInputHandler keyboardInputHandler;

    private WorldMap   worldMap;
    private WorldSaver worldSaver;

    private CollisionDetector collisionDetector;
    private List<Person>      players = new ArrayList<>();
    private Person            currentPlayer;
    private List<Camera>      cameras = new ArrayList<>();
    private Camera            currentCamera;

    private RenderedWorldArea renderedWorldArea;
    private GameRenderer      gameRenderer;

    private GameEntityManager gameEntityManager;
}
