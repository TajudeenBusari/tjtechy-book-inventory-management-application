package com.tjtechy.tjtechyinventorymanagementsept2024.system;

public class StatusCode {
    public static final int SUCCESS = 200;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_FOUND = 404;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    //public static final int INVALID_ARGUMENT = 400;
}
/*
* This is class is not really needed since the Spring has a HttpStatus class, but
* it is good to have a custom class like this in ase your company wants to define some
* custom status code
*
* */