package com.hatchbaby.streaming.ice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.sdp.Attribute;
import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.Agent;
import org.ice4j.ice.Candidate;
import org.ice4j.ice.CandidatePair;
import org.ice4j.ice.CandidateType;
import org.ice4j.ice.Component;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.RemoteCandidate;
import org.ice4j.ice.harvest.TurnCandidateHarvester;
import org.ice4j.ice.sdp.CandidateAttribute;
import org.ice4j.ice.sdp.IceSdpUtils;
import org.ice4j.security.LongTermCredential;
import org.jitsi.util.Logger;
import org.opentelecoms.javax.sdp.NistSdpFactory;

import com.hatchbaby.streaming.model.ClientType;

import rx.Observable;

public class IceClient
{
	private final Logger logger = Logger.getLogger(getClass());

	public Observable<CandidatePair> startIceDancing(int port, String sdpExchangeUrl, ClientType clientType, LongTermCredential turnCreds) throws Exception
	{
		Agent agent = new Agent();

		String[] hostnames = new String[] { "stun.l.google.com"};
		for(String hostname : hostnames)
		{
			TransportAddress ta = new TransportAddress(InetAddress.getByName(hostname), 19302, Transport.UDP);
			//agent.addCandidateHarvester(new StunCandidateHarvester(ta));
			agent.addCandidateHarvester(new StunHarvester(ta));
		}
		String turnHost = "global.turn.twilio.com";
		TransportAddress ta = new TransportAddress(InetAddress.getByName(turnHost), 3478, Transport.UDP);
		agent.addCandidateHarvester(new TurnCandidateHarvester(ta, turnCreds));
		

		IceMediaStream stream = agent.createMediaStream("audio");
		agent.createComponent(stream, Transport.UDP, port, port, port + 100);

		SdpFactory factory = new NistSdpFactory();
		SessionDescription sdess = factory.createSessionDescription();
		IceSdpUtils.initSessionDescription(sdess, agent);
		String toSend = sdess.toString();
		postToServer(sdpExchangeUrl, clientType, toSend);

		String remoteSdp = fetchSdpFromServer(sdpExchangeUrl, clientType);
		parseSDP(agent, remoteSdp); 
		
		StateListener listener = new StateListener();
		agent.addStateChangeListener(listener);
		agent.startConnectivityEstablishment();
		return listener.toObservable();
	}

	/**
	 * Configures <tt>localAgent</tt> the the remote peer streams, components,
	 * and candidates specified in <tt>sdp</tt>
	 *
	 * @param localAgent
	 *            the {@link Agent} that we'd like to configure.
	 *
	 * @param sdp
	 *            the SDP string that the remote peer sent.
	 *
	 * @throws Exception
	 *             for all sorts of reasons.
	 */
	@SuppressWarnings("unchecked") // jain-sdp legacy code.
	public static void parseSDP(Agent localAgent, String sdp) throws Exception
	{
		SdpFactory factory = new NistSdpFactory();
		SessionDescription sdess = factory.createSessionDescription(sdp);

		for(IceMediaStream stream : localAgent.getStreams())
		{
			stream.setRemotePassword(sdess.getAttribute("ice-pwd"));
			stream.setRemoteUfrag(sdess.getAttribute("ice-ufrag"));
		}

		Connection globalConn = sdess.getConnection();
		String globalConnAddr = null;
		if(globalConn != null)
			globalConnAddr = globalConn.getAddress();

		Vector<MediaDescription> mdescs = sdess.getMediaDescriptions(true);

		for(MediaDescription desc : mdescs)
		{
			String streamName = desc.getMedia().getMediaType();

			IceMediaStream stream = localAgent.getStream(streamName);

			if(stream == null)
				continue;

			Vector<Attribute> attributes = desc.getAttributes(true);
			for(Attribute attribute : attributes)
			{
				if(!attribute.getName().equals(CandidateAttribute.NAME))
					continue;

				parseCandidate(attribute, stream);
			}

			// set default candidates
			Connection streamConn = desc.getConnection();
			String streamConnAddr = null;
			if(streamConn != null)
				streamConnAddr = streamConn.getAddress();
			else
				streamConnAddr = globalConnAddr;

			int port = desc.getMedia().getMediaPort();

			TransportAddress defaultRtpAddress = new TransportAddress(streamConnAddr, port, Transport.UDP);

			int rtcpPort = port + 1;
			String rtcpAttributeValue = desc.getAttribute("rtcp");

			if(rtcpAttributeValue != null)
				rtcpPort = Integer.parseInt(rtcpAttributeValue);

			TransportAddress defaultRtcpAddress = new TransportAddress(streamConnAddr, rtcpPort, Transport.UDP);

			Component rtpComponent = stream.getComponent(Component.RTP);
			Component rtcpComponent = stream.getComponent(Component.RTCP);

			Candidate<?> defaultRtpCandidate = rtpComponent.findRemoteCandidate(defaultRtpAddress);
			rtpComponent.setDefaultRemoteCandidate(defaultRtpCandidate);

			if(rtcpComponent != null)
			{
				Candidate<?> defaultRtcpCandidate = rtcpComponent.findRemoteCandidate(defaultRtcpAddress);
				rtcpComponent.setDefaultRemoteCandidate(defaultRtcpCandidate);
			}
		}
	}

	/**
	 * Parses the <tt>attribute</tt>.
	 *
	 * @param attribute
	 *            the attribute that we need to parse.
	 * @param stream
	 *            the {@link IceMediaStream} that the candidate is supposed to
	 *            belong to.
	 *
	 * @return a newly created {@link RemoteCandidate} matching the content of
	 *         the specified <tt>attribute</tt> or <tt>null</tt> if the
	 *         candidate belonged to a component we don't have.
	 */
	private static RemoteCandidate parseCandidate(Attribute attribute, IceMediaStream stream)
	{
		String value = null;

		try
		{
			value = attribute.getValue();
		}
		catch(Throwable t)
		{
		} // can't happen

		StringTokenizer tokenizer = new StringTokenizer(value);

		String foundation = tokenizer.nextToken();
		int componentID = Integer.parseInt(tokenizer.nextToken());
		Transport transport = Transport.parse(tokenizer.nextToken());
		long priority = Long.parseLong(tokenizer.nextToken());
		String address = tokenizer.nextToken();
		int port = Integer.parseInt(tokenizer.nextToken());

		TransportAddress transAddr = new TransportAddress(address, port, transport);

		tokenizer.nextToken(); // skip the "typ" String
		CandidateType type = CandidateType.parse(tokenizer.nextToken());

		Component component = stream.getComponent(componentID);

		if(component == null)
			return null;

		// check if there's a related address property

		RemoteCandidate relatedCandidate = null;
		if(tokenizer.countTokens() >= 4)
		{
			tokenizer.nextToken(); // skip the raddr element
			String relatedAddr = tokenizer.nextToken();
			tokenizer.nextToken(); // skip the rport element
			int relatedPort = Integer.parseInt(tokenizer.nextToken());

			TransportAddress raddr = new TransportAddress(relatedAddr, relatedPort, Transport.UDP);

			relatedCandidate = component.findRemoteCandidate(raddr);
		}

		RemoteCandidate cand = new RemoteCandidate(transAddr, component, type, foundation, priority,
				relatedCandidate);

		component.addRemoteCandidate(cand);

		return cand;
	}

	private void postToServer(String sdpExchangeUrl, ClientType clientType, String toSend) throws IOException
	{
		String url = sdpExchangeUrl + clientType.getUri();
		URL obj = new URL(url);
		logger.info("Posting to [" + url + "]");
		logger.info("Sending this SDP");
		logger.info(toSend);
		// in real life, this should be HTTPS, but let's not worry about SSL certs etc.
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "text/plain");

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(toSend);
		wr.flush();
		wr.close();

		//int responseCode = con.getResponseCode();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		in.close();
		
		System.out.println(response.toString());
	}
	
	private String fetchSdpFromServer(String sdpExchangeUrl, ClientType clientType) throws Exception
	{
		String sdp = do_fetchSdp(sdpExchangeUrl, clientType);
		while(sdp.length() < 5)
		{
			Thread.sleep(1000);
			sdp = do_fetchSdp(sdpExchangeUrl, clientType);
		}
		logger.info("Got this SDP:");
		logger.info(sdp);
		return sdp;
	}
	
	private String do_fetchSdp(String sdpExchangeUrl, ClientType clientType) throws IOException
	{
		URL obj = new URL(sdpExchangeUrl + clientType.getUri());
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");

		//int responseCode = con.getResponseCode();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine).append("\n");
		}
		in.close();

		return response.toString();
	}
}
