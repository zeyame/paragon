package com.paragon.application.queries;

public interface QueryHandler<TQuery, TQueryResponse> {
    TQueryResponse handle(TQuery query);
}
