package com.hatchbaby.streaming.media;

import java.net.UnknownHostException;

import com.hatchbaby.streaming.model.ClientType;

public class FfmpegTranceiver extends Transceiver
{

	public FfmpegTranceiver(int localPortBase, String remoteHost, int remotePortBase, ClientType clientType) throws UnknownHostException
	{
		super(localPortBase, remoteHost, remotePortBase, clientType);
	}
	
	@Override
	public void start() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void stop()
	{
		// TODO Auto-generated method stub

	}

}
