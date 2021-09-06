These instructions would be for macOS with Apple M1 chip

## Installation of Java

This provides a OpenJDK distribution built for arm64
- https://www.azul.com/downloads/?version=java-11-lts&os=macos&architecture=arm-64-bit&package=jdk

## Installation of Hadoop

```
$ brew install hadoop
```

This gets installed here: `/opt/homebrew/Cellar/hadoop/3.3.1/`
- `etc\hadoop` has the configurations
- `sbin` contains start scripts for the daemons
- `bin` contains the binaries for hdfs/hadoop

Configure HADOOP HOME and JAVA_HOME in `~/.zshrc`
```
export JAVA_HOME=$(/usr/libexec/java_home)
export HADOOP_HOME="/opt/homebrew/Cellar/hadoop/3.3.1/libexec/"
```

Follow [this tutorial](https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html) to configure the Single Node setup and Run a sample job. Also can use [this](https://towardsdatascience.com/installing-hadoop-on-a-mac-ec01c67b003c) as a reference

## Troubleshoot

- Datanode not coming up
    ```
    rm -Rf /tmp/hadoop-<username>/*
    ```
    start from step one in the tutorial
- Sometime namenode does not come up, try
    ```
    sbin/stop-all.sh
    hdfs namenode -format
    sbin/start-all.sh
    ```
