package com.paragon.application.queries.repositoryinterfaces;

public interface QueryHandler<TQuery, TQueryResponse> {
    TQueryResponse handle(TQuery query);
}
