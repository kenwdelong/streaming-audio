package com.hatchbaby.streaming.model;

public enum ClientType
{
	Rx("/rx"), Tx("/tx");
	
	private String uri;
	
	private ClientType(String uri)
	{
		this.uri = uri;
	}
	
	public String getUri()
	{
		return uri;
	}
}
