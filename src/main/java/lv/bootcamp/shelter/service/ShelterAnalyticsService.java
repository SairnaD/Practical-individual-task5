package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShelterAnalyticsService {

    public ShelterReportData buildReportData(ImportResult importResult) {
        List<Animal> allAnimals = importResult.allAnimals();

        Set<String> uniqueSpecies = allAnimals.stream()
                .map(Animal::getSpecies)
                .collect(Collectors.toCollection(TreeSet::new));

        Map<String, List<Animal>> animalsBySpecies = allAnimals.stream()
                .collect(Collectors.groupingBy(
                        Animal::getSpecies,
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<String> animalsNeedingVetInput = allAnimals.stream()
                .filter(animal -> animal.getAge() == null)
                .map(animal -> animal.getName() + "(" + animal.getSpecies() + ")")
                .toList();

        Map<String, Long> vaccinatedCounts = allAnimals.stream()
                .filter(Animal::isVaccinated)
                .collect(Collectors.groupingBy(
                        Animal::getSpecies,
                        TreeMap::new,
                        Collectors.counting()
                ));

        Map<String, Long> unvaccinatedCounts = allAnimals.stream()
                .filter(animal -> !animal.isVaccinated())
                .collect(Collectors.groupingBy(
                        Animal::getSpecies,
                        TreeMap::new,
                        Collectors.counting()
                ));

        Map<String, Animal> oldestAnimalsBySpecies = allAnimals.stream()
                .filter(animal -> animal.getAge() != null)
                .collect(Collectors.toMap(
                        Animal::getSpecies,
                        Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparingInt(Animal::getAge)),
                        TreeMap::new
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
