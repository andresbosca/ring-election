package Services;

public class Main {

    public static void main(String[] args) {
        Client client;
        try {
            client = new Client();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        client.communicate();
    }
}
