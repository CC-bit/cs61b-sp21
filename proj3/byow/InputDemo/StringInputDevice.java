package byow.InputDemo;

/**
 * Created by hug.
 */
public class StringInputDevice implements InputSource  {
    private final String input;
    private int index;

    public StringInputDevice(String s) {
        index = 0;
        input = s;
    }

    public Command getNextInput() {
        char returnChar = input.charAt(index);
        index += 1;
        return new Command(returnChar);
    }

    public boolean hasNextInput() {
        return index < input.length();
    }
}
