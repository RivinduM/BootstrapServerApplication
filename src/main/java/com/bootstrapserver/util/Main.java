package com.bootstrapserver.util;

import com.bootstrapserver.repository.PeerRepository;
import com.bootstrapserver.repository.UserRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import messenger.ReceiverHandler;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.net.*;
import java.util.Enumeration;

public class Main extends Application {
    private static int userID;

    private static PropertiesConfiguration prop;
    private static UIUpdater registrationListener;
    private static Integer loggedInUsers = 0;

    public static void main(String[] args) {

        UserRepository userRepo = new UserRepository();
        userRepo.setupUserTable();
        PeerRepository peerRepo = new PeerRepository();
        peerRepo.setupPeerTable();

        try {
            prop = new PropertiesConfiguration("properties/user.properties");
            userID = Integer.parseInt(String.valueOf(prop.getProperty("user_id")));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        int port = 25025;
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }
        ReceiverHandler receiverHandler = new ReceiverHandler(port);
        Thread t = new Thread(receiverHandler);
        t.start();

        getLocalIPAddress();

        launch(args);
    }


    public static Integer getLoggedInUsers() {
        return loggedInUsers;
    }

    public static void increaseLoggedInUsers() {
        synchronized (Main.loggedInUsers) {
            Main.loggedInUsers += 1;
        }
    }

    public static void decreaseLoggedInUsers() {
        synchronized (Main.loggedInUsers) {
            if (Main.loggedInUsers > 0) {
                Main.loggedInUsers -= 1;
            }
        }
    }


    public static UIUpdater getRegistrationListener() {
        return registrationListener;
    }

    public static void setRegistrationListener(UIUpdater registrationListener) {
        Main.registrationListener = registrationListener;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
        Scene scene = new Scene(parent, 1024, 768);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    public static int giveUserID() {
        Main.userID += 1;
        synchronized (prop) {
            prop.setProperty("user_id", String.valueOf(userID));
            try {
                prop.save();
                System.out.println("saved");
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }
        return userID;
    }

    public static String getLocalIPAddress() {
        InetAddress inetAddress = null;
        try {
            for (
                    final Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();
                    interfaces.hasMoreElements();
                    ) {
                final NetworkInterface cur = interfaces.nextElement();

                if (cur.isLoopback()) {
                    continue;
                }

                for (final InterfaceAddress addr : cur.getInterfaceAddresses()) {
                    final InetAddress inet_addr = addr.getAddress();

                    if (!(inet_addr instanceof Inet4Address)) {
                        continue;
                    }
                    inetAddress = inet_addr;
                    System.out.println(
                            "  address: " + inet_addr.getHostAddress() +
                                    "/" + addr.getNetworkPrefixLength()
                    );
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (inetAddress != null) {
            return inetAddress.getHostAddress();
        }
        return "No network Connection!";
    }

}
