package org.kopingenieria.model.classes;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonRootName(value = "opcua-configuration-data")
@XmlRootElement(name = "opcua-configuration-data")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({"opcua-configuration", "configuration-timestamp","configuration-metadata"})
public class OpcUaConfigurationSerializable extends OpcUaSerializable{

}
