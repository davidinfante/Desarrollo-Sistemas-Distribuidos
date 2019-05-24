# Desarrollo-Sistemas-Distribuidos
Prácticas de la asignatura Desarollo de Sistemas Distribuidos en la UGR

### Práctica 3 - JavaRMI
Modo de uso en una sola máquina:

- Modificar en Cliente y Servidor la variable numServer_max por el número máximo de servidores que sean necesarios.
- Cada servidor va identificado por un ID desde 0 hasta numServer_max.
- Compilar el Cliente con -Djava.security.manager -Djava.security.policy=./src/cliente/client.policy
- Compilar el Servidor con -Djava.security.manager -Djava.security.policy=./src/servidor/server.policy
- Ejecutar Servidor para cada servidor necesario y darle su ID.
- Cuando el servidor está listo se lanza un mensaje y se queda esperando peticiones.
- Lanzar los Clientes (los cuales se conectarán aleatoriamente a un servidor operativo) y realizar las operaciones.


*** EXAMEN ***  
a)
- Se han añadido las operaciones pertinentes en el Cliente (Donar Causa 2, Consultar total Causa 2 y
	Consultar total Ambas Causas).
- Se han añadido las operaciones donarCausa2(String), totalDonadoCausa2(String) y totalDonadoAmbasCausas(String)
	en la Interfaz ServidorCliente.
- Se han añadido las operaciones getTotalDonadoCausa2(), getTotalDonadoAmbasCausas() en la Interfaz Servidor Servidor.
- Se han implementado las nuevas operaciones en el BalanceadorCarga

b)
- Por defecto se ha establecido el número de réplicas a 3 (0, 1 y 2), pero el número de servidores es 
	parametrizable mediante las variables numServer_max en Servidor y Cliente.

c)
- Se ha establecido la réplica con ID=2 (la tercera) como réplica en la que no se puede donar a la Causa 2.

### Práctica 4 - Sistema Domótico

- Ejecutar: node server.js
- Conectar a: localhost:8080 para la interfaz de usuario
- Conectar a: localhost:8080/sensors.html para la vista de sensores

El sistema enciende automáticamente el aire acondicionado si se superan los 22 grados;
lo apaga si la temperatura se mantiene por debajo de 23.

La persiana tiene 10 posiciones entre 0% subida y 100% subida, subiendola o bajándola de 10% en 10%.

Si el sistema se encuentra en modo "Auto", la persiana se bajará si se exceden los límites de
luminosidad y temperatura. Además mostrará una notificación en rojo si se superan ambos límites.

Si se detecta presencia se encenderá la TV automáticamente; si se deja de detectar y tras 60 segundos
sigue sin haber presencia, se apagará la TV.