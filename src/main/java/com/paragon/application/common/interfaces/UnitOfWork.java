package com.paragon.application.common.interfaces;

public interface UnitOfWork {
    void begin();
    void commit();
    void rollback();
}