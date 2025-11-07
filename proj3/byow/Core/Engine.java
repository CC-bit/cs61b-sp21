package byow.Core;

import byow.InputDemo.*;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class Engine {
    private TERenderer ter;
    private Input doulInput;
    private String seed = "";
    private World world;

    public Engine() {
    }
    public Engine(Input doulInput, TERenderer ter) {
        this.doulInput = doulInput;
        this.ter = ter;
    }

    public static final Path saveFolderPath = Path.of(System.getProperty("user.dir"), "save");
    private final Path saveInfoPath = saveFolderPath.resolve("saveInfo");
    private final String saveFile = "save";
    public static final int MAX_SAVE_SLOTS = 5;
    private static boolean[] isSlotOccupied;
    private static int selectedSlot;

    public static boolean isSlotOccupied(int index) {
        return isSlotOccupied[index];
    }
    public static int getSelectedSlot() {
        return selectedSlot;
    }

    private void saveObject(Object obj, Path file) throws IOException {
        try (OutputStream fos = Files.newOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(obj);
        }
    }

    private void save(int i) throws IOException {
        Path newSave = saveFolderPath.resolve(saveFile + i);

        saveObject(new GameData(seed, world), newSave);

        isSlotOccupied[i] = true;
        saveObject(isSlotOccupied, saveInfoPath);
    }

    /**
     * Reads an object from file.
     *
     * @param file file path
     * @param clazz expected class
     * @return the expected object; null if failure
     */
    public <T> T loadObject(Class<T> clazz, Path file) {
        T loadedObject = null;

        try (InputStream fis = Files.newInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Object rawObject = ois.readObject();
            loadedObject = clazz.cast(rawObject);

        } catch (ClassNotFoundException | ClassCastException | IOException e) {
            System.err.println(e.getMessage());
        }

        return loadedObject;
    }

    private void load(int slotNum) {
        Path saveFile = saveFolderPath.resolve(this.saveFile + slotNum);
        GameData gameData = loadObject(GameData.class, saveFile);
        this.seed = gameData.getSeed();
        this.world = gameData.getWorld();
    }

    /** Game state. */
    public static final String MAIN_MENU = "mainMnue";
    public static final String SEED_TYPING = "seedtyping";
    public static final String LOAD_MENU = "loadMnue";
    public static final String SAVE_MENU = "saveMnue";
    public static final String IN_GAMING = "inGaming";
    private static boolean COMMAND_MODE = false;
    private String gameState = MAIN_MENU;

    public void mainLoop()
            throws IOException, ClassNotFoundException {
        ter.initialize(World.FLOOR_WIDTH, World.FLOOR_HEIGHT + TERenderer.HUDheight,
                0, TERenderer.HUDheight);
        ter.render(MAIN_MENU, null, null);

        while (true) {
            if (doulInput.hasNextInput()) {
                Command command = doulInput.getNextInput();
                interactWithCommand(command);
                ter.render(gameState, seed, world);
            }
            StdDraw.pause(16);
        }
    }

    /** Prompts a message for 1 second. */
    private void prompt(String msg) {
        double w = World.FLOOR_WIDTH;
        double h = World.FLOOR_HEIGHT;
        StdDraw.setPenColor(Color.black);
        StdDraw.filledRectangle(w / 2, h / 2, w / 2, 2);

        StdDraw.setPenColor(Color.white);
        StdDraw.text(w / 2, h / 2, msg);

        StdDraw.show();
        StdDraw.pause(1000);
        doulInput.clearInputs();
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithCommand(Command command)
            throws IOException, ClassNotFoundException {
        String commandType = command.getType();
        if (gameState.equals(MAIN_MENU)) {
            if (commandType.equals(Command.KEYBOARD)) {
                char key = command.getKey();
                if (key == 'N') {
                    gameState = SEED_TYPING;
                } else if (key == 'L') {
                    gameState = LOAD_MENU;
                    //saveinfo file check
                    if (Files.exists(saveInfoPath)) {
                        isSlotOccupied = loadObject(boolean[].class, saveInfoPath);
                    } else {
                        isSlotOccupied = new boolean[MAX_SAVE_SLOTS + 1];
                    }
                } else if (key == 'Q') {
                    System.exit(0);
                }
            }
        } else if (gameState.equals(SEED_TYPING)) {
            if (commandType.equals(Command.KEYBOARD)) {
                char key = command.getKey();
                if (Character.isDigit(key)) {
                    seed += key;
                } else if (key == 'S' && !seed.isEmpty()) {
                    world = new World(new Random(Long.parseLong(seed)));
                    gameState = IN_GAMING;
                } else if (key == 'N') {
                    seed = null;
                    gameState = SEED_TYPING;
                } else if (key == 'Q') {
                    seed = null;
                    gameState = MAIN_MENU;
                }
            }
        } else if (gameState.equals(LOAD_MENU)) {
            if (commandType.equals(Command.KEYBOARD)) {
                char key = command.getKey();
                if (key == 'W') {
                    if (selectedSlot == 0) {
                        selectedSlot = MAX_SAVE_SLOTS;
                    } else {
                        selectedSlot -= 1;
                    }
                } else if (key == 'S') {
                    if (selectedSlot == MAX_SAVE_SLOTS) {
                        selectedSlot = 0;
                    } else {
                        selectedSlot += 1;
                    }
                } else if (key == 'F') {
                    if (isSlotOccupied[selectedSlot]) {
                        load(selectedSlot);
                        gameState = IN_GAMING;
                    } else {
                        prompt("Empty slot, please select another.");
                    }
                } else if (key == 'Q') {
                    gameState = MAIN_MENU;
                }

            } else if (commandType.equals(Command.MOUSE)) {

            }
        } else if (gameState.equals(SAVE_MENU)) {
            if (commandType.equals(Command.KEYBOARD)) {
                char key = command.getKey();
                if (key == 'W') {
                    if (selectedSlot == 1) {
                        selectedSlot = MAX_SAVE_SLOTS;
                    } else {
                        selectedSlot -= 1;
                    }
                } else if (key == 'S') {
                    if (selectedSlot == MAX_SAVE_SLOTS) {
                        selectedSlot = 1;
                    } else {
                        selectedSlot += 1;
                    }
                } else if (key == 'F') {
                    save(selectedSlot);
                    gameState = MAIN_MENU;
                } else if (key == 'Q') {
                    System.exit(0);
                }

            } else if (commandType.equals(Command.MOUSE)) {

            }
        } else if (gameState.equals(IN_GAMING)) {
            if (commandType.equals(Command.KEYBOARD)) {
                char key = command.getKey();
                if (COMMAND_MODE && key == 'Q') {
                    COMMAND_MODE = false;
                    save(0);
                    System.exit(0);
                }
                if (key == 'W' || key == 'A' || key == 'S' || key == 'D') {
                    world.moveAvatar(key);
                } else if (key == ':') {
                    COMMAND_MODE = true;
                } else if (key == 'Q') {
                    gameState = SAVE_MENU;
                }

            } else if (commandType.equals(Command.MOUSE)) {

            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww"). The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        for (int i = 0; i < input.length(); i += 1) {
            char c = input.charAt(i);
            Command command = new Command(c);
            try {
                interactWithCommand(command);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return world.getCurrentFloor().getFloorTiles();
    }
}
