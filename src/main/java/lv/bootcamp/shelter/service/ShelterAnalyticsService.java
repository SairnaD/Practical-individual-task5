package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.util.*;
import java.util.stream.Collectors;

public class ShelterAnalyticsService {

    public ShelterReportData buildReportData(ImportResult importResult) {
        List<Animal> allAnimals = importResult.allAnimals();

        Set<String> uniqueSpecies = new TreeSet<>();
        Map<String, List<Animal>> animalsBySpecies = new HashMap<>();
        List<String> animalsNeedingVetInput = new ArrayList<>();

        for (Animal animal : allAnimals) {
            uniqueSpecies.add(animal.getSpecies());

            animalsBySpecies.computeIfAbsent(animal.getSpecies(),
                    species -> new ArrayList<>())
                    .add(animal);

            if (animal.getAge() == null) {
                animalsNeedingVetInput.add(
                        animal.getName() + "(" + animal.getSpecies() + ")"
                );
            }
        }

        Map<String, Long> vaccinatedCounts = allAnimals.stream()
                .filter(Animal::isVaccinated)
                .collect(Collectors.groupingBy(
                        Animal::getSpecies,
                        Collectors.counting()
                ));

        Map<String, Long> unvaccinatedCounts = allAnimals.stream()
                .filter(animal -> !animal.isVaccinated())
                .collect(Collectors.groupingBy(
                        Animal::getSpecies,
                        Collectors.counting()
                ));

        Map<String, Animal> oldestAnimalsBySpecies = allAnimals.stream()
                .filter(animal -> animal.getAge() != null)
                .collect(Collectors.groupingBy(
                        Animal::getSpecies,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(
                                        java.util.Comparator.comparingInt(
                                                Animal::getAge
                                        )
                                ),
                                optional -> optional.orElse(null)
                        )
                ));

        return new ShelterReportData(
                importResult,
                allAnimals,
                uniqueSpecies,
                animalsBySpecies,
                animalsNeedingVetInput,
                vaccinatedCounts,
                unvaccinatedCounts,
                oldestAnimalsBySpecies
        );

    }
}
