package io.github.delationnn.executeHostCommand;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;

public class HostCommandExecutor implements SimpleCommand {
    private final ProxyServer server;
    private final Logger logger;
    private final Map<UUID, Process> activeProcesses = new HashMap<>();

    public HostCommandExecutor(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        server.getEventManager().register(this, this);
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // Debug log
        logger.info("Command received: " + String.join(" ", args));

        if (!source.hasPermission("hostcommandexecutor.execute")) {
            source.sendMessage(Component.text("You don't have permission to execute host commands."));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(Component.text("Usage: /executehostcommand <command> [--input]"));
            return;
        }

        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        boolean useInput = argsList.remove("--input");
        String command = String.join(" ", argsList);

        executeHostCommand(command, source, useInput);
    }

    private void executeHostCommand(String command, CommandSource source, boolean useInput) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "))
                    .redirectErrorStream(true);
            Process process = processBuilder.start();

            if (useInput && source instanceof Player) {
                Player player = (Player) source;
                UUID playerId = player.getUniqueId();
                activeProcesses.put(playerId, process);
                source.sendMessage(Component.text("Input mode active - type '!end' to finish"));
            }

            // Async output reading
            server.getScheduler().buildTask(this, () -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        source.sendMessage(Component.text(line));
                    }
                    process.waitFor();
                } catch (IOException | InterruptedException e) {
                    source.sendMessage(Component.text("Error reading output: " + e.getMessage()));
                    logger.error("Error reading process output", e);
                } finally {
                    if (source instanceof Player) {
                        activeProcesses.remove(((Player) source).getUniqueId());
                    }
                }
            }).schedule();

        } catch (IOException e) {
            source.sendMessage(Component.text("Error executing command: " + e.getMessage()));
            logger.error("Error executing host command", e);
        }
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();

        if (activeProcesses.containsKey(playerId)) {
            event.setResult(PlayerChatEvent.ChatResult.denied());

            try {
                Process process = activeProcesses.get(playerId);
                if (message.equalsIgnoreCase("!end")) {
                    activeProcesses.remove(playerId);
                    process.getOutputStream().close();
                    player.sendMessage(Component.text("Input closed"));
                } else {
                    OutputStream output = process.getOutputStream();
                    output.write((message + "\n").getBytes());
                    output.flush();
                }
            } catch (IOException e) {
                player.sendMessage(Component.text("Input error: " + e.getMessage()));
                activeProcesses.remove(playerId);
                logger.error("Error handling player input", e);
            }
        }
    }
}
