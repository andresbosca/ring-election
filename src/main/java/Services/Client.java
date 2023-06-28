package Services;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    public ArrayList<Service> services;
    public Service service;
    public boolean servicesChanged;

    public Client() throws UnknownHostException, SocketException {
        this.services = new ArrayList<Service>();
        this.service = new Service("10.3.54.26");
        this.connectServices();
        this.keepServicesUpdated();
        this.updateServices();
        this.sendServices();
        this.healthy();
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
                            String resposta = (String) entrada.readObject();

                            services.sort((s1, s2) -> s1.entryDate.compareTo(s2.entryDate) > 0 ? 1 : -1);

                            if (!services.get(0).ip.equals(service.ip)) {
                                service.leader = false;

                                boolean leaderFound = false;

                                while (!leaderFound && services.size() > 1) {
                                    System.out.println("TENTANDO IP:" + services.get(0).ip);
                                    if (services.get(0).ip == service.ip) {
                                        service.leader = true;
                                        leaderFound = true;
                                        break;
                                    }
                                    leaderFound = service.sendMessageToLeader(resposta, services.get(0).ip);
                                    if (!leaderFound) {
                                        System.out.println("REMOVENDO LIDER - sendMessageToLeader");
                                        servicesChanged = true;
                                        services.remove(0);
                                        service.updateServices(services);
                                    }
                                }
                            }

                            if (services.get(0).ip.equals(service.ip)) {
                                service.leader = true;
                                System.out.println(resposta);
                            }

                            saida.flush();
                            saida.writeObject("OK");
                            saida.close();
                            entrada.close();
                            cliente.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.out.println("communicate - ConexÃ£o encerrada");
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
                            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

                            saida.flush();
                            saida.writeObject(services);

                            System.out.println(entrada.readObject());

                            entrada.close();
                            saida.close();
                            cliente.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.out.println("sendServices - ConexÃ£o encerrada");
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

                            ArrayList<Service> resposta = (ArrayList<Service>) entrada.readObject();

                            System.out.println("updateServices - O cliente respondeu:\n" + resposta);

                            saida.flush();
                            saida.writeObject("recieved");

                            saida.close();
                            entrada.close();
                            cliente.close();

                            if (resposta.get(0).entryDate.before(services.get(0).entryDate)) {
                                services.add(new Service(resposta.get(0).ip));
                                service.updateServices(services);
                                System.out.println("updateServices - Removido antigo lider");
                            } else {
                                System.out.println("updateServices - Atualizado services");
                                services = resposta;
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.out.println("updateServices - ConexÃ£o encerrada");
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
                    int contador = 0;
                    while (true) {
                        if (service.ip.equals(services.get(0).ip)) {
                            System.out.println("SOU O LIDER");
                        }
                        Thread.sleep(2000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

   public void healthy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean wasOffline = false;
                while (true) {
                    int x = 1;
                    try {
                        Thread.sleep(5000);
                        Process process = java.lang.Runtime.getRuntime().exec("ping www.google.com");
                        x = process.waitFor();
                        if (x != 0) {
                            wasOffline = true;
                        }

                        if (wasOffline && x == 0) {
                            System.out.println("OFFLINE-OFFLINE-OFFLINE-OFFLINE-OFFLINE");
                            service.getServices();
                            wasOffline = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        wasOffline = true;
                    }
                }
            }
        }
        ).start();
    }
}
