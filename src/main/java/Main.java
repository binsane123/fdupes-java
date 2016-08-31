import org.slf4j.Logger;

import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

final class Main {

    private static final Logger LOGGER = getLogger(Main.class);

    public static void main(final String[] args) {
        new DuplicateFileTreeWalker().extractDuplicates(asList(args))
                                     .stream()
                                     .peek(LOGGER::info);
    }

    public Main() {
        // NOP
    }

}
