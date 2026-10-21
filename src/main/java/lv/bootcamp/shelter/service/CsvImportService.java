
package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public class CsvImportService {

    public ImportResult importAnimals(Path inputPath) {
        log.info("Starting import from {}", inputPath);

        List<Animal> allAnimals = new ArrayList<>();
        List<String> invalidRows = new ArrayList<>();
        int skippedRows = 0;

        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {

            String line;
            int rowNumber = 0;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (rowNumber == 1) {
                    continue;
                }

                try {
                    String[] parts = line.split(",", -1);

                    if (parts.length != 5) {
                        throw new IllegalArgumentException(
                                "Expected 5 fields");
                    }

                    String name = parts[0].trim();
                    String species = parts[1].trim();
                    String ageText = parts[2].trim();
                    String vaccinatedText = parts[3].trim();

                    if (name.isEmpty() || species.isEmpty()
                            || vaccinatedText.isEmpty()
                            || parts[4].trim().isEmpty()) {
                        throw new IllegalArgumentException( "Required field is missing");
                    }

                    Integer age = null;

                    if (!ageText.isEmpty()) {
                        age = Integer.parseInt(ageText);

                        if (age < 0) {
                            throw new IllegalArgumentException("Age cannot be negative");
                        }
                    }

                    if (!vaccinatedText.equalsIgnoreCase("true")
                            && !vaccinatedText.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Invalid vaccination status");
                    }

                    boolean vaccinated = Boolean.parseBoolean(vaccinatedText);

                    LocalDate intakeDate = LocalDate.parse(
                            parts[4].trim(),
                            DateTimeFormatter.ofPattern(
                                    "dd.MM.yyyy", Locale.ROOT
                            ).withResolverStyle(ResolverStyle.STRICT)
                    );

                    allAnimals.add(new Animal(name, species, age, vaccinated, intakeDate));

                } catch (IllegalArgumentException | DateTimeParseException e) {

                    skippedRows++;

                    String invalidRow = inputPath.getFileName() + ": row " + rowNumber;

                    invalidRows.add(invalidRow);

                    log.warn("Skipping invalid CSV row {} in {}: {}",
                            rowNumber, inputPath.getFileName(),
                            e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to read CSV file: " + inputPath, e);
        }

        log.info("Import completed. Imported: {}, skipped: {}", allAnimals.size(), skippedRows);

        return new ImportResult(allAnimals, skippedRows, invalidRows);
    }
}
