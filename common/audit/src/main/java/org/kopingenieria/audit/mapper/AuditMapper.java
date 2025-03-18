package org.kopingenieria.audit.mapper;

import org.kopingenieria.audit.exceptions.MappingException;
import org.kopingenieria.audit.model.entity.AuditEvent;
import org.kopingenieria.audit.model.dto.AuditEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditMapper INSTANCE = Mappers.getMapper(AuditMapper.class);

    AuditEventDTO toDto(AuditEvent auditEvent)throws MappingException;

    AuditEvent toEntity(AuditEventDTO auditEventDTO)throws MappingException;

}
