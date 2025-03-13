package org.kopingenieria.mapper;

import org.kopingenieria.model.entity.AuditEvent;
import org.kopingenieria.model.dto.AuditEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditMapper INSTANCE = Mappers.getMapper(AuditMapper.class);

    AuditEventDTO toDto(AuditEvent auditEvent);

    AuditEvent toEntity(AuditEventDTO auditEventDTO);

}
