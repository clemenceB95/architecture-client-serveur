package heptathlon.server;

import heptathlon.server.database.DatabaseInitializer;
import heptathlon.server.service.HeadOfficeSyncService;
import heptathlon.server.service.StoreServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {

    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
    private static final String RESET_ON_START_ENV = "HEPTATHLON_DB_RESET_ON_START";

    public static void main(String[] args) {
        try {
            boolean resetDatabase = Boolean.parseBoolean(System.getenv(RESET_ON_START_ENV));
            logger.info(() -> "Initialisation de la base de donnees..."
                    + (resetDatabase ? " Mode reinitialisation active." : " Conservation des donnees existantes."));
            DatabaseInitializer.initialize(resetDatabase);

            logger.info("Creation du service RMI...");
            StoreServiceImpl service = new StoreServiceImpl();
            HeadOfficeSyncService headOfficeSyncService = new HeadOfficeSyncService(
                    service.getProductDAO(),
                    service.getInvoiceDAO()
            );
            service.setHeadOfficeSyncService(headOfficeSyncService);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("StoreService", service);
            headOfficeSyncService.start();

            logger.info("Serveur RMI demarre sur le port 1099.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur au demarrage du serveur", e);
        }
    }
}
