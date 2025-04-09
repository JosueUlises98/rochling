package org.kopingenieria.application.service.communication.bydefault;

import org.kopingenieria.api.request.communication.CommunicationRequest;
import org.kopingenieria.api.response.communication.CommunicationResponse;


public interface DefaultComunication {

    CommunicationResponse lectura(CommunicationRequest request)throws Exception;

    CommunicationResponse escritura(CommunicationRequest request)throws Exception;

    CommunicationResponse modificacion(CommunicationRequest request)throws Exception;

    CommunicationResponse eliminacion(CommunicationRequest request)throws Exception;
}
