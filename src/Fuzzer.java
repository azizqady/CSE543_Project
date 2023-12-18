import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Fuzzer {

    // Constants for controlling the behavior of the fuzzer
    private static final int RANDOM_BYTE_PROBABILITY = 13;
    private static final int EXTEND_INPUT_INTERVAL = 500;
    private static final int EXTEND_INPUT_LENGTH = 10;

    public static void main(String[] args) throws IOException {
        String prngSeed;
        String outputFile = null;
        int numIterations;

        // Check if the correct number of command-line arguments is provided
        if (args.length < 2 || args.length > 5) {
            System.err.println("Usage: fuzzer (-s seed | <prng_seed>) <num_iterations> [-o <output_file>]");
            return;
        }

        // Parse command-line arguments to determine PRNG seed, number of iterations, and optional output file
        if (args[0].equals("-s")) {
            if (args.length < 3) {
                System.err.println("Missing seed string or seed file path");
                return;
            }
            
            prngSeed = readSeedStringOrFile(args[1]);
            numIterations = Integer.parseInt(args[2]);
            if (prngSeed == null) {
                System.err.println("Failed to read seed string or file");
                return;
            }
        } else {
            prngSeed = args[0];
            numIterations = Integer.parseInt(args[1]);
        }

        // Check for the optional output file argument and set the output file name
        if (args.length == 4 && args[2].equals("-o")) {
            outputFile = args[3];
        } else if (args.length == 5 && args[3].equals("-o")) {
            outputFile = args[4];
        }
        
        // Convert the PRNG seed to a byte array and initialize the random number generator
        byte[] input = prngSeed.getBytes(StandardCharsets.UTF_8);
        Random random = new Random(prngSeed.hashCode());

        // Iterate over the specified number of iterations, mutating the input
        for (int i = 0; i < numIterations; i++) {
            mutateInput(input, random);

            // Extend the input periodically based on the defined interval
            if (i % EXTEND_INPUT_INTERVAL == 0) {
                input = extendInput(input, random);
            }
        }

        // Write the mutated input to either the specified output file or standard output
        if (outputFile != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                fileOutputStream.write(input);
            } catch (IOException e) {
                System.err.println("Failed to write output to file: " + outputFile);
            }
        } else {
            System.out.write(input);
        }
    }

    // Helper method to determine whether the seed is a file path or a string
    private static String readSeedStringOrFile(String seedArgument) {
        if (seedArgument.endsWith(".txt")) {
            // Assume it's a file path
            return readSeedFile(seedArgument);
        } else {
            // Assume it's a string
            return seedArgument;
        }
    }

    // Helper method to read the contents of a seed file
    private static String readSeedFile(String seedFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(seedFilePath))) {
            StringBuilder seed = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                seed.append(line);
            }
            return seed.toString();
        } catch (IOException e) {
            return null;
        }
    }

    // Helper method to mutate the input byte array randomly
    private static void mutateInput(byte[] input, Random random) {
        for (int i = 0; i < input.length; i++) {
            if (random.nextInt(100) < RANDOM_BYTE_PROBABILITY) {
                input[i] = (byte) random.nextInt();
            }
        }
    }

    // Helper method to extend the input byte array with random bytes
    private static byte[] extendInput(byte[] input, Random random) {
        byte[] extendedInput = new byte[input.length + EXTEND_INPUT_LENGTH];
        System.arraycopy(input, 0, extendedInput, 0, input.length);

        for (int i = input.length; i < extendedInput.length; i++) {
            extendedInput[i] = (byte) random.nextInt();
        }

        return extendedInput;
    }
}
