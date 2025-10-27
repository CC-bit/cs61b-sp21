package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.Random;

public class MemoryGame {
    private final long seed;
    /** The width of the window of this game. */
    private final int width;
    /** The height of the window of this game. */
    private final int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private final Random rand;
    /** Whether it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

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
        this.seed = seed;
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
        StdDraw.clear(Color.BLACK);
        StdDraw.text(width / 2.0, height / 2.0, s);
        drawHUD();
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        char[] l = letters.toCharArray();
        for (char c : l) {
            StdDraw.clear(Color.BLACK);
            drawHUD();
            StdDraw.text(width / 2.0, height / 2.0, String.valueOf(c));
            StdDraw.show();
            StdDraw.pause(1000);

            StdDraw.clear(Color.BLACK);
            drawHUD();
            StdDraw.show();
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        while (StdDraw.hasNextKeyTyped()) {
            StdDraw.nextKeyTyped();
        }

        StringBuilder stringBuilder = new StringBuilder();
        while (stringBuilder.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                stringBuilder.append(c);
                drawFrame(stringBuilder.toString());
            }
        }
        StdDraw.pause(500);
        return stringBuilder.toString();
    }

    private void drawHUD() {
        String s = String.valueOf(seed);
        StdDraw.text(5, height - 1, "Round: " + round);
        StdDraw.text(width - s.length() - 4, height - 1, "seed: " + s);
        StdDraw.line(0, height - 2, width, height - 2);
        if (playerTurn) {
            StdDraw.text(width / 2.0, height - 1, "Type!");
        } else {
            StdDraw.text(width / 2.0, height - 1, "Watch!");
        }
    }

    public void startGame() {
        playerTurn = false;

        round += 1;
        drawFrame("Round: " + round);
        StdDraw.pause(1000);
        StdDraw.clear(Color.black);
        drawHUD();
        StdDraw.show();
        StdDraw.pause(500);

        String question = generateRandomString(round);
        flashSequence(question);

        StdDraw.clear(Color.BLACK);
        playerTurn = true;
        drawHUD();
        StdDraw.show();

        String answer = solicitNCharsInput(round);
        if (answer.equals(question)) {
            StdDraw.clear(Color.BLACK);
            drawFrame("Great!");
            StdDraw.show();
            StdDraw.pause(1000);

            StdDraw.clear(Color.BLACK);
            drawHUD();
            StdDraw.show();
            StdDraw.pause(500);

            startGame();
        } else {
            drawFrame("Game Over! You made it to round: " + round);
        }
    }
}
