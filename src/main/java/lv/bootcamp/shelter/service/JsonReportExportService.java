package lv.bootcamp.shelter.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonReportExportService {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void writeJsonReport(Path outputPath, ShelterReportData reportData) {
        JsonObject report = new JsonObject();

        report.addProperty("totalImported", reportData.importResult().allAnimals().size());

        report.addProperty("totalSkipped", reportData.importResult().skippedRows());

        JsonArray speciesArray = new JsonArray();

        for (String species : reportData.uniqueSpecies()) {
            JsonObject speciesData = new JsonObject();

            speciesData.addProperty("species", species);

            speciesData.addProperty("total", reportData.animalsBySpecies().get(species).size());

            speciesData.addProperty("vaccinated", reportData.vaccinatedCounts().getOrDefault(species, 0L));

            speciesData.addProperty("unvaccinated", reportData.unvaccinatedCounts().getOrDefault(species, 0L));

            Animal oldest = reportData.oldestAnimalsBySpecies().get(species);

            if (oldest != null) {
                speciesData.addProperty("oldestAnimal", oldest.getName());
                speciesData.addProperty("oldestAge", oldest.getAge());
            } else {
                speciesData.add("oldestAnimal", null);
                speciesData.add("oldestAge", null);
            }

            speciesArray.add(speciesData);
        }

        report.add("species", speciesArray);

        JsonArray needsVetArray = new JsonArray();

        for (String animal : reportData.animalsNeedingVetInput()) {
            needsVetArray.add(animal);
        }

        report.add("animalsNeedingVetInput", needsVetArray);

        JsonArray invalidRowsArray = new JsonArray();

        for (String invalidRow : reportData.importResult().invalidRows()) {
            invalidRowsArray.add(invalidRow);
        }

        report.add("invalidRows", invalidRowsArray);

        try {
            Path parent = outputPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    outputPath,
                    gson.toJson(report),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to write JSON report: " + outputPath, e
            );
        }
    }
}
