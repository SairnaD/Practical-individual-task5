package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CsvBatchImportService {

    private final CsvImportService csvImportService = new CsvImportService();

    private final Path processedFilesPath = Path.of("output", "processed-files.txt");

    public ImportResult importAll(Path inputDirectory) {
        List<Animal> allAnimals = new ArrayList<>();
        List<String> invalidRows = new ArrayList<>();
        int totalSkipped = 0;

        try {
            Files.createDirectories(processedFilesPath.getParent());

            Set<String> processedFiles = new HashSet<>();

            if (Files.exists(processedFilesPath)) {
                processedFiles.addAll(Files.readAllLines(processedFilesPath,StandardCharsets.UTF_8)
                );
            }

            List<Path> csvFiles;

            try (Stream<Path> paths = Files.walk(inputDirectory)) {
                csvFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString()
                                .toLowerCase()
                                .endsWith(".csv"))
                        .sorted()
                        .toList();
            }

            for (Path csvFile : csvFiles) {
                String fileKey = csvFile.toAbsolutePath().normalize().toString();

                if (processedFiles.contains(fileKey)) {
                    System.out.println("Skipping already processed file: " + csvFile);
                    continue;
                }

                System.out.println("Processing file: " + csvFile);

                ImportResult result = csvImportService.importAnimals(csvFile);

                allAnimals.addAll(result.allAnimals());
                totalSkipped += result.skippedRows();
                invalidRows.addAll(result.invalidRows());

                Files.writeString(
                        processedFilesPath,
                        fileKey + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );

                processedFiles.add(fileKey);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to process input directory: " + inputDirectory,e);
        }

        return new ImportResult(allAnimals, totalSkipped, invalidRows);
    }
}
