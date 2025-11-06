package byow.InputDemo;

/**
 * Created by hug.
 */

import edu.princeton.cs.introcs.StdDraw;

public class KeyboardInputSource implements InputSource {
    private static final boolean PRINT_TYPED_KEYS = false;
    public KeyboardInputSource() {
        StdDraw.text(0.3, 0.3, "press m to moo, q to quit");
    }

    public Command getNextInput() {
        return new Command(Character.toUpperCase(StdDraw.nextKeyTyped()));
    }

    public boolean hasNextInput() {
        return StdDraw.hasNextKeyTyped();
    }

}
