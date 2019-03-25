package com.hatchbaby.streaming.media;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.hatchbaby.streaming.model.ClientType;

public class FfmpegTranceiver extends Transceiver
{
	private static final String URI = "/stream";
	private Process process;
	private Properties properties = new Properties();

	public FfmpegTranceiver(int localPortBase, String remoteHost, int remotePortBase, ClientType clientType) throws IOException
	{
		super(localPortBase, remoteHost, remotePortBase, clientType);
		logger.info("Creating FfmpegTransceiver - " + clientType);
		properties.load(ClassLoader.getSystemResourceAsStream("application.properties"));
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
			String url = "rtsp://" + remoteAddr.getHostAddress() + ":" + remotePortBase + URI;
			logger.info("Transmitting on url [" + url + "]");
			String source = properties.getProperty("streaming.source.file");
			
			ProcessBuilder pb = new ProcessBuilder("cmd", "/C", "ffmpeg.exe -re -i " +  source + " -f rtsp -allowed_media_types audio " + url);
			File file = new File(properties.getProperty("streaming.ffmpeg.dir"));
			pb.directory(file);
			logger.info("Executing: " + pb.command());
			process = pb.start();

//			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
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
			String url = "rtsp://127.0.0.1:" + localPortBase + URI;
			logger.info("Receiving on url [" + url + "]");
			
			ProcessBuilder pb = new ProcessBuilder("cmd", "/C", "ffplay.exe -f rtsp -rtsp_flags listen " + url);
			File file = new File(properties.getProperty("streaming.ffmpeg.dir"));
			pb.directory(file);
			logger.info("Executing: " + pb.command());
			process = pb.start();

//			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
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
			logger.info("Destroying process");
			process.destroyForcibly();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		FfmpegTranceiver tx = new FfmpegTranceiver(5100, "localhost", 5200, ClientType.Tx);
		FfmpegTranceiver rx = new FfmpegTranceiver(5200, "localhost", 5100, ClientType.Rx);
		
		tx.start();
		rx.start();
		
		Thread.sleep(10_000);
		System.out.println("Stopping streams");
		
		tx.stop();
		rx.stop();

	}
}
