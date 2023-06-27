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
import java.io.Serializable;

public class Service implements Serializable {
	public String ip;
	public Date entryDate;
	public boolean leader;

	public static final String BASIC_IP = "192.168.1.";
	public static final int INICIAL_IP_POSITION = 100;
	public static final int FINAL_IP_POSITION = 110;
	public static final int COMMUNICATION_PORT = 6000;
	public static final int LIST_SERVICES_PORT = 6001;
	public static final int UPDATE_SERVICES_PORT = 6002;
	public static final int TIMEOUT = 100;
	public static final int LEADER = 1;

	public Service(String ip) {
		this.ip = ip;
		this.leader = false;
		entryDate = Date.from(Instant.now());
	}

	public ArrayList<Service> initialConnection() {
		ArrayList<Service> services = getServices();

		if (!this.leader) {
			services = updateServices(services);
		}

		return services;
	}

	public ArrayList<Service> updateServices(ArrayList<Service> services) {
		for (int i = 0; i < services.size(); i++) {
			Service service = services.get(i);
			if (service.ip == this.ip) {
				continue;
			}
			try {
				Socket client = new Socket(service.ip, UPDATE_SERVICES_PORT);

				ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
				ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());

				saida.flush();
				saida.writeObject(services);

				entrada.readObject();

				entrada.close();
				saida.close();
				client.close();

			} catch (Exception e) {
				e.printStackTrace();
				services.remove(service);
				i = 0;
			}
		}

		return services;

	}

	public ArrayList<Service> getServices() {
		ArrayList<Service> services = new ArrayList<Service>();
		for (int i = INICIAL_IP_POSITION; i < FINAL_IP_POSITION; i++) {
			String ip = BASIC_IP + i;
			try {
				Socket client = new Socket();
				client.connect(new java.net.InetSocketAddress(ip, LIST_SERVICES_PORT), TIMEOUT);

				ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
				ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());

				services = (ArrayList<Service>) entrada.readObject();

				saida.flush();
				saida.writeObject("recebido");
				entrada.close();
				saida.close();
				client.close();

				break;
			} catch (Exception e) {
				System.out.println("Não foi possível conectar ao ip: " + ip);
				// e.printStackTrace();
			}
		}

		if (services.size() == 0) {
			this.leader = true;
		}
		int found = 0;

		for (int i = 0; i < services.size(); i++) {
			if (services.get(i).ip == this.ip) {
				found = i;
				break;
			}
		}
		if (found != 0)
			services.remove(found);

		services.add(this);

		return services;
	}

	public boolean sendMessageToLeader(String resposta, String ip) {
		try {
			Socket client = new Socket(ip, COMMUNICATION_PORT);

			ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
			ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());

			saida.flush();
			saida.writeObject(resposta);

			entrada.readObject();

			entrada.close();
			saida.close();
			client.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
