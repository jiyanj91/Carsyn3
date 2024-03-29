package carsynth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class OsmandConnector
{
	private String turn = new String();
	private String street = new String();
	private double lat;
	private double lon;
	private long meter;
	private double speed = 0;
	private Date lastDate;
	
	private boolean bChanged = false;
	Socket echoSocket = null;
	
	public OsmandConnector()
	{
		lastDate = new Date();
	}
	
	public void connect()
	{
		ObjectInputStream in = null;
		try
		{
			echoSocket = new Socket("localhost", 38300);
			System.out.println("Try Conn...");
			try
			{
				while(true){
					String ms = leseNachricht(echoSocket);
					System.out.println("server>" + ms);
					if(ms.equals("Connect"))
					{
						System.out.println("Start");
						break;
					}
				}
			}finally{}
		}
		catch (UnknownHostException e)
		{
			System.err.println("Don't know about host: LocalHost.");
			//System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for " + "the connection to: LocalHost:");
			//System.exit(1);
		}
		finally
		{
			// Closing connection
			try
			{
				if(in != null)in.close();
				/*if (echoSocket != null)
				{
					echoSocket.close();
				}*/
			}
			catch (IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
	}
	public void getConnValues()
	{
		ObjectInputStream in = null;
		String ms = new String();;
		try
		{
			try
			{
				int messageCounter = 0;
				do
				{
					messageCounter++;
					ms = leseNachricht(echoSocket);
				}while((!giveTurn(ms)) && messageCounter < 10);
				{
					// Turn Operation
					turn = translate(ms);
					
					// Street Operation
					ms = leseNachricht(echoSocket);
					if(!ms.equals(street))bChanged = true;
					street = ms;
					
					// Latitude Operation
					ms = leseNachricht(echoSocket);
					ms = ms.substring(0, ms.length()-5);
					if(! (lat == Double.valueOf(ms)))bChanged = true;
					lat = Double.valueOf(ms);
					
					// Longitude Operation
					ms = leseNachricht(echoSocket);
					ms = ms.substring(0, ms.length()-6);
					if(! (lon == Double.valueOf(ms)))bChanged = true;
					lon = Double.valueOf(ms);
					
					// Meter Operation
					ms = leseNachricht(echoSocket);
					long oldMeter = meter;
					meter = Long.parseLong(ms.substring(0, ms.indexOf('m')));//Long.valueOf(ms.substring(0, ms.length()-2));
					Date newDate = new Date();
					if(oldMeter >= meter)
					{
						speed = ((1000*(oldMeter-meter))/(newDate.getTime()-lastDate.getTime()));
						System.out.println(">>>>>"+speed);
						Math.abs(speed);
					}
					//System.out.println(oldMeter-meter + " Zeit " + (newDate.getTime()-lastDate.getTime()) + " sp " + speed);
					lastDate = newDate;
					
					//ms = leseNachricht(echoSocket);
					//System.out.println("server>" + ms);
				}//while();
			}finally{}
		}
		catch (UnknownHostException e)
		{
			System.err.println("Don't know about host: LocalHost.");
			//System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for " + "the connection to: LocalHost:");
			//System.exit(1);
		}
		finally
		{
			// Closing connection
			try
			{
				if(in != null)in.close();
				/*if (echoSocket != null)
				{
					echoSocket.close();
				}*/
			}
			catch (IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
	}
	
	public String translate(String value)
	{
		String ret = new String(value);
		/*
		value.replace("Turn", "Biege");
		value.replace("sharply", "scharf");
		value.replace("slightly", "leicht");
		value.replace("left", "links ab in");
		value.replace("right", "rechts ab in");
		ms.equals("Go ahead")||
				ms.equals("Turn slightly left")||
				ms.equals("Turn left")||
				ms.equals("Turn sharply left")||
				ms.equals("Turn slightly right")||
				ms.equals("Turn right")||
				ms.equals("Turn sharply right")||
				ms.equals("Make uturn")||
				ms.equals("Keep left")||
				ms.equals("Keep right"))
		*/
		switch(value)
		{
		case "Go ahead":
			ret = "Geradeaus in";
			break;
		case "Turn sharply left":
			ret = "Scharf links abbiegen in";
			break;
		case "Turn sharply right":
			ret = "Scharf rechts abbiegen in";
			break;
		case "Turn slightly left":
			ret = "Leicht links abbiegen in";
			break;
		case "Turn slightly right":
			ret = "Leicht rechts abbiegen in";
			break;
		case "Turn left":
			ret = "Links abbiegen in";
			break;
		case "Turn right":
			ret = "Rechts abbiegen in";
			break;
		case "Keep left":
			ret = "Links halten für";
			break;
		case "Keep right":
			ret = "Rechts halten für";
			break;
		}
		return ret;
	}
	
	void schreibeNachricht(java.net.Socket socket, String nachricht) throws IOException {
		PrintWriter printWriter =
				new PrintWriter(
						new OutputStreamWriter(
								socket.getOutputStream()));
		printWriter.print(nachricht);
		printWriter.flush();
	}
	String leseNachricht(java.net.Socket socket) throws IOException {
		BufferedReader bufferedReader =
				new BufferedReader(
						new InputStreamReader(
								socket.getInputStream()));
		char[] buffer = new char[200];
		int anzahlZeichen = bufferedReader.read(buffer, 0, 200); // blockiert bis Nachricht empfangen
		String nachricht = new String(buffer, 0, anzahlZeichen);
		System.out.println("SERVER>" + nachricht);
		return nachricht;
	}
	private boolean giveTurn(String ms)
	{
		if(ms.equals("Go ahead")||
				ms.equals("Turn slightly left")||
				ms.equals("Turn left")||
				ms.equals("Turn sharply left")||
				ms.equals("Turn slightly right")||
				ms.equals("Turn right")||
				ms.equals("Turn sharply right")||
				ms.equals("Make uturn")||
				ms.equals("Keep left")||
				ms.equals("Keep right")) // TODO: all turns
		{
			return true;
		}
		return false;
	}
	
	public long getMeter()
	{
		return meter;
	}
	
	public String getTurn()
	{
		return turn;
	}
	
	public boolean isChanged()
	{
		return bChanged;
	}
	
	public void setChanged(boolean val)
	{
		bChanged = val;
	}
	
	public double getSpeed()
	{
		return speed;
	}
}
