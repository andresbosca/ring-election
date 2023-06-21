package Services;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class Service {
	public String ip;
	public Date entryDate;
	public boolean leader;

	public static final String BASIC_IP = "10.3.54.";
	public static final int INICIAL_IP_POSITION = 2;
	public static final int FINAL_IP_POSITION = 255;
	public static final int COMMUNICATION_PORT = 6000;
	public static final int LIST_SERVICES_PORT = 6001;
	public static final int UPDATE_SERVICES_PORT = 6002;
	public static final int LEADER = 1;

	public Service(String ip) {
		this.ip = ip;
		this.leader = false;
		entryDate = Date.from(Instant.now());
	}

	public ArrayList<Service> initialConnection() {
		ArrayList<Service> services = getServices();

		updateServices(services);
		
		return services;
	}

	private void updateServices(ArrayList<Service> services) {
		for (int i = INICIAL_IP_POSITION; i < FINAL_IP_POSITION; i++) {
			String ip = BASIC_IP + i;
			try {
				Socket client = new Socket(ip, UPDATE_SERVICES_PORT);

				ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
				ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());

				saida.flush();
				saida.writeObject(services);
				
				entrada.close();
				saida.close();
				client.close();

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public ArrayList<Service> getServices() {
		ArrayList<Service> services = new ArrayList<Service>();
		for (int i = INICIAL_IP_POSITION; i < FINAL_IP_POSITION; i++) {
			String ip = BASIC_IP + i;
			try {
				Socket client = new Socket(ip, LIST_SERVICES_PORT);

				ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
				ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());

				services = (ArrayList<Service>) entrada.readObject();

				entrada.close();
				saida.close();
				client.close();

				break;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (services.size() == 0) {
			this.leader = true;
		}

		services.add(this);

		return services;
	}
}
