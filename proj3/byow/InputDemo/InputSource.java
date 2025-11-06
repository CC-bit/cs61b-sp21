package byow.InputDemo;

/**
 * Created by hug.
 */
public interface InputSource {
    public Command getNextInput();
    public boolean hasNextInput();
    public default void clear() {
        while (hasNextInput()) {
            getNextInput();
        }
    }
}
