package byow.InputDemo;

import java.util.Random;

/**
 * Created by hug.
 */
public class RandomInputSource implements InputSource {
    Random r;

    public RandomInputSource(Long seed) {
        r = new Random(seed);
    }

    /** Returns a random letter between a and z.*/
    public Command getNextInput() {
        return new Command((char) (r.nextInt(26) + 'A'));
    }

    public boolean hasNextInput() {
        return true;
    }

    @Override
    public void clear() {
        return;
    }
}
