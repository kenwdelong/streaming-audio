package com.hatchbaby.streaming.media;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import com.hatchbaby.streaming.model.ClientType;

public class FfmpegTranceiver extends Transceiver
{
	private Process process;

	public FfmpegTranceiver(int localPortBase, String remoteHost, int remotePortBase,
			ClientType clientType) throws UnknownHostException
	{
		super(localPortBase, remoteHost, remotePortBase, clientType);
	}

	// This worked locally!
	// ffplay -f rtsp -rtsp_flags listen -v debug rtsp://127.0.0.1:50000/stream
	// ffmpeg -re -i /tmp/Earl04.mp3 -f rtsp -allowed_media_types audio -v debug rtsp://127.0.0.1:50000/stream
	
	@Override
	public void start() throws Exception
	{
		// Here's a more robust impl: https://alvinalexander.com/java/java-exec-processbuilder-process-1
		try
		{
			ProcessBuilder pb = new ProcessBuilder("myCommand", "myArg1", "myArg2");
			File file = new File("myDir");
			pb.directory(file);
			process = pb.start();

//			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			String line = reader.readLine();
//			while(line != null)
//			{
//				System.out.println(line);
//				line = reader.readLine();
//			}
			logger.info("Ffmpeg started");
		}
		catch(IOException e1)
		{
			logger.error("Can't start ffmpeg", e1);
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
