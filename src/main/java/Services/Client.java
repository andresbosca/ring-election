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

	public Client() throws UnknownHostException {
		this.services = new ArrayList<Service>();
		this.service = new Service(InetAddress.getLocalHost().getHostAddress());
		this.connectServices();
	}

	public void connectServices() {
		this.services = this.service.initialConnection();
	}

	public void communicate() {
        try {

            ServerSocket servidor = new ServerSocket(Service.COMMUNICATION_PORT);
            System.out.println("communicate - Servidor ouvindo a porta: " + Service.COMMUNICATION_PORT);

            while (true) {

                Socket cliente = servidor.accept();

                System.out.println("communicate - Cliente : " + cliente.getInetAddress().getHostAddress());
                ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
                ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

                System.out.println("communicate - Recebendo a mensagem");
                String resposta = (String) entrada.readObject();

                services.sort();
                if ()
                
                System.out.println("communicate - Limpando o buffer e enviando mensagem");
                saida.flush();
                saida.writeObject(mensagem);

                System.out.println("communicate - O cliente respondeu:\n" + resposta);
                
                System.out.println("communicate - Conexão encerrada");

                saida.close();
                entrada.close();
                cliente.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void sendServices() {
		try {
			ServerSocket servidor = new ServerSocket(Service.LIST_SERVICES_PORT);
			System.out.println("sendServices - Servidor ouvindo a porta: " + Service.LIST_SERVICES_PORT);

			while (true) {

				Socket cliente = servidor.accept();

				System.out.println("sendServices - Cliente : " + cliente.getInetAddress().getHostAddress());
				ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
				ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

				System.out.println("sendServices - Limpando o buffer e enviando mensagem");
				saida.flush();
				saida.writeObject(this.services);

				System.out.println("sendServices - Recebendo a mensagem");
				String resposta = (String) entrada.readObject();

				System.out.println("sendServices - O cliente respondeu:\n" + resposta);

				System.out.println("sendServices - Conexão encerrada");

				saida.close();
				entrada.close();
				cliente.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateServices() {
		try {
			ServerSocket servidor = new ServerSocket(Service.UPDATE_SERVICES_PORT);
			System.out.println("updateServices - Servidor ouvindo a porta: " + Service.UPDATE_SERVICES_PORT);

			while (true) {

				Socket cliente = servidor.accept();

				System.out.println("updateServices - Cliente : " + cliente.getInetAddress().getHostAddress());
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

				this.services = resposta;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
