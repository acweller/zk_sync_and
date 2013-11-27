# Parametros de entrada:
# $1 - qTest (para executar 'queueTest')
# $2 - <host>:<port>
# $3 - (<size> = Total a consumir - Valor default = 50)
# $4 - c   (Indica que é um Consumidor)

teste=${1-qTest}
echo "Tipo de teste = $teste"

conex=${2-cluster1.lab.ic.unicamp.br:44655}
echo "Host:Port = $conex"

total=${3-50}
echo "Total de itens a consumir = $total"

tipo=${4-c}
echo "Tipo de cliente = $tipo"

java -cp ../zookeeper-3.4.5.jar:../lib/jline-0.9.94.jar:../lib/log4j-1.2.15.jar:../lib/slf4j-api-1.6.1.jar:../lib/slf4j-log4j12-1.6.1.jar:..:/conf:.:src:bin zk_sync.and.SyncPrimitive $teste $conex $total $tipo
