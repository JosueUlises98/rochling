package org.kopingenieria.api.controller;

import lombok.RequiredArgsConstructor;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.kopingenieria.application.service.opcua.workflow.user.UserConfigurationImpl;
import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/v1/opcua/configuration")
public class OpcUaConfigurationController {

    private final UserConfigurationImpl userConfiguration;
    private final DefaultConfiguration defaultConfiguration;

    @PostMapping("/client/default")
    public ResponseEntity<?> createDefaultClient() {
        try {
            OpcUaClient client = defaultConfiguration.createDefaultOpcUaClient();
            return ResponseEntity.ok("Cliente OPC UA predeterminado creado exitosamente");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear cliente OPC UA predeterminado: " + e.getMessage());
        }
    }

    @PostMapping("/client/user")
    public ResponseEntity<?> createUserClient() {
        try {
            OpcUaClient client = opcUaConfiguration.createUserOpcUaClient();
            return ResponseEntity.ok("Cliente OPC UA de usuario creado exitosamente");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear cliente OPC UA de usuario: " + e.getMessage());
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<Map<OpcUaClient, List<UaSubscription>>> getAllSubscriptions() {
        try {
            Map<OpcUaClient, List<UaSubscription>> subscriptions = opcUaConfiguration.getMapSubscriptions();
            if (subscriptions == null || subscriptions.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
