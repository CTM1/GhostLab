package ghostlab;

import java.io.PrintStream;

public class Logger {
	private static PrintStream output = System.err;
	private static boolean verbose = false;

	/**
	 * setStream sets the output stream of the logger. Useful in case
	 * we want to be able to write logs to a file, for instance.
	 * Default value is stderr.
	 *
	 * @param s The output stream, must be already opened.
	 */
	public static void setStream(PrintStream s) { output = s; }

	/**
	 * verbose setter.
	 *
	 * @param v new verbose value.
	 */
	public static void setVerbose(boolean v) { verbose = v; }

	/**
	 * Verbose logging. This takes a message, objects, and writes it to output iff verbose is activated.
	 *
	 * @param message the message/format string.
	 * @param args Optionnal, the additional arguments.
	 */
	public static void verbose(String message, Object ... args) {
		if (verbose) {
			output.format(message, args);
		}
	}

	/**
	 * General logging. Takes a message, objects, and prints it to output.
	 *
	 * @param message the message/format string.
	 * @param args Optionnal, the additional arguments.
	 */
	public static void log(String message, Object ... args) {
		output.format(message, args);
	}
}
