package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ReportExportService {

    public void writeReport(Path outputPath, ShelterReportData reportData) {

        try {
            Path parent = outputPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {

                writer.write("=== Shelter Intake Report ===");
                writer.newLine();

                writer.write("Date generated: " + LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                writer.newLine();

                writer.write("Total imported: "
                        + reportData.importResult().allAnimals().size());
                writer.newLine();

                writer.write("Total skipped: "
                        + reportData.importResult().skippedRows());
                writer.newLine();

                writer.write("--- Invalid rows ---");
                writer.newLine();

                if (reportData.importResult().invalidRows().isEmpty()) {
                    writer.write("None");
                } else {
                    writer.write(String.join(", ", reportData.importResult().invalidRows()));
                }
                writer.newLine();

                writer.write("--- Unique species ---");
                writer.newLine();
                writer.write(String.join(", ", reportData.uniqueSpecies()));
                writer.newLine();

                writer.write("--- Per-species breakdown ---");
                writer.newLine();

                for (String species : reportData.uniqueSpecies()) {
                    List<Animal> animals = reportData.animalsBySpecies().get(species);

                    long vaccinated = reportData.vaccinatedCounts()
                            .getOrDefault(species, 0L);

                    writer.write(species + ":");
                    writer.newLine();

                    writer.write(animals.size() + " total, " + vaccinated + " vaccinated");
                    writer.newLine();
                }

                writer.write("--- Oldest per species ---");
                writer.newLine();

                for (String species : reportData.uniqueSpecies()) {
                    Optional<Animal> oldest = Optional.ofNullable(
                            reportData.oldestAnimalsBySpecies().get(species)
                    );

                    writer.write(species + ":");
                    writer.newLine();

                    if (oldest.isPresent()) {
                        Animal animal = oldest.get();
                        writer.write(animal.getName() + " (age " + animal.getAge() + ")");
                    } else {
                        writer.write("No known ages");
                    }

                    writer.newLine();
                }

                writer.write("--- Needs Vet input to determine age ---");
                writer.newLine();

                writer.write(String.join(", ", reportData.animalsNeedingVetInput()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report: " + outputPath, e);
        }
    }
}
