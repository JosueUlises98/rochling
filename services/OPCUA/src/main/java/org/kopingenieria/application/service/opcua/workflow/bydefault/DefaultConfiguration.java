package org.kopingenieria.application.service.opcua.workflow.bydefault;


import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.domain.model.bydefault.DefaultConfigurationOpcUa;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;

public interface DefaultConfiguration {

    OpcUaClient createDefaultOpcUaClient(DefaultConfigurationOpcUa defaultopcua) throws OpcUaConfigurationException;

}
