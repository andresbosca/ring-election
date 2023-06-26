package Services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;

public class Client {
	public ArrayList<Service> services;
	public Service service;
	public boolean servicesChanged;

	public Client() throws UnknownHostException {
		this.services = new ArrayList<Service>();
		this.service = new Service(InetAddress.getLocalHost().getHostAddress());
		this.connectServices();
		this.keepServicesUpdated();
		this.updateServices();
		this.sendServices();
	}

	public void connectServices() {
		this.services = this.service.initialConnection();

	}

	public void communicate() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					ServerSocket servidor = new ServerSocket(
							Service.COMMUNICATION_PORT);
					System.out.println("communicate - Servidor ouvindo a porta: " + Service.COMMUNICATION_PORT);

					while (true) {
						try {

							Socket cliente = servidor.accept();

							System.out.println("communicate - Cliente : " + cliente.getInetAddress().getHostAddress());
							ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
							ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

							System.out.println("communicate - Recebendo a mensagem");
							int resposta = (int) entrada.readObject();

							services.sort((s1, s2) -> s1.entryDate.compareTo(s2.entryDate));

							if (services.get(0).ip != service.ip) {
								service.leader = false;

								boolean leaderFound = false;

								while (!leaderFound && services.size() > 1) {
									if (services.get(0).ip == service.ip) {
										service.leader = true;
										leaderFound = true;
										break;
									}
									// leaderFound = service.sendMessageToLeader(resposta, services.get(0).ip);
									if (!leaderFound) {
										servicesChanged = true;
										services.remove(0);
									}
								}
							}

							if (services.get(0).ip == service.ip) {
								service.leader = true;
								System.out.println(resposta);
							}

							System.out.println("communicate - Conexão encerrada");

							saida.close();
							entrada.close();
							cliente.close();
						} catch (Exception ex) {
							ex.printStackTrace();
							System.out.println("communicate - Conexão encerrada");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public void sendServices() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ServerSocket servidor = new ServerSocket(Service.LIST_SERVICES_PORT);
					System.out.println("sendServices - Servidor ouvindo a porta: " + Service.LIST_SERVICES_PORT);

					while (true) {

						try {

							Socket cliente = servidor.accept();

							System.out.println("sendServices - Cliente : " + cliente.getInetAddress().getHostAddress());
							ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());

							System.out.println("sendServices - Limpando o buffer e enviando mensagem");
							saida.flush();
							saida.writeObject(services);

							System.out.println("sendServices - Conexão encerrada");

							saida.close();
							cliente.close();
						} catch (Exception ex) {
							ex.printStackTrace();
							System.out.println("sendServices - Conexão encerrada");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void updateServices() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ServerSocket servidor = new ServerSocket(Service.UPDATE_SERVICES_PORT);
					System.out.println("updateServices - Servidor ouvindo a porta: " + Service.UPDATE_SERVICES_PORT);

					while (true) {

						try {

							Socket cliente = servidor.accept();

							System.out
									.println("updateServices - Cliente : " + cliente.getInetAddress().getHostAddress());
							ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
							ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

							System.out.println("updateServices - Limpando o buffer e enviando mensagem");

							System.out.println("updateServices - Recebendo a mensagem");
							ArrayList<Service> resposta = (ArrayList<Service>) entrada.readObject();

							System.out.println("updateServices - O cliente respondeu:\n" + resposta);

							System.out.println("updateServices - Conexão encerrada");
							saida.flush();
							saida.writeObject("recieved");

							saida.close();
							entrada.close();
							cliente.close();

							services = resposta;
						} catch (Exception ex) {
							ex.printStackTrace();
							System.out.println("updateServices - Conexão encerrada");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void keepServicesUpdated() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						if (servicesChanged) {
							servicesChanged = false;
							service.updateServices(services);
						}
						Thread.sleep(2000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
