HADOOP = $(HADOOP_HOME)/bin/hadoop
COUNT_KEY = 0

APP = Top10
APP_JAR = $(APP).jar
SRC = $(APP).java 
OUT = ${APP}Tmp
APP_OUT = $(OUT)/$(APP)
DATA_DIR = ${OUT}/data
DATA = 1x

$(APP): $(SRC)
	mkdir -p $(OUT)
	mkdir -p $(APP_OUT)
	javac -classpath `$(HADOOP) classpath` -d $(APP_OUT)/ $(SRC) 
	jar -cvf $(APP_JAR) -C $(APP_OUT)/ .
	mv $(APP_JAR) $(OUT)

$(DATA):
	mkdir -p $(DATA_DIR)
ifeq ($(DATA), 6x)
	curl https://www.whitehouse.gov/wp-content/uploads/2021/05/2021.01_WAVES-ACCESS-RECORDS.csv -o $(DATA_DIR)/data1
	curl https://www.whitehouse.gov/wp-content/uploads/2021/06/2021.02_WAVES-ACCESS-RECORDS.csv -o $(DATA_DIR)/data2
	curl https://www.whitehouse.gov/wp-content/uploads/2021/06/2021.03_WAVES-ACCESS-RECORDS.csv -o $(DATA_DIR)/data3
else ifeq ($(DATA), 14x)
	curl https://www.whitehouse.gov/wp-content/uploads/2021/05/2021.01_WAVES-ACCESS-RECORDS.csv -o $(DATA_DIR)/data1
	curl https://www.whitehouse.gov/wp-content/uploads/2021/06/2021.02_WAVES-ACCESS-RECORDS.csv -o $(DATA_DIR)/data2
	curl https://www.whitehouse.gov/wp-content/uploads/2021/06/2021.03_WAVES-ACCESS-RECORDS.csv -o $(DATA_DIR)/data3
	curl https://www.whitehouse.gov/wp-content/uploads/2021/07/2021.04_WAVES-ACCESS-RECORDS.csv -o $(DATA_DIR)/data4
	curl https://www.whitehouse.gov/wp-content/uploads/2021/08/2021.05_WAVES-ACCESS-RECORDS.csv -o $(DATA_DIR)/data5
else 
	# ifeq ($(DATA), 1x)
	curl https://www.whitehouse.gov/wp-content/uploads/2021/05/2021.01_WAVES-ACCESS-RECORDS.csv -o $(DATA_DIR)/data1
endif
	$(HADOOP_HOME)/bin/hdfs dfs -mkdir -p input
	$(HADOOP_HOME)/bin/hdfs dfs -put -f $(DATA_DIR)/* input

run: $(APP) $(DATA)
	$(HADOOP) jar $(OUT)/$(APP_JAR) $(APP) $(COUNT_KEY) input tmp output
	$(HADOOP_HOME)/bin/hdfs dfs -get output/part-r-00000 $(OUT)/output
	cat $(OUT)/output

clean: 
	rm -rf $(OUT) \
	; $(HADOOP_HOME)/bin/hdfs dfs -rm -r input \
	; $(HADOOP_HOME)/bin/hdfs dfs -rm -r tmp \
	; $(HADOOP_HOME)/bin/hdfs dfs -rm -r output \