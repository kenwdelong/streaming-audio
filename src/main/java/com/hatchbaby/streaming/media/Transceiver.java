/*
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hatchbaby.streaming.media;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jitsi.util.Logger;

import com.hatchbaby.streaming.model.ClientType;
import com.hatchbaby.streaming.model.MediaServer;

public abstract class Transceiver
{
	protected static final Logger logger = Logger.getLogger(getClass());
	
	/**
	 * The port which is the source of the transmission i.e. from which the
	 * media is to be transmitted.
	 *
	 * @see #LOCAL_PORT_BASE_ARG_NAME
	 */
	protected int localPortBase;

	protected ClientType clientType;

	/**
	 * The <tt>InetAddress</tt> of the host which is the target of the
	 * transmission i.e. to which the media is to be transmitted.
	 *
	 * @see #REMOTE_HOST_ARG_NAME
	 */
	protected InetAddress remoteAddr;

	/**
	 * The port which is the target of the transmission i.e. to which the media
	 * is to be transmitted.
	 *
	 * @see #REMOTE_PORT_BASE_ARG_NAME
	 */
	protected int remotePortBase;

	protected Transceiver(int localPortBase, String remoteHost, int remotePortBase, ClientType clientType) throws UnknownHostException
	{
		this.localPortBase = localPortBase;
		this.remoteAddr = InetAddress.getByName(remoteHost);
		this.remotePortBase = remotePortBase;
		this.clientType = clientType;
	}
	
	/**
	 * @param localPortBase
	 *            the port which is the source of the transmission i.e. from
	 *            which the media is to be transmitted
	 * @param remoteHost
	 *            the name of the host which is the target of the transmission
	 *            i.e. to which the media is to be transmitted
	 * @param remotePortBase
	 *            the port which is the target of the transmission i.e. to which
	 *            the media is to be transmitted
	 * @throws UnknownHostException 
	 *             
	 */
	public static Transceiver create(int localPortBase, String remoteHost, int remotePortBase, ClientType clientType, MediaServer media) throws UnknownHostException
	{
		logger.info("Local port base: " + localPortBase);
		logger.info("Remote host: " + remoteHost);
		logger.info("Remote port base: " + remotePortBase);
		logger.info("Client type: " + clientType);
		logger.info("Media server: " + media);
		if(media == MediaServer.jistsi)
		{
			return new JitsiTranceiver(localPortBase, remoteHost, remotePortBase, clientType);
		}
		else if(media == MediaServer.ffmpeg)
		{
			return new FfmpegTranceiver(localPortBase, remoteHost, remotePortBase, clientType);
		}
		else
		{
			throw new IllegalArgumentException("No media server for [" + media + "]");
		}
	}

	public abstract void start() throws Exception;

	public abstract void stop();

}
