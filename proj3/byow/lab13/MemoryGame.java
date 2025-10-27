package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private final int width;
    /** The height of the window of this game. */
    private final int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private final Random rand;
    /** Whether the game is over. */
    private boolean gameOver;
    /** Whether it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);

        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the bottom left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        this.rand = new Random(seed);
        this.round = 0;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.white);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    public String generateRandomString(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i += 1) {
            stringBuilder.append(CHARACTERS[RandomUtils.uniform(rand, 26)]);
        }
        return stringBuilder.toString();
    }

    public void drawFrame(String s) {
        //TODO: Take the string and display it in the center of the screen
        //TODO: If game is not over, display relevant game information at the top of the screen
        StdDraw.clear(Color.BLACK);
        StdDraw.text(width / 2.0, height / 2.0, s);
        if (!gameOver) {

        }
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        char[] l = letters.toCharArray();
        for (char c : l) {
            StdDraw.clear(Color.BLACK);
            StdDraw.text(width / 2.0, height / 2.0, String.valueOf(c));
            StdDraw.show();
            StdDraw.pause(1000);

            StdDraw.clear(Color.BLACK);
            StdDraw.show();
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        while (stringBuilder.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                stringBuilder.append(c);
                drawFrame(stringBuilder.toString());
            }
        }
        return stringBuilder.toString();
    }

    public void startGame() {
        //TODO: Set any relevant variables before the game starts

        //TODO: Establish Engine loop
        round += 1;
        drawFrame("Round: " + round);
        StdDraw.pause(1000);

        StdDraw.clear(Color.black);
        StdDraw.show();
        StdDraw.pause(500);

        String question = generateRandomString(round);
        flashSequence(question);

        String answer = solicitNCharsInput(round);

        if (answer.equals(question)) {
            startGame();
        } else {
            gameOver = true;
            drawFrame("Game Over! You made it to round: " + round);
        }
    }

}
