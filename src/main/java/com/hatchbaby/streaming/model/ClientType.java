package com.hatchbaby.streaming.model;

import org.jitsi.service.neomedia.MediaDirection;

public enum ClientType
{
	Rx("/rx", MediaDirection.RECVONLY), Tx("/tx", MediaDirection.SENDONLY);
	
	private String uri;
	private MediaDirection direction;
	
	private ClientType(String uri, MediaDirection direction)
	{
		this.uri = uri;
		this.direction = direction;
	}
	
	public String getUri()
	{
		return uri;
	}
	
	public MediaDirection getDirection()
	{
		return direction;
	}
}
