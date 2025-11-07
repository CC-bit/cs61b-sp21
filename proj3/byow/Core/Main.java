package byow.Core;

import byow.InputDemo.Input;
import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.InputDemo.MouseInputSource;
import byow.TileEngine.TERenderer;

import java.io.IOException;
import java.nio.file.Files;

/** This is the main entry point for the program. This class simply parses
 *  the command line inputs, and lets the byow.Core.Engine class take over
 *  in either keyboard or input string mode.
 */
public class Main {
    public static void main(String[] args)
            throws IOException, ClassNotFoundException {
        if (args.length == 0) {
            startEngine();
        } else if (args.length > 2) {
            System.out.println("Can only have two arguments - the flag and input string");
            System.exit(0);
        } else if (args.length == 2 && args[0].equals("-s")) {
            startEngine();
        } else if (args.length == 2 && args[0].equals("-p")) {
            System.out.println("Coming soon.");
        } else {
            System.out.println("Please input correct flag.");
            System.exit(0);
        }
    }

    private static void startEngine()
            throws IOException, ClassNotFoundException {
        InputSource[] inputSources = new InputSource[] {new KeyboardInputSource(),
                new MouseInputSource()};
        Input input = new Input(inputSources);

        TERenderer ter = new TERenderer();

        Engine engine = new Engine(input, ter);

        if (!Files.exists(Engine.SAVE_FLODER)) {
            Files.createDirectories(Engine.SAVE_FLODER);
        }

        engine.mainLoop();
    }
}
