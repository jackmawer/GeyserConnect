package org.geysermc.connect.proxy;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.ConnectorServerEventHandler;
import org.geysermc.connector.network.UpstreamPacketHandler;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Player;

public class ProxyConnectorServerEventHandler extends ConnectorServerEventHandler {

    private final GeyserConnector connector;

    public ProxyConnectorServerEventHandler(GeyserConnector connector) {
        super(connector);
        this.connector = connector;
        MasterServer.getInstance().getLogger().debug("Registered custom ConnectorServerEventHandler");
    }

    @Override
    public void onSessionCreation(BedrockServerSession bedrockServerSession) {
        bedrockServerSession.setPacketCodec(GeyserConnector.BEDROCK_PACKET_CODEC); // Only done here as it allows us to disconnect the player

        Player player = MasterServer.getInstance().getPlayers().get(bedrockServerSession.getAddress());
        if (player == null) {
            bedrockServerSession.disconnect("Please connect to the master server and pick a server first!");
            return;
        }

        super.onSessionCreation(bedrockServerSession);

        // This doesn't clean up the old packet handler, so may cause a memory leak?
        bedrockServerSession.setPacketHandler(new UpstreamPacketHandler(connector, new GeyserProxySession(connector, bedrockServerSession)));

        // Add another disconnect handler to remove the player on final disconnect
        bedrockServerSession.addDisconnectHandler(disconnectReason -> {
            MasterServer.getInstance().getLogger().debug("Player disconnected from geyser proxy: " + player.getDisplayName() + " (" + disconnectReason + ")");
            MasterServer.getInstance().getPlayers().remove(bedrockServerSession.getAddress());
        });
    }
}