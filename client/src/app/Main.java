package app;

/**
 * The technical entry point of the project.
 * <p>
 * This class serves as a "Launcher" wrapper. It exists to bypass specific
 * JavaFX classpath requirements when running from a JAR file or certain IDE
 * configurations, by delegating the main execution to the {@link App} class.
 * </p>
 */
public class Main {
    /**
     * Main project entry point.
     * Redirects execution to the JavaFX Application class.
     * @param args Command-line arguments passed to the program.
     */
    public static void main(String[] args) {
        App.main(args);
    }
}