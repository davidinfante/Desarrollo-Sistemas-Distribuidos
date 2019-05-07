package servidor;

import balanceador.carga.BalanceadorCarga;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

/**
 *
 * @author David Infante Casas
 */
public class Servidor {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int numServer_min = 0;
        //Variable para modificar el número máximo de servidores
        int numServer_max = 2;
        
        String numServer = "";
        boolean salir = false;
        Scanner teclado = new Scanner (System.in);
        
        //Comprobarmos que el ID del servidor es un número correcto
        while (!salir) {
            System.out.println("Introduzca el número que identifica a este servidor: (entre " + numServer_min + " y " + numServer_max + ")");
            numServer = teclado.nextLine();
            if (numServer_min <= Integer.parseInt(numServer) && numServer_max >= Integer.parseInt(numServer)) salir = true;
        }
        
        //Creamos el security manager
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
        try {
            Registry reg;
            //Ejecutar los servidores en la misma máquina
            reg = LocateRegistry.createRegistry(1099 - Integer.parseInt(numServer));
            //Ejecutar cada servidor en una máquina distinta
            //reg = LocateRegistry.createRegistry(1099);
            
            //Creamos el balanceador de carga del servidor
            BalanceadorCarga balanceadorCarga = new BalanceadorCarga(Integer.parseInt(numServer), numServer_max);
            Naming.rebind(numServer, balanceadorCarga);
            System.out.println("Servidor " + numServer + " preparado");
        } catch (RemoteException | MalformedURLException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
    
}