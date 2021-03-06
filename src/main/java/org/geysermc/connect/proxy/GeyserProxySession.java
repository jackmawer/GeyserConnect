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
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connect.MasterServer;
import org.geysermc.connect.utils.Player;

public class GeyserProxySession extends GeyserSession {

    private final GeyserConnector connector;
    private final BedrockServerSession bedrockServerSession;

    public GeyserProxySession(GeyserConnector connector, BedrockServerSession bedrockServerSession) {
        super(connector, bedrockServerSession);
        this.connector = connector;
        this.bedrockServerSession = bedrockServerSession;
    }

    public void authenticate(String username, String password) {
        // Get the player based on the connection address
        Player player = MasterServer.getInstance().getPlayers().get(bedrockServerSession.getAddress());
        if (player != null && player.getCurrentServer() != null) {
            // Set the remote server info for the player
            connector.getRemoteServer().setAddress(player.getCurrentServer().getAddress());
            connector.getRemoteServer().setPort(player.getCurrentServer().getPort());
            super.authenticate(username, password);
        }else{
            // Disconnect the player if they haven't picked a server on the master server list
            bedrockServerSession.disconnect("Please connect to the master server and pick a server first!");
        }
    }
}
