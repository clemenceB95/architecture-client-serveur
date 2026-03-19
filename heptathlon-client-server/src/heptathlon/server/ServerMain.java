package heptathlon.server;

import heptathlon.server.database.DatabaseInitializer;
import heptathlon.server.service.StoreServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {

    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());

    public static void main(String[] args) {
        try {
            logger.info("Initialisation de la base de données...");
            DatabaseInitializer.initialize();

            logger.info("Création du service RMI...");
            StoreServiceImpl service = new StoreServiceImpl();

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("StoreService", service);

            logger.info("Serveur RMI démarré sur le port 1099.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur au démarrage du serveur", e);
        }
    }
}