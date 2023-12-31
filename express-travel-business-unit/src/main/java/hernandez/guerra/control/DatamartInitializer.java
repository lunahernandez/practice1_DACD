package hernandez.guerra.control;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class DatamartInitializer {
    private final String eventStoreDirectory;

    public DatamartInitializer(String eventStoreDirectory) {
        this.eventStoreDirectory = eventStoreDirectory;
    }

    public File findLatestEventFile(String topicName) {
        File topicDirectory = new File(eventStoreDirectory + "/" + topicName);

        if (!topicDirectory.exists() || !topicDirectory.isDirectory()) {
            return null;
        }

        File[] ssDirectories = topicDirectory.listFiles(File::isDirectory);
        if (ssDirectories == null || ssDirectories.length == 0) {
            return null;
        }

        return Arrays.stream(ssDirectories)
                .flatMap(ssDir -> Arrays.stream(Objects.requireNonNull(ssDir.listFiles())))
                .filter(File::isFile)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }
}

