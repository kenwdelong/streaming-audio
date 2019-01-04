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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.DefaultStreamConnector;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.neomedia.MediaStream;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.MediaUseCase;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.util.Logger;

import com.hatchbaby.streaming.model.ClientType;

/**
 * Implements an example application in the fashion of JMF's AVTransmit2 example
 * which demonstrates the use of the <tt>libjitsi</tt> library for the purposes
 * of transmitting audio and video via RTP means.
 *
 * @author Lyubomir Marinov
 */
public class Transceiver
{
	private final Logger logger = Logger.getLogger(getClass());
	
	/**
	 * The port which is the source of the transmission i.e. from which the
	 * media is to be transmitted.
	 *
	 * @see #LOCAL_PORT_BASE_ARG_NAME
	 */
	private final int localPortBase;

	private MediaStream mediaStream;
	
	private ClientType clientType;

	/**
	 * The <tt>InetAddress</tt> of the host which is the target of the
	 * transmission i.e. to which the media is to be transmitted.
	 *
	 * @see #REMOTE_HOST_ARG_NAME
	 */
	private InetAddress remoteAddr;

	/**
	 * The port which is the target of the transmission i.e. to which the media
	 * is to be transmitted.
	 *
	 * @see #REMOTE_PORT_BASE_ARG_NAME
	 */
	private final int remotePortBase;

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
	public Transceiver(int localPortBase, String remoteHost, int remotePortBase, ClientType clientType) throws UnknownHostException
	{
		this.localPortBase = localPortBase;
		this.remoteAddr = InetAddress.getByName(remoteHost);
		this.remotePortBase = remotePortBase;
		this.clientType = clientType;
	}

	public void start() throws Exception
	{
		MediaService mediaService = LibJitsi.getMediaService();

		MediaType mediaType = MediaType.AUDIO;
		MediaDevice device = mediaService.getDefaultDevice(mediaType, MediaUseCase.CALL);
		mediaStream = mediaService.createMediaStream(device);

		mediaStream.setDirection(clientType.getDirection());

		// format
		String encoding = "PCMU";;
		double clockRate = 8000;
        MediaFormat format = mediaService.getFormatFactory().createMediaFormat(encoding, clockRate);
        mediaStream.setFormat(format);
        
		int localRTPPort = localPortBase;
		int localRTCPPort = localRTPPort + 1;
		logger.info("Creating stream on ports " + localRTPPort + " and " + localRTCPPort);
		StreamConnector connector = new DefaultStreamConnector(new DatagramSocket(localRTPPort), new DatagramSocket(localRTCPPort));
		mediaStream.setConnector(connector);

		int remoteRTPPort = remotePortBase;
		int remoteRTCPPort = remoteRTPPort + 1;

		InetSocketAddress rtpTarget = new InetSocketAddress(remoteAddr, remoteRTPPort);
		InetSocketAddress rtcpTarget = new InetSocketAddress(remoteAddr, remoteRTCPPort);
		MediaStreamTarget target = new MediaStreamTarget(rtpTarget, rtcpTarget);
		mediaStream.setTarget(target);

		/*
		 * The name is completely optional and it is not being used by the
		 * MediaStream implementation at this time, it is just remembered so
		 * that it can be retrieved via MediaStream#getName(). It may be
		 * integrated with the signaling functionality if necessary.
		 */
		mediaStream.setName(mediaType.toString());

		/*
		 * Do start the transmission i.e. start the initialized MediaStream
		 */
		if(mediaStream != null) mediaStream.start();
	}

	public void stop()
	{
		if(mediaStream != null)
		{
			try
			{
				mediaStream.stop();
			}
			finally
			{
				mediaStream.close();
			}
		}
		mediaStream = null;
	}

	public static void main(String[] args) throws Exception
	{
		LibJitsi.start();
		
		// local test
		Transceiver tx = new Transceiver(5100, "localhost", 5200, ClientType.Tx);
		Transceiver rx = new Transceiver(5200, "localhost", 5100, ClientType.Rx);
		
		tx.start();
		rx.start();
		
		Thread.sleep(10_000);
		
		tx.stop();
		rx.stop();
		
		LibJitsi.stop();
		System.exit(0);
	}
}
