package balanceador.carga;

import iservidor.servidor.IServidorServidor;
import iservidor.cliente.IServidorCliente;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author David Infante Casas
 */
public class BalanceadorCarga extends UnicastRemoteObject implements IServidorServidor, IServidorCliente {

    private HashMap <String, Boolean> listaRegistros;
    private double totalDonadoCausa1;
    private double totalDonadoCausa2;
    private ArrayList<Integer> listaServers;
    private int idServidor;
    
    public BalanceadorCarga(int id, int numServer_max) throws RemoteException {
        super();
        this.listaRegistros = new HashMap<>();
        this.totalDonadoCausa1 = 0;
        this.totalDonadoCausa2 = 0;
        this.listaServers = new ArrayList<>();
        for (int i = 0; i < numServer_max+1; ++i) {
            if (i == id) idServidor = id;
            listaServers.add(i);
        }
    }
    
    //Crea una instancia de ServidorCliente para el servidor i
    public IServidorCliente newServidorCliente(int i) throws RemoteException {
        IServidorCliente instancia = null;
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            instancia = (IServidorCliente) registry.lookup(String.valueOf(listaServers.get(i)));
        } catch(NotBoundException | RemoteException | NullPointerException e) {
            System.out.println("El servidor " + i + " no está disponible en este momento");
        }
        return instancia;
    }
    
    //Crea una instancia de ServidorServidor para el servidor i
    public IServidorServidor newServidorServidor(int i) throws RemoteException {
        IServidorServidor instancia = null;
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            instancia = (IServidorServidor) registry.lookup(String.valueOf(listaServers.get(i)));
        } catch(NotBoundException | RemoteException | NullPointerException e) {
            System.out.println("El servidor " + i + " no está disponible en este momento");
        }
        return instancia;
    }
    
    
    //Métodos IServidorServidor
    
    @Override
    public int getTotalRegistrados() throws RemoteException {
        return listaRegistros.size();
    }
    
    @Override
    public double getTotalDonadoCausa1() throws RemoteException {
        return totalDonadoCausa1;
    }
    
    @Override
    public double getTotalDonadoCausa2() throws RemoteException {
        return totalDonadoCausa2;
    }
    
    @Override
    public double getTotalDonadoAmbasCausas() throws RemoteException {
        return totalDonadoCausa1 + totalDonadoCausa2;
    }
    
    @Override
    public boolean isRegistrado(String username) throws RemoteException {
        return listaRegistros.containsKey(username);
    }
    
    @Override
    public boolean haDonado(String username) throws RemoteException {
        if (listaRegistros.containsKey(username)) return listaRegistros.get(username);
        return false;
    }
    
    
    //Métodos IServidorCliente
    
    @Override
    public String registrar(String username) throws RemoteException {
        //Cantidad mínima de usuarios registrados en un servidor
        int menosReg = 9999;
        //Server en el que va a ser registrado en el caso de que no lo esté
        int serverARegistrar = -1;
        //Server en el que está registrado en el caso de que lo esté
        int serverRegistrado = -1;
        boolean isRegistrado = false;
        
        for (int i = 0; i < listaServers.size(); ++i) {
            //Caso de este servidor
            if (i == idServidor) {
                if (menosReg > listaRegistros.size()) {
                    menosReg = listaRegistros.size();
                    serverARegistrar = i;
                }
                if (listaRegistros.containsKey(username)) {
                    serverRegistrado = i;
                    isRegistrado = true;
                }
            //Caso del resto de servidores
            } else {
                try {
                    IServidorServidor servidorServidor = newServidorServidor(i);
                    if (menosReg > servidorServidor.getTotalRegistrados()) {
                        menosReg = servidorServidor.getTotalRegistrados();
                        serverARegistrar = i;
                    }
                    if (servidorServidor.isRegistrado(username)) {
                        serverRegistrado = i;
                        isRegistrado = true;
                    }
                } catch(RemoteException | NullPointerException e) {
                    System.out.println("El servidor " + i + " no está disponible en este momento");
                }
            }
        }
        //En el caso de que haya otro servidor con el minimo número de registrados, lo registramos en este servidor
        if (menosReg == listaRegistros.size()) serverARegistrar = idServidor;
        
        //Si no está registrado en ningún servidor
        if (!isRegistrado) {
            //En el caso de que haya que registrarlo en este servidor
            if (serverARegistrar == idServidor) {
                listaRegistros.put(username, Boolean.FALSE);
                System.out.println("Registrado " + username + " en este servidor\n");
            //En el caso de que haya que registrarlo en otro servidor
            } else {
                try {
                    IServidorCliente servidorCliente = newServidorCliente(serverARegistrar);
                    System.out.println("Registrado " + username + " en el servidor: " + serverARegistrar + "\n");
                    return servidorCliente.registrar(username);
                } catch(RemoteException e) {
                    System.err.println("Excepción del sistema: " + e);
                }
            }
            return "Registro realizado correctamente";
        }
        return "El usuario ya estaba registrado en el servidor: " + serverRegistrado;
    }
    
    @Override
    public String donarCausa1(String username, double cantidad) throws RemoteException {
        //Si está registrado en este servidor
        if (listaRegistros.containsKey(username)) {
            listaRegistros.put(username, Boolean.TRUE);
            totalDonadoCausa1 += cantidad;
            System.out.println("Donación de " + cantidad + " euros por " + username + " realizada en este servidor\n");
            return "Donación realizada correctamente";
        //Comprobamos si está registrado en otro servidor
        } else {
            for (int i = 0; i < listaServers.size(); ++i) {
                if (i != idServidor) {
                    try {
                        IServidorServidor servidorServidor = newServidorServidor(i);
                        if (servidorServidor.isRegistrado(username)) {
                            IServidorCliente servidorCliente = newServidorCliente(i);
                            System.out.println("Donación " + cantidad + " euros por " + username + " realizada en el servidor: " + i + "\n");
                            return servidorCliente.donarCausa1(username, cantidad);
                        }
                    } catch (RemoteException | NullPointerException e) {
                        System.out.println("El servidor " + i + " no está disponible en este momento");
                    }
                }
            }
        }
        return "No puede donar porque no está registrado";
    }
    
    @Override
    public String donarCausa2(String username, double cantidad) throws RemoteException {
        if (listaRegistros.containsKey(username) && this.idServidor == 2) return "El servidor 2 no permite donaciones a la causa 2";

        //Si está registrado en este servidor
        if (listaRegistros.containsKey(username)) {
            listaRegistros.put(username, Boolean.TRUE);
            totalDonadoCausa2 += cantidad;
            System.out.println("Donación (causa 2) de " + cantidad + " euros por " + username + " realizada en este servidor\n");
            return "Donación para la causa 2 realizada correctamente";
        //Comprobamos si está registrado en otro servidor
        } else {
            for (int i = 0; i < listaServers.size(); ++i) {
                if (i != idServidor) {
                    try {
                        IServidorServidor servidorServidor = newServidorServidor(i);
                        if (servidorServidor.isRegistrado(username)) {
                            IServidorCliente servidorCliente = newServidorCliente(i);
                            System.out.println("Donación (causa 2) de " + cantidad + " euros por " + username + " realizada en el servidor: " + i + "\n");
                            return servidorCliente.donarCausa2(username, cantidad);
                        }
                    } catch (RemoteException | NullPointerException e) {
                        System.out.println("El servidor " + i + " no está disponible en este momento");
                    }
                }
            }
        }
        return "No puede donar porque no está registrado";
    }
    
    @Override
    public String totalDonadoCausa1(String username) throws RemoteException {
        double total = 0;
        //Si el usuario ha donado en este servidor calculamos el total
        if (haDonado(username)) {
            for (int i = 0; i < listaServers.size(); ++i) {
                if (i == idServidor) total += totalDonadoCausa1;
                else {
                    try {
                        IServidorServidor servidorServidor = newServidorServidor(i);
                        total += servidorServidor.getTotalDonadoCausa1();
                    } catch (RemoteException | NullPointerException e) {
                        System.out.println("El servidor " + i + " no está disponible en este momento");
                    }
                }
            }
            System.out.println("Total (causa 1) consultado en este servidor\n");
            return "El total (causa 1) donado es: " + total;
        //Comprobamos si ha donado en otro servidor
        } else {
            for (int i = 0; i < listaServers.size(); ++i) {
                if (i != idServidor) {
                    try {
                        IServidorServidor servidorServidor = newServidorServidor(i);
                        if (servidorServidor.haDonado(username)) {
                            IServidorCliente servidorCliente = newServidorCliente(i);
                            System.out.println("Total (causa 1) consultado en el servidor" + i + "\n");
                            return servidorCliente.totalDonadoCausa1(username);
                        }
                    } catch (RemoteException | NullPointerException e) {
                        System.out.println("El servidor " + i + " no está disponible en este momento");
                    }
                }
            }
        }
        return "No se encuentra registrado o no ha realizado ninguna donación";
    }
    
    @Override
    public String totalDonadoCausa2(String username) throws RemoteException {
        double total = 0;
        //Si el usuario ha donado en este servidor calculamos el total
        if (haDonado(username)) {
            for (int i = 0; i < listaServers.size(); ++i) {
                if (i == idServidor) total += totalDonadoCausa2;
                else {
                    try {
                        IServidorServidor servidorServidor = newServidorServidor(i);
                        total += servidorServidor.getTotalDonadoCausa2();
                    } catch (RemoteException | NullPointerException e) {
                        System.out.println("El servidor " + i + " no está disponible en este momento");
                    }
                }
            }
            System.out.println("Total (causa 2) consultado en este servidor\n");
            return "El total (causa 2) donado es: " + total;
        //Comprobamos si ha donado en otro servidor
        } else {
            for (int i = 0; i < listaServers.size(); ++i) {
                if (i != idServidor) {
                    try {
                        IServidorServidor servidorServidor = newServidorServidor(i);
                        if (servidorServidor.haDonado(username)) {
                            IServidorCliente servidorCliente = newServidorCliente(i);
                            System.out.println("Total (causa 2) consultado en el servidor" + i + "\n");
                            return servidorCliente.totalDonadoCausa2(username);
                        }
                    } catch (RemoteException | NullPointerException e) {
                        System.out.println("El servidor " + i + " no está disponible en este momento");
                    }
                }
            }
        }
        return "No se encuentra registrado o no ha realizado ninguna donación";
    }
    
    @Override
    public String totalDonadoAmbasCausas(String username) throws RemoteException {
        double total = 0;
        //Si el usuario ha donado en este servidor calculamos el total
        if (haDonado(username)) {
            for (int i = 0; i < listaServers.size(); ++i) {
                if (i == idServidor) total += this.getTotalDonadoAmbasCausas();
                else {
                    try {
                        IServidorServidor servidorServidor = newServidorServidor(i);
                        total += servidorServidor.getTotalDonadoAmbasCausas();
                    } catch (RemoteException | NullPointerException e) {
                        System.out.println("El servidor " + i + " no está disponible en este momento");
                    }
                }
            }
            System.out.println("Total (ambas causas) consultado en este servidor\n");
            return "El total (ambas causas) donado es: " + total;
        //Comprobamos si ha donado en otro servidor
        } else {
            for (int i = 0; i < listaServers.size(); ++i) {
                if (i != idServidor) {
                    try {
                        IServidorServidor servidorServidor = newServidorServidor(i);
                        if (servidorServidor.haDonado(username)) {
                            IServidorCliente servidorCliente = newServidorCliente(i);
                            System.out.println("Total (ambas causas) consultado en el servidor" + i + "\n");
                            return servidorCliente.totalDonadoAmbasCausas(username);
                        }
                    } catch (RemoteException | NullPointerException e) {
                        System.out.println("El servidor " + i + " no está disponible en este momento");
                    }
                }
            }
        }
        return "No se encuentra registrado o no ha realizado ninguna donación";
    }
}