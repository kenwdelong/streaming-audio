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
		String sdp = rxSdp;
		rxSdp = null;
		return sdp;
	}
	
	@PostMapping("/tx")
	public String setTxSdp(@RequestBody String sdp)
	{
		System.out.println(sdp);
		txSdp = sdp;
		return "OK";
	}

	@GetMapping("/rx")
	public String getSdpForRx()
	{
		String sdp = txSdp;
		txSdp = null;
		return sdp;
	}
	
	@PostMapping("/rx")
	public String setRxSdp(@RequestBody String sdp)
	{
		rxSdp = sdp;
		return "OK";
	}

}
