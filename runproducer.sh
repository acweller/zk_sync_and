# Parametros de entrada:
# $1 - qTest (para executar 'queueTest')
# $2 - <host>:<port>
# $3 - (<size> = Total a produzir - Valor default = 100)
# $4 - p   (Indica que é um Produtor)

teste=${1-qTest}
echo "Tipo de teste = $teste"

conex=${2-cluster1.lab.ic.unicamp.br:44655}
echo "Host:Port = $conex"

total=${3-100}
echo "Total de itens a produzir = $total"

tipo=${4-p}
echo "Tipo de cliente = $tipo"

java -cp ../zookeeper-3.4.5.jar:../lib/jline-0.9.94.jar:../lib/log4j-1.2.15.jar:../lib/slf4j-api-1.6.1.jar:../lib/slf4j-log4j12-1.6.1.jar:..:/conf:.:src:bin zk_sync.and.SyncPrimitive $teste $conex $total $tipo
