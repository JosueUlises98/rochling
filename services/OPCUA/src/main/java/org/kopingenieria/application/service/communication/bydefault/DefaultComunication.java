package org.kopingenieria.application.service.communication.bydefault;

import org.kopingenieria.api.request.communication.CommunicationRequest;
import org.kopingenieria.api.response.communication.OpcUaCommunicationResponse;


public interface DefaultComunication {

    OpcUaCommunicationResponse lectura(CommunicationRequest request)throws Exception;

    OpcUaCommunicationResponse escritura(CommunicationRequest request)throws Exception;

    OpcUaCommunicationResponse modificacion(CommunicationRequest request)throws Exception;

    OpcUaCommunicationResponse eliminacion(CommunicationRequest request)throws Exception;
}
