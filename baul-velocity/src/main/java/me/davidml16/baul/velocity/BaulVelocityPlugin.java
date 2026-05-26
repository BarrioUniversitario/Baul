package me.davidml16.baul.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import com.google.inject.Inject;

@Plugin(
        id = "baul-velocity",
        name = "BaulVelocity",
        version = "3.0.0",
        description = "Cross-server sync forwarder for Baul plugin",
        authors = {"DavidML16"}
)
public class BaulVelocityPlugin {

    public static final String CHANNEL = "baul:sync";
    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.create("baul", "sync");

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public BaulVelocityPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(IDENTIFIER);
        logger.info("BaulVelocity enabled! Listening on channel: " + CHANNEL);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().getId().equals(CHANNEL)) return;

        if (!(event.getSource() instanceof ServerConnection)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        byte[] data = event.getData();

        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        in.readUTF(); // action
        in.readUTF(); // uuid
        String sourceServer = in.readUTF(); // sourceServer

        for (com.velocitypowered.api.proxy.server.RegisteredServer registeredServer : server.getAllServers()) {
            String serverName = registeredServer.getServerInfo().getName();
            if (serverName.equals(sourceServer)) continue;
            registeredServer.sendPluginMessage(IDENTIFIER, data);
        }
    }
}
