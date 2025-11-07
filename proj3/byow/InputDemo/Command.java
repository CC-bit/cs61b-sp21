package byow.InputDemo;

public class Command {
    public static final String MOUSE = "MOUSE";
    public static final String KEYBOARD = "Keyboard";
    private final String type;
    private double xOrdinary;
    private double yOrdinary;
    private char key;

    public Command(double x, double y) {
        xOrdinary = x;
        yOrdinary = y;
        type = MOUSE;
    }

    public Command(char c) {
        key = Character.toUpperCase(c);
        type = KEYBOARD;
    }

    public String getType() {
        return type;
    }

    public double getxOrdinary() {
        return xOrdinary;
    }

    public double getyOrdinary() {
        return yOrdinary;
    }

    public char getKey() {
        return key;
    }
}
