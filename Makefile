HADOOP = $(HADOOP_HOME)/bin/hadoop
COUNT_KEY = 0

APP = Top10
APP_JAR = $(APP).jar
SRC = $(APP).java 
OUT = ${APP}Tmp
APP_OUT = $(OUT)/$(APP)
DATA_DIR = ${OUT}/data
DATA = 1x

jar: $(SRC)
	mkdir -p $(OUT)
	mkdir -p $(APP_OUT)
	javac -classpath `$(HADOOP) classpath` -d $(APP_OUT)/ $(SRC) 
	jar -cvf $(APP_JAR) -C $(APP_OUT)/ .
	mv $(APP_JAR) $(OUT)

define get_data
	curl $(1) -o $(2).zip && unzip $(2).zip -d $(DATA_DIR) && rm $(2).zip && rm -rf $(DATA_DIR)/__MACOSX
endef

data:
	mkdir -p $(DATA_DIR)
ifeq ($(DATA), 2x)
	$(call get_data, https://obamawhitehouse.archives.gov/sites/default/files/disclosures/whitehouse-waves-2013.csv__0.zip, data2)
else ifeq ($(DATA), 6x)
	$(call get_data, https://obamawhitehouse.archives.gov/sites/default/files/disclosures/whitehouse-waves-2014_03.csv_.zip, data1)
	$(call get_data, https://obamawhitehouse.archives.gov/sites/default/files/disclosures/whitehouse-waves-2012.csv_.zip, data3)
else ifeq ($(DATA), 14x)
	$(call get_data, https://obamawhitehouse.archives.gov/sites/default/files/disclosures/whitehouse-waves-2013.csv__0.zip, data2)
	$(call get_data, https://obamawhitehouse.archives.gov/sites/default/files/disclosures/whitehouse-waves-2012.csv_.zip, data3)
	$(call get_data, https://obamawhitehouse.archives.gov/files/disclosures/visitors/WhiteHouse-WAVES-Released-1210.zip, data4)
else 
	# ifeq ($(DATA), 1x)
	$(call get_data, https://obamawhitehouse.archives.gov/sites/default/files/disclosures/whitehouse-waves-2014_03.csv_.zip, data1)
endif
	$(HADOOP_HOME)/bin/hdfs dfs -mkdir -p input
	$(HADOOP_HOME)/bin/hdfs dfs -put -f $(DATA_DIR)/* input

run: jar data
ifeq ($(APP), Top10)
	$(HADOOP) jar $(OUT)/$(APP_JAR) $(APP) $(COUNT_KEY) input tmp output
else 
	$(HADOOP) jar $(OUT)/$(APP_JAR) $(APP) $(COUNT_KEY) input output
endif
	mkdir -p $(OUT)/output/
	$(HADOOP_HOME)/bin/hdfs dfs -get output/* $(OUT)/output/
	cat $(OUT)/output/part-r-*

clean: 
	rm -rf $(OUT) \
	; $(HADOOP_HOME)/bin/hdfs dfs -rm -r input \
	; $(HADOOP_HOME)/bin/hdfs dfs -rm -r tmp \
	; $(HADOOP_HOME)/bin/hdfs dfs -rm -r output \