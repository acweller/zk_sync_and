# Parametros de entrada:
# $1 - bTest (para executar 'barrierTest')
# $2 - <host>:<port>
# $3 - (<size> = Numero de itens que devem chegar aa barreira (Valor default = 3))


teste=${1-bTest}
echo "Tipo de teste = $teste"

conex=${2-cluster1.lab.ic.unicamp.br:44655}
echo "Host:Port = $conex"

total=${3-3}
echo "Total de itens na barreira = $total"

java -cp ../zookeeper-3.4.5.jar:../lib/jline-0.9.94.jar:../lib/log4j-1.2.15.jar:../lib/slf4j-api-1.6.1.jar:../lib/slf4j-log4j12-1.6.1.jar:..:/conf:.:src:bin zk_sync.and.SyncPrimitive $teste $conex $total
