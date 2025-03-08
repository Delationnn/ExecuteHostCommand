package io.github.delationnn.executeHostCommand;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

@Plugin(id = "executehostcommand", name = "ExecuteHostCommand", version = BuildConstants.VERSION, authors = {"Longwise (because yes, I am very long)"})
public class ExecuteHostCommandPlugin {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public ExecuteHostCommandPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getCommandManager().register("executehostcommand",
                new HostCommandExecutor(), "ehc");
    }

    private class HostCommandExecutor implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] args = invocation.arguments();

            if (!source.hasPermission("executehostcommand.execute")) {
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
                logger.error("Error executing host command", e);
            }
        }
    }
}
