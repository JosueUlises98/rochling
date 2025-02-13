PASOS PARA IMPLEMENTAR LA APLICACION COMPLETA

1.-DEFINIR EL CODIGO FUENTE
2.-DEFINIR LAS ANOTACIONES PARA EL CODIGO FUENTE
3.-IMPLEMENTAR LOS TESTING UNITARIOS E INTEGRALES
4.-CONFIGURACION DE LOS COMPONENTES EXTERNOS ->GRAFANA,PROMETHEUS,
5.-TESTEAR CADA MODULO DE LA APLICACION
6.-EMPAQUETAR EN UN CONTENEDOR DE DOCKER EL MODULO 
8.-TESTEAR LA APLICACION DE END TO END
9.-CREAR MI ARCHIVO DOCKER-COMPOSE .YML
10.-DEFINIR UN WIZARD INSTALLATION
11.-PROGRAMAR EL WIZARD INSTALLATION
12.-CONFIGURAR EL WIZARD INSTALLATION
13.-TESTEAR A NIVEL DE USUARIO LA APLICACION DE END TO END CADA FUNCIONALIDAD
14.-FINALIZACION DE LA APLICACION

//PROCESOS A REALIZAR EN SERVICES

1.-SERVICES

1.1-DEFINIR UN DISEÑO DE COMPOSICION DE TIPOS DE SERVICIOS
Ejemplo:
Abstraccion:
Authentication
Implementación:
SSLAuthentication,TCP/IPAuthentication,etc.

1.2 DEFINIR UN METODO FLEXIBLE QUE IMPLEMENTE UN PATRON DE DISEÑO PARA ENVIAR CUALQUIER TIPO DE PROCESO
Método:submitTask();
Clase:ExecutorServicePool
Modulo:OpcUaClient
Paquete:org.kopingenieria.services
Proyecto:rochlingapp

1.3 DEFINIR CANAL TUNNELING TCP/IP PARA LOS DISTINTOS TIPOS DE SERVICIOS COMO:
TCP/IP Connection,SSLConnection
TCP/IP Autentication,SSLAutentication
TCP/IP Encryption,SSLEncryption
TCP/IP Session,SSLSession
TCP/IP Comunication,SSLComunication


// Intentamos conectar al cliente opcua
            opcUaClient = configuration.create(Url.Adress2.getUrl(),
                    new OpcUaClientConfigBuilder().setAcknowledgeTimeout(UInteger.MAX)
                            .setApplicationUri("opcuaclient")
                            .setApplicationName(LocalizedText.english("opcua-client"))
                            .setConnectTimeout(UInteger.valueOf(10000))
                            .setSessionName(()->new SessionObject(String.valueOf(UUID.randomUUID()),"default-session","user",SessionStatus.ACTIVE,"2025-01-10T23:59:59","2025-01-10T23:59:59").toString())
                            .build());

//Notas importantes sobre las conexiones opcua

1.-ConexionTCp tunnel tcp.opcua
el túnel tendra la lógica de conexión de la clase tcpconnection para procesar la conexión de un cliente tcp hacia un servidor opcua
2.-ConexionSSL tunnel tcp.opcua
el túnel tendra la lógica de conexión de la clase tcpconnection para procesar la conexión de un cliente tcp hacia un servidor opcua
junto con la seguridad de encriptación de la conexión y la comunicación.
3.-ConexionOPCUA 
Esta conexión será una conexión directa entre un opcuaclient y un opcuaserver,en donde no se establecen intermediarios como un tunnel tcp para la conexión.

//Notas importantes sobre la comunicación opcua

1.-Toda la comunicación será encapsulada en una clase que será utilizada por los tipos de conexión:tcp,ssl,opcua.

//Notas importantes sobre la autenticación opcua

1.-