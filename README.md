zk_sync_and
===========

Barreiras e Filas (Produtor / Consumidor) no Zookeeper


Barreiras:
----------

1. Implementação de Barreiras:
	* Para gerar os Locks é utilizado "synchronized" (com 'mutex.Wait' e 'mutex.Notify')
	* Inicialmente é criada uma barreira
	* Que só será ultrapassada quando uma certa quantidade de Clientes chegar à barreira.


2. Executar implementação de Barreiras:

    a. Abrir 3 terminais Linux e acessar o cluster
    
        ssh mc914u11@cluster1.lab.ic.unicamp.br
		
    b. Verificar se os 3 servidores do Zookeeper estão ativos
    
        ./zookeeper-3.4.5/bin/zkServer.sh stop node01.cfg
        ./zookeeper-3.4.5/bin/zkServer.sh stop node03.cfg
        ./zookeeper-3.4.5/bin/zkServer.sh stop node04.cfg
        
        ./zookeeper-3.4.5/bin/zkServer.sh start node01.cfg
        ./zookeeper-3.4.5/bin/zkServer.sh start node03.cfg
        ./zookeeper-3.4.5/bin/zkServer.sh start node04.cfg
		
    c. Para cada terminal acessar via SSH diferente
    
        ssh node01
        ssh node03
        ssh node04
		
    d. Acessar o diretório do aplicativo
    
        cd ./zookeeper-3.4.5/zk_sync_and/
		
    e. Iniciar o arquivo de Barreiras (em cada terminal):
	
        ./runbarrier.sh
        
        Parâmetros default:
        bTest
        cluster1.lab.ic.unicamp.br:44655
        3
			
    f. Após iniciar o 3º Cliente, todos eles passam a funcionar.


Filas (Produtor e Consumidores):
--------------------------------

3. Implementação de Filas (Produtor e Consumidores):
    * Para gerar os Locks é utilizado "synchronized" (com 'mutex.Wait' e 'mutex.Notify')
    * (para simplificar) O primeiro que entra cria o objeto !Zookeeper
    * Só há um Produtor que cria os novos nós e os insere na lista (Zookeeper é que gera o número sequencial)
    * Os Consumidores buscam o nó com menor valor (utiliza um loop para encontrá-lo - NÃO É BOM)
    * e deletam concorrentemente (o ZK é que garante a exclusão mútua)
    * O que perder a disputa recebe uma exceção e reinicia o processo
    * Se não houver itens os Consumidores dormem até o Produtor inserir novos.


0. Demostrar implementação de Filas (Produtor / Consumidor)

    a. Iniciar o arquivo de Consumidor (em dois terminais):
    
        ./runconsumer.sh
        
        Parâmetros default:
        qTest
        cluster1.lab.ic.unicamp.br:44655
        50
        c
			
    b. Iniciar o arquivo de Produtor (em dois terminais):
    
        ./runproducer.sh
		
        Parâmetros default:
        qTest
        cluster1.lab.ic.unicamp.br:44655
        100
        p
        
    c. Assim que o Produtor iniciar, os Consumidores são acordados e começam a disputar os Nós.
