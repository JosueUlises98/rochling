package org.kopingenieria.model.classes;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.kopingenieria.model.enums.tcp.IpVersion;

@Entity
@Getter
@Setter
public class TcpConfiguration extends Configuration<TcpConfiguration> {
    // Atributos específicos TCP
    private boolean keepAliveEnabled;
    private Integer keepAliveTime;
    private Integer keepAliveInterval;
    private Integer keepAliveRetries;

    private boolean noDelayEnabled;
    private Integer sendBufferSize;
    private Integer receiveBufferSize;

    private String bindAddress;
    private boolean reuseAddress;
    private Integer connectionTimeout;

    @Enumerated(EnumType.STRING)
    private IpVersion version; // IPv4/IPv6
}
