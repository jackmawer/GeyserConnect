/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 *  @author GeyserMC
 *  @link https://github.com/GeyserMC/GeyserConnect
 *
 */

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
