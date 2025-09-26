package com.paragon.application.commands;

public interface CommandHandler<TCommand, TCommandResponse> {
    TCommandResponse handle(TCommand command);
}
