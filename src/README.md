
# Suzuki Kasami - Sistemas Distribuidos

### Compilar

```
make
```

### Ejecución
```
java Process N <archivo> <capacidad> <velocidad> <delay> <bearer> <idProceso>
```

* Se añade idProceso. Este valor parte desde 0 para el primer proceso creado, para los siguientes se debe incrementar en uno (Ver casos de ejemplo)

### Ejemplos de comandos
#### Primer caso de prueba

 ```  
java Process 3 test.txt 5 1 1000 true 0
 ```

 ```  
java Process 3 test.txt 5 1 1000 false 1
 ```

 ```  
java Process 3 test.txt 5 1 1000 false 2
 ```
#### Segundo caso de prueba

 ```  
java Process 4 test2.txt 5 1 1000 false 0
 ```

 ```  
java Process 4 test2.txt 2 1 1000 true 1
 ```

 ```  
java Process 4 test2.txt 2 1 1000 false 2
 ```

 ```  
java Process 4 test2.txt 2 1 1000 false 3
 ```

Ok, lo mismo para el tercer caso. 

### Adicionales
* El tema del delay no influye demasiado. El algoritmo va esperar en todo momento que lleguen los procesos antes de comenzar.
* El salto de linea se considera como un solo carácter/letra.


### Cosas no logradas
* Al aplicar método kill(), los procesos que esperan token no pudieron ser terminados.



