package iservidor.servidor;

import java.rmi.Remote; 
import java.rmi.RemoteException;

/**
 *
 * @author David Infante Casas
 */
public interface IServidorServidor extends Remote {
    //Operaciones que realiza un servidor sobre otro servidor
    public int getTotalRegistrados() throws RemoteException;
    public double getTotalDonadoCausa1() throws RemoteException;
    public double getTotalDonadoCausa2() throws RemoteException;
    public double getTotalDonadoAmbasCausas() throws RemoteException;
    public boolean isRegistrado(String username) throws RemoteException;
    public boolean haDonado(String username) throws RemoteException;
    
}