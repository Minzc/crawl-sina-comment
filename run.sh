print_usage () 
{
  echo "Usage: run COMMAND"
  echo "where COMMAND is one of the follows:"
  echo "	getcmmn -url <weiboUrl>	-u <username> -p <password>	获取评论用户的id"
  echo "	getrpst -url <weiboUrl>	-u <username> -p <password>	获取转发用户的id"
  exit 1
}

if [ $# = 0 ] || [ $1 = "help" ]; then
  print_usage
fi
COMMAND=$1
shift


if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi

JAVA=$JAVA_HOME/bin/java
JAVA_HEAP_MAX=-Xmx2000m 

CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar
CLASSPATH=${CLASSPATH}:conf
CLASSPATH=${CLASSPATH}:`ls |grep jar|grep bin`
for f in lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

params=""
if [ $COMMAND = "getcmmn" ]; then
	params=$@" -c -id"
	CLASS="SinaCraw"
elif [ $COMMAND = "getrpst" ]; then
	params=$@" -r -id"
	CLASS="SinaCraw"
fi

"$JAVA" -Xmx2048m -Djava.awt.headless=true $JAVA_HEAP_MAX -classpath "$CLASSPATH" $CLASS $params
#java -jar craw.jar $params
