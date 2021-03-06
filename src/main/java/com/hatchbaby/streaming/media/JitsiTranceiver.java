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

import com.hatchbaby.streaming.model.ClientType;

/**
 * Implements an example application in the fashion of JMF's AVTransmit2 example
 * which demonstrates the use of the <tt>libjitsi</tt> library for the purposes
 * of transmitting audio and video via RTP means.
 *
 * @author Lyubomir Marinov
 */
public class JitsiTranceiver extends Transceiver
{
	private MediaStream mediaStream;
	
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
	public JitsiTranceiver(int localPortBase, String remoteHost, int remotePortBase, ClientType clientType) throws UnknownHostException
	{
		super(localPortBase, remoteHost, remotePortBase, clientType);
		logger.info("Creating JitsiTransceiver");
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
        
		int localRTPPort = localPortBase + 1;
		int localRTCPPort = localRTPPort + 1;
		logger.info("Creating stream on ports " + localRTPPort + " and " + localRTCPPort);
		DatagramSocket rtpSocket = new DatagramSocket(localRTPPort);
		DatagramSocket rtcpSocket = new DatagramSocket(localRTCPPort);
		StreamConnector connector = new DefaultStreamConnector(rtpSocket, rtcpSocket);
		mediaStream.setConnector(connector);

		int remoteRTPPort = remotePortBase + 1;
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
		JitsiTranceiver tx = new JitsiTranceiver(5100, "localhost", 5200, ClientType.Tx);
		JitsiTranceiver rx = new JitsiTranceiver(5200, "localhost", 5100, ClientType.Rx);
		
		tx.start();
		rx.start();
		
		Thread.sleep(10_000);
		
		tx.stop();
		rx.stop();
		
		LibJitsi.stop();
		System.exit(0);
	}
}
