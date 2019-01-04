package com.hatchbaby.streaming.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SdpExchangeController
{
	private String txSdp;
	private String rxSdp;
	
	/*
	 * The transmitting client should POST its SDP to /tx, and GET it from /tx
	 * The receiving client should POST its SDP to /rx, and GET it from /rx
	 */
	
	@GetMapping("/tx")
	public String getSdpForTx()
	{
		return rxSdp;
	}
	
	@PostMapping("/tx")
	public String setTxSdp(@RequestBody String sdp)
	{
		txSdp = sdp;
		return "OK";
	}

	@GetMapping("/rx")
	public String getSdpForRx()
	{
		return txSdp;
	}
	
	@PostMapping("/rx")
	public String setRxSdp(@RequestBody String sdp)
	{
		rxSdp = sdp;
		return "OK";
	}

}
