package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
public class DefaultOpcUa implements Serializable {
    @Serial
    private static final long serialVersionUID = 7L;

    private final String id;
    private final String name;
    private final DefaultConnectionConfiguration connection;
    private final DefaultSessionConfiguration session;
    private final DefaultIndustrialConfiguration industrial;
}
