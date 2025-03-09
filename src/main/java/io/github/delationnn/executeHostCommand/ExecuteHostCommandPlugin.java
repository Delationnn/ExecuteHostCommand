package io.github.delationnn.executeHostCommand;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;
import com.velocitypowered.api.proxy.ProxyServer;

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
        HostCommandExecutor commandExecutor = new HostCommandExecutor(server, logger);
        server.getCommandManager().register("executehostcommand", commandExecutor, "ehc");
    }
}
