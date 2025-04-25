package com.example.summar_ai.services;


@FunctionalInterface
public interface AuthenticatedApiCall<T> {
    T call(String accessToken) throws Exception;
}
