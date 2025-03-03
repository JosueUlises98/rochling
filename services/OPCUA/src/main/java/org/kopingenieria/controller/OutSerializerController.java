package org.kopingenieria.controller;

import org.kopingenieria.domain.classes.serialization.out.serializables.OutSerializable;
import org.kopingenieria.services.OutSerializerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/out-serializer")
public class OutSerializerController {

    private final OutSerializerService<OutSerializable> outSerializerService;

    public OutSerializerController(OutSerializerService<OutSerializable> outSerializerService) {
        this.outSerializerService = outSerializerService;
    }

    @PostMapping("/test")
    public ResponseEntity<byte[]> serialize(@RequestBody OutSerializable serializable){
        try {
            return ResponseEntity.ok(outSerializerService.serialize(serializable));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
