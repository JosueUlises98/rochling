package org.kopingenieria.application.service.opcua.workflow.user;


import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.domain.model.user.UserConfigurationOpcUa;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;

public interface UserConfiguration {

    OpcUaClient createUserOpcUaClient(UserConfigurationOpcUa useropcua) throws OpcUaConfigurationException;

}
