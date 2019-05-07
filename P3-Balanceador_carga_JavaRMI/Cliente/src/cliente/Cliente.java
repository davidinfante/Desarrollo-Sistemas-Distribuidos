package cliente;

import iservidor.cliente.IServidorCliente;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author David Infante Casas
 */
public class Cliente {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int numServer_min = 0;
        //Variable para modificar el número máximo de servidores
        int numServer_max = 2;
        boolean conectado = false;
        int numServer = -1;
        IServidorCliente servidorCliente = null;
        
        String seleccion = "";
        String username;
        double donacion = -1;
        boolean donacion_correcta;
        Scanner input = new Scanner(System.in);
        
        //Creamos el security manager
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            
            //Conectamos al cliente con un servidor aleatorio
            Random random = new Random();
            while (!conectado) {
                numServer = random.nextInt(numServer_max - numServer_min + 1) + numServer_min;
                try {
                    servidorCliente = (IServidorCliente) registry.lookup(Integer.toString(numServer));
                    conectado = true;
                } catch(NotBoundException | RemoteException e) {
                    System.out.println("El servidor " + numServer + " no está disponible en este momento");
                }
            }
            System.out.println("Conectado al servidor número: " + numServer);
            
            //Login
            System.out.println("Introduzca su username: ");
            username = input.nextLine();
            
            //Bucle con las operaciones que puede realizar el cliente
            while (!seleccion.equals("7")) {
                if (!seleccion.equals("2") && !seleccion.equals("3")) System.out.println("\n1)Registrarse\n2)Donar\n3)Donar Causa 2\n4)Total Donado\n5)Total Donado Causa 2\n6)Total Donado Ambas Causas\n7)Salir");
                seleccion = input.nextLine();

                switch (seleccion) {
                    //Registro
                    case "1":
                        System.out.println(servidorCliente.registrar(username));
                    break;
                    //Donar Causa 1
                    case "2":
                        donacion_correcta = false;
                        do {
                            try {
                                System.out.println("Introduzca cuanto va a donar a la causa 1: ");
                                donacion = input.nextDouble();
                                donacion_correcta = true;
                            } catch(InputMismatchException e) {
                                input.next();
                            }
                        } while (!donacion_correcta);
                        System.out.println(servidorCliente.donarCausa1(username, donacion));
                    break;
                    //Donar Causa 2
                    case "3":
                        donacion_correcta = false;
                        do {
                            try {
                                System.out.println("Introduzca cuanto va a donar a la causa 2: ");
                                donacion = input.nextDouble();
                                donacion_correcta = true;
                            } catch(InputMismatchException e) {
                                input.next();
                            }
                        } while (!donacion_correcta);
                        System.out.println(servidorCliente.donarCausa2(username, donacion));
                    break;
                    //Consultar total Causa 1
                    case "4":
                        System.out.println(servidorCliente.totalDonadoCausa1(username));
                    break;
                    //Consultar total Causa 2
                    case "5":
                        System.out.println(servidorCliente.totalDonadoCausa2(username));
                    break;
                    //Consultar total Ambas Causas
                    case "6":
                        System.out.println(servidorCliente.totalDonadoAmbasCausas(username));
                    break;
                }
            }
        } catch(RemoteException e) {
            System.err.println("Excepción del sistema: " + e);
        }
    }
    
}