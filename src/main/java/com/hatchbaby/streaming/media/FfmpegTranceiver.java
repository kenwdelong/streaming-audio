package com.hatchbaby.streaming.media;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import com.hatchbaby.streaming.model.ClientType;

public class FfmpegTranceiver extends Transceiver
{
	private Process process;

	public FfmpegTranceiver(int localPortBase, String remoteHost, int remotePortBase, ClientType clientType) throws UnknownHostException
	{
		super(localPortBase, remoteHost, remotePortBase, clientType);
	}

	// This worked locally!
	// ffplay -f rtsp -rtsp_flags listen -v debug rtsp://127.0.0.1:50000/stream
	// ffmpeg -re -i /tmp/Earl04.mp3 -f rtsp -allowed_media_types audio -v debug rtsp://127.0.0.1:50000/stream
	
	@Override
	public void start() throws Exception
	{
		switch(clientType)
		{
			case Rx:
				startReceiver();
				break;
			case Tx:
				startTransmitter();
				break;
			default:
				throw new UnsupportedOperationException("Can't support clientType [" + clientType + "]");
		}
	}

	private void startTransmitter()
	{
		// Here's a more robust impl: https://alvinalexander.com/java/java-exec-processbuilder-process-1
		try
		{
			// ffmpeg -re -i /tmp/Earl04.mp3 -f rtsp -allowed_media_types audio -v debug rtsp://127.0.0.1:50000/stream
			String url = "rtsp://" + remoteAddr + ":" + remotePortBase + "/stream";
			ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-re", "/tmp/Eral04.mp3", "-f", "rtsp", "-allowed_media_types", "audio", url);
			File file = new File("/Users/Ken/java/ffmpeg-4.1-win64-static/bin");
			pb.directory(file);
			process = pb.start();

//			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			String line = reader.readLine();
//			while(line != null)
//			{
//				System.out.println(line);
//				line = reader.readLine();
//			}
			logger.info("ffmpeg started");
		}
		catch(IOException e1)
		{
			logger.error("Can't start ffmpeg", e1);
		}
	}

	private void startReceiver()
	{
		// Here's a more robust impl: https://alvinalexander.com/java/java-exec-processbuilder-process-1
		try
		{
			// ffplay -f rtsp -rtsp_flags listen -v debug rtsp://127.0.0.1:50000/stream
			String url = "rtsp://127.0.0.1:" + localPortBase + "/stream"; 
			ProcessBuilder pb = new ProcessBuilder("ffplay", "-f", "rtsp", "-rtsp_flags", "listen", url);
			File file = new File("/tmp/ffmpeg-20190312-d227ed5-win64-static/bin");
			pb.directory(file);
			process = pb.start();

//			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			String line = reader.readLine();
//			while(line != null)
//			{
//				System.out.println(line);
//				line = reader.readLine();
//			}
			logger.info("ffplay started");
		}
		catch(IOException e1)
		{
			logger.error("Can't start ffplay", e1);
		}
	}

	@Override
	public void stop()
	{
		if(process != null)
		{
			process.destroy();
		}
	}

}
