package com.example.minestomstarter.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;

public class PingCommand extends Command {
    public PingCommand() {
        super("ping");

        //When no args are provide or nothing matches
        setDefaultExecutor((sender, commandContext) -> {
            sender.sendMessage("Pong!");
        });

        var pingAmountArg = ArgumentType.Integer("pingamount");
        addSyntax((sender, commandContext) -> {
            //Get int from context
            int pingAmount = commandContext.get(pingAmountArg);
            for (int i = 1; i <= pingAmount; i++) {
                if((i % 2) == 1 ) {
                    sender.sendMessage("Pong!");
                }
                else  {
                    sender.sendMessage("Ping!");
                }
            }
        }, pingAmountArg);
    }
}
