package org.kopingenieria.model.classes;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.kopingenieria.model.enums.opcua.MessageSecurityMode;
import org.kopingenieria.model.enums.opcua.SecurityPolicy;

public class OpcUaAuthentication extends Authentication<OpcUaAuthentication>{

    // Configuración de seguridad
    @Enumerated(EnumType.STRING)
    @Column(name = "security_policy")
    private SecurityPolicy securityPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_security_mode")
    private MessageSecurityMode messageSecurityMode;

    @Column(name = "certificate_path")
    private String certificatePath;

    @Column(name = "private_key_path")
    private String privateKeyPath;

    @Column(name = "trust_list_path")
    private String trustListPath;

    @Column(name = "issuer_list_path")
    private String issuerListPath;

    @Column(name = "revocation_list_path")
    private String revocationListPath;

}
