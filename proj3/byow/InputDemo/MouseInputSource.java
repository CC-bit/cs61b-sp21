package byow.InputDemo;

public class MouseInputSource implements InputSource{
    @Override
    public Command getNextInput() {
        return null;
    }

    @Override
    public boolean hasNextInput() {
        return false;
    }
}
