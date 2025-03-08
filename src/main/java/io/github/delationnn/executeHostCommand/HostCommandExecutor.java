package io.github.delationnn.executeHostCommand;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class HostCommandExecutor implements SimpleCommand {
    private final ProxyServer server;

    public HostCommandExecutor(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // Debug log
        source.sendMessage(Component.text("Command received: " + String.join(" ", args)));

        if (!source.hasPermission("hostcommandexecutor.execute")) {
            source.sendMessage(Component.text("You don't have permission to execute host commands."));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(Component.text("Usage: /executehostcommand <command>"));
            return;
        }

        String command = String.join(" ", args);
        executeHostCommand(command, source);
    }

    private void executeHostCommand(String command, CommandSource source) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                source.sendMessage(Component.text(line));
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            source.sendMessage(Component.text("Error executing command: " + e.getMessage()));
            // Consider logging the error here as well
        }
    }
}
