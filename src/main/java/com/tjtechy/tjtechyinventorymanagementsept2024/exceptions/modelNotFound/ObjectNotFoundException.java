package com.tjtechy.tjtechyinventorymanagementsept2024.exceptions.modelNotFound;

import java.util.UUID;

/*In the future, I will use this exception class for all class model e.g Author, Books users etc get
* Remember to implement also in ExceptionHandleAdvice
* */

public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(String objectName, Long id) {
        super("Could not find " + objectName + " with Id " + id);

    }

    public ObjectNotFoundException(String objectName, UUID id) {
        super("Could not find " + objectName + " with Id " + id);

    }
}
