package net.issachanzi.resteasy.model;

/**
 * Access type as in CRUD
 *
 * CREATE, READ, UPDATE or DELETE
 *
 * Corresponds to POST, GET, PUT and DELETE methods of HTTP
 *
 */
public enum AccessType {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    CUSTOM_METHOD
}
