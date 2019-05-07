package iservidor.cliente;

import java.rmi.Remote; 
import java.rmi.RemoteException;

/**
 *
 * @author David Infante Casas
 */
public interface IServidorCliente extends Remote {
    //Operaciones que realiza el cliente sobre el servidor
    public String registrar(String username) throws RemoteException;
    public String donarCausa1(String username, double cantidad) throws RemoteException;
    public String donarCausa2(String username, double cantidad) throws RemoteException;
    public String totalDonadoCausa1(String username) throws RemoteException;
    public String totalDonadoCausa2(String username) throws RemoteException;
    public String totalDonadoAmbasCausas(String username) throws RemoteException;
    
}