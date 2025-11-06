package byow.InputDemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Input {
    private final List<InputSource> inputList = new ArrayList<>();
    private InputSource current;

    public Input(InputSource[] args) {
        inputList.addAll(Arrays.asList(args));
    }

    public boolean hasNextInput() {
        for (InputSource s : inputList) {
            if (s.hasNextInput()) {
                current = s;
                return true;
            }
        }
        return false;
    }

    public Command getNextInput() {
        if (current != null && current.hasNextInput()) {
            return current.getNextInput();
        } else {
            return null;
        }
    }

    public void clearInputs() {
        if (hasNextInput()) {
            getNextInput();
        }
    }
}
