package ru.mikst74.mikstcraft.main;

import lombok.Getter;
import org.joml.Vector3f;
import ru.mikst74.mikstcraft.collision.CollisionDetector;
import ru.mikst74.mikstcraft.collision.SelectionDetector;
import ru.mikst74.mikstcraft.dictionary.BlockTypeDictionary;
import ru.mikst74.mikstcraft.dictionary.ItemDictionary;
import ru.mikst74.mikstcraft.dictionary.TextureDictionary;
import ru.mikst74.mikstcraft.input.*;
import ru.mikst74.mikstcraft.model.Person;
import ru.mikst74.mikstcraft.model.camera.Camera;
import ru.mikst74.mikstcraft.model.chunk.VoxelFieldAoFactorsMatrix;
import ru.mikst74.mikstcraft.render.RenderedWorldArea;
import ru.mikst74.mikstcraft.server.GameServer;
import ru.mikst74.mikstcraft.storage.WorldSaver;
import ru.mikst74.mikstcraft.util.time.Profiler;
import ru.mikst74.mikstcraft.world.WorldMap;
import ru.mikst74.mikstcraft.world.generator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static ru.mikst74.mikstcraft.input.InputEventDictionary.*;
import static ru.mikst74.mikstcraft.settings.GameProperties.PLAYER_EYE_HEIGHT;
import static ru.mikst74.mikstcraft.util.time.Profiler.printProfile;

@Getter
public class Starter {
    private static final WorldMapGenerator MAP_GENERATOR =
//    new FullEgdesTerrain3DGenerator();
    new RealWorldTerrain3DGenerator();
//    new OnlyEgdesTerrain3DGenerator();
//    new WallXTerrain3DGenerator();
//            new FlatTerrain3DGenerator();
    //                new NoiseTerrain3DGenerator();;
    private ThreadManager threadManager;
    public  GameInstance  gameInstance;

    private GameServer           gameServer;
    private CommunicationManager communicationManager;
    private List<Consumer<Long>> gameTickHandlers;

    public Starter() {
        gameInstance = new GameInstance();

        threadManager = new ThreadManager();
    }

    public void createLocalGame() {
        Profiler.start();
        VoxelFieldAoFactorsMatrix.aoFactorMatrixCalculate();
        createWindow();
        createInputHandlers();

        createDictionaries();

        createWorld();
        createGameServer();
        createCommunicationManager();

        createPlayerAndCamera();
        createRenderedWorldArea();

        linkHandlerToInputEvent();
        createGameEntityManager();

        createGameTickQueue();
    }

    private void createCommunicationManager() {
        communicationManager = new CommunicationManager(gameInstance, gameServer);
    }


    private void createInputHandlers() {
        long window = gameInstance.getWindowManager().getWindow();
        MouseInputHandler mouseInputHandler = new MouseInputHandler();
        mouseInputHandler.addMouseMap(DefaultMapCreator.createDefaultPlayerWorldMouseMap(this));
        glfwSetCursorPosCallback(window, mouseInputHandler::onCursorPos);
        glfwSetMouseButtonCallback(window, mouseInputHandler::onMouseButton);
        glfwSetScrollCallback(window, mouseInputHandler::onScroll);
        mouseInputHandler.selectActiveMouseMap(InputHandlerMapType.PLAYER_WORLD);
        gameInstance.setMouseInputHandler(mouseInputHandler);


        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler();
        glfwSetKeyCallback(window, keyboardInputHandler::onKey);
        keyboardInputHandler.addKeyboardMap(DefaultMapCreator.createDefaultPlayerWorldKeyboardMap(this));
        keyboardInputHandler.selectActiveKeyboardMap(InputHandlerMapType.PLAYER_WORLD);
        gameInstance.setKeyboardInputHandler(keyboardInputHandler);
    }


    private void createWindow() {
        WindowManager windowManager = new WindowManager();
        windowManager.createWindow();

        gameInstance.setWindowManager(windowManager);
    }

    private void createDictionaries() {
        TextureDictionary.getInstance().init();
        BlockTypeDictionary.getInstance().init();
        ItemDictionary.getInstance().init();

    }


    private void createWorld() {
        WorldMapGenerator generator = MAP_GENERATOR;

        WorldMap worldMap = new WorldMap(generator);
        gameInstance.setWorldMap(worldMap);
        gameInstance.setWorldSaver(new WorldSaver(worldMap));
    }

    private void createPlayerAndCamera() {
        CollisionDetector collisionDetector = new CollisionDetector(gameInstance.getWorldMap());
        SelectionDetector selectionDetector = new SelectionDetector(gameInstance.getWorldMap());

        Person player = new Person(communicationManager);
//        player.goForward(0.0001f);
        player.setCollisionDetector(collisionDetector);
        player.setSelectionDetector(selectionDetector);
        gameInstance.getPlayers().add(player);
        gameInstance.setCurrentPlayer(player);
        gameInstance.getMouseInputHandler().setMouseMoveConsumer(gameInstance.getCurrentPlayer()::rotateDelta);

        Camera camera = new Camera(communicationManager, (float) gameInstance.getWindowManager().getWidth() / gameInstance.getWindowManager().getHeight());
        camera.linkAngle(player);
        camera.linkPosition(player);
        camera.setOffset(new Vector3f(0, PLAYER_EYE_HEIGHT, 0));
        camera.subscribeToOnChange(player);
        gameInstance.getCameras().add(camera);
        gameInstance.setCurrentCamera(camera);


    }

    private void createRenderedWorldArea() {
        RenderedWorldArea renderedWorldArea = new RenderedWorldArea(gameInstance.getWorldMap());
        renderedWorldArea.linkToCamera(gameInstance.getCameras().get(0));
        gameInstance.setRenderedWorldArea(renderedWorldArea);

        GameRenderer gameRenderer = new GameRenderer(gameInstance);
        gameRenderer.setRenderedWorldArea(renderedWorldArea);
        gameRenderer.setWindowManager(gameInstance.getWindowManager());
        gameInstance.setGameRenderer(gameRenderer);
    }

    private void createGameServer() {
        gameServer = new GameServer(gameInstance.getWorldMap());
    }


    public void linkHandlerToInputEvent() {
        QUIT.setRunnableOnPress(this::stop);
        Person cp = gameInstance.getCurrentPlayer();
        GO_FORWARD.setRunnable(cp::goForward, cp::stopForward);
        GO_RIGHT.setRunnable(cp::goRight, cp::stopRight);
        GO_LEFT.setRunnable(cp::goLeft, cp::stopLeft);
        GO_BACK.setRunnable(cp::goBack, cp::stopBack);
        FLY_UP.setRunnable(cp::goUp, cp::stopUp);
        FLY_DOWN.setRunnable(cp::goDown, cp::stopDown);
        JUMP.setRunnable(cp::goJump, cp::stopJump);

        ATTACK.setRunnable(cp::doAttack, cp::stopAttack);
        INTERACTION.setRunnable(cp::doInteraction, cp::stopInteraction);
        WIREFRAME.setRunnableOnPress(gameInstance.getRenderedWorldArea().getChunkRenderer()::wireFrameToggler);

    }

    private void createGameEntityManager() {
        GameEntityManager gameEntityManager = new GameEntityManager(gameInstance);
        threadManager.addAndRunThread(gameEntityManager.createThread());
        gameInstance.setGameEntityManager(gameEntityManager);
    }


    private void createGameTickQueue() {
//        gameTickQueue = new ZeroCpuQueue<>();
    }


    public void start() {
        threadManager.addAndRunThread(gameInstance.getGameRenderer().createThread());

        gameTickHandlers = new ArrayList<>();
        gameTickHandlers.add(gameInstance.getGameEntityManager()::gameTickHandler);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            long timeMillis = System.currentTimeMillis();
            gameTickHandlers.forEach(h -> h.accept(timeMillis));
        }, 0, 2, TimeUnit.MILLISECONDS);
        threadManager.addScheduler(scheduler);
//        gameTickQueue.startThread(60);


        // Must be last call - infinite loop
        gameInstance.getWindowManager().runWndProcLoop();
    }


    public void stop(InputEventData inputEventData) {
        stop();
    }

    public void stop() {
//        gameTickQueue.stop();
        gameInstance.getWorldSaver().save();
        printProfile();
        threadManager.stop();
        glfwSetWindowShouldClose(gameInstance.getWindowManager().getWindow(), true);
        gameServer.stop();
        gameInstance.getWindowManager().close();
    }
}
