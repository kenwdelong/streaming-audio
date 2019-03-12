package com.hatchbaby.streaming.client;

import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ice4j.ice.CandidatePair;
import org.ice4j.ice.LocalCandidate;
import org.ice4j.ice.RemoteCandidate;
import org.ice4j.security.LongTermCredential;
import org.jitsi.service.libjitsi.LibJitsi;

import com.hatchbaby.streaming.ice.IceClient;
import com.hatchbaby.streaming.media.Transceiver;
import com.hatchbaby.streaming.model.ClientType;

import rx.Observable;

public class Driver
{
	private String sdpExchangeHost;
	private ClientType clientType;
	private int port;
	private LongTermCredential turnCredentials;
	
	private Transceiver transceiver;

	public static void main(String[] args)
	{
		try
		{
			CommandLine cmd = parseCommandLine(args);
			String host = "http://" + cmd.getOptionValue("h");
			ClientType clientType = ClientType.valueOf(cmd.getOptionValue("t"));
			int port = Integer.parseInt(cmd.getOptionValue("p"));
			
			String turnUser = cmd.getOptionValue("u");
			String turnCredential = cmd.getOptionValue("c");
			LongTermCredential turnCredentials = new LongTermCredential(turnUser, turnCredential);

			Driver driver = new Driver(host, clientType, port, turnCredentials);
			driver.start();
			Thread.sleep(60_000);
			driver.stop();
			System.exit(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public Driver(String sdpExchangeHost, ClientType clientType, int port, LongTermCredential turnCreds)
	{
		this.sdpExchangeHost = sdpExchangeHost;
		this.clientType = clientType;
		this.port = port;
		this.turnCredentials = turnCreds;
	}
	
	public void start() throws Exception
	{
		LibJitsi.start();
		IceClient iceClient = new IceClient();
		Observable<CandidatePair> iceResult = iceClient.startIceDancing(port, sdpExchangeHost, clientType, turnCredentials);
		iceResult.map(pair -> createTransceiver(pair, clientType))
			.flatMap(t -> startTransceiver(t))
			.subscribe(t -> this.transceiver = t,
					   e -> 
					   {
						   e.printStackTrace();
						   stop();
					   }
			);
	}
	
	private Observable<Transceiver> startTransceiver(Transceiver transceiver)
	{
		try
		{
			transceiver.start();
			return Observable.just(transceiver);
		}
		catch(Exception e)
		{
			return Observable.error(e);
		}
	}
	
	public void stop()
	{
		if(transceiver != null)
		{
			transceiver.stop();
		}
		LibJitsi.stop();
	}
	
	private Transceiver createTransceiver(CandidatePair pair, ClientType clientType)
	{
		Transceiver transceiver = null;
		try
		{
			LocalCandidate local = pair.getLocalCandidate();
			RemoteCandidate remote = pair.getRemoteCandidate();
			transceiver = Transceiver.create(local.getTransportAddress().getPort(),
													 remote.getTransportAddress().getAddress().getHostName(),
													 remote.getTransportAddress().getPort(),
													 clientType);
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
		return transceiver;
	}

	private static CommandLine parseCommandLine(String[] args) throws ParseException
	{
		Options options = new Options(); 
		options.addRequiredOption("h", "host", true, "name of SDP exchange host");
		options.addRequiredOption("t", "type", true, "client type (Rx or Tx)");
		options.addRequiredOption("p", "port", true, "Base port number");
		options.addOption("u", "turnuser", true, "Turn user name");
		options.addOption("c", "turncredential", true, "Turn password");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args);
		return cmd;
	}
}
