package org.kopingenieria.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.kopingenieria.application.service.files.OpcUaConfigFile;
import org.kopingenieria.config.OpcUaConfiguration;
import org.kopingenieria.exception.exceptions.ConfigurationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/v1/opcua/configurationfile")
public class OpcUaConfigurationFileController {

    private final OpcUaConfigFile configurationService;
    private final ObjectMapper objectMapper;

    @PostMapping("/init")
    public ResponseEntity<Void> initializeSystem() {
        try {
            configurationService.init();
            return ResponseEntity.ok().build();
        } catch (ConfigurationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get/{filename}")
    public ResponseEntity<OpcUaConfiguration> getConfiguration(@PathVariable @NotBlank String filename) {
        try {
            OpcUaConfiguration config = configurationService.loadConfiguration(filename);
            return ResponseEntity.ok(config);
        } catch (ConfigurationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create/{filename}")
    public ResponseEntity<Void> createConfiguration(
            @PathVariable @NotBlank String filename,
            @RequestBody @Valid OpcUaConfiguration configuration) {
        try {
            configurationService.saveConfiguration(configuration, filename);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ConfigurationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{filename}")
    public ResponseEntity<Void> updateConfiguration(
            @PathVariable @NotBlank String filename,
            @RequestBody @Valid OpcUaConfiguration configuration) {
        try {
            configurationService.updateConfiguration(configuration, filename);
            return ResponseEntity.ok().build();
        } catch (ConfigurationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<Void> deleteConfiguration(
            @PathVariable @NotBlank String filename) {
        try {
            configurationService.deleteConfiguration(filename);
            return ResponseEntity.noContent().build();
        } catch (ConfigurationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listConfigurations() {
        try {
            List<String> configurations = configurationService.listConfigurations();
            return ResponseEntity.ok(configurations);
        } catch (ConfigurationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadConfiguration(
            @RequestParam("file") MultipartFile file) {
        try {
            OpcUaConfiguration config = objectMapper.readValue(file.getInputStream(), OpcUaConfiguration.class);
            configurationService.saveConfiguration(config, file.getOriginalFilename());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateConfiguration(
            @RequestBody @Valid OpcUaConfiguration configuration) {
        try {
            // Aquí iría la lógica de validación
            Map<String, Object> validationResult = Map.of(
                    "isValid", true,
                    "message", "Configuración válida"
            );
            return ResponseEntity.ok(validationResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "isValid", false,
                    "message", e.getMessage()
            ));
        }
    }
}
