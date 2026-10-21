package lv.bootcamp.shelter;

import lv.bootcamp.shelter.service.*;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ShelterCsvApplication {

    public static void main(String[] args) {
        Path inputDirectory = Path.of("src", "main", "resources", "data");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));

        Path outputPath = Path.of("output", "upload-report-" + timestamp + ".txt");

        Path jsonOutputPath = Path.of("output", "upload-report-" + timestamp + ".json");

        ShelterAnalyticsService analyticsService = new ShelterAnalyticsService();
        ReportExportService reportExportService = new ReportExportService();
        JsonReportExportService jsonExportService =  new JsonReportExportService();
        CsvBatchImportService batchImportService = new CsvBatchImportService();

        ImportResult importResult = batchImportService.importAll(inputDirectory);
        ShelterReportData reportData = analyticsService.buildReportData(importResult);

        reportExportService.writeReport(outputPath, reportData);
        jsonExportService.writeJsonReport(jsonOutputPath, reportData);
    }
}
