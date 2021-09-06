# Mining White House Visitor Logs

- Check [Data.md](DATA.md): for details about the dataset and groups
- Check [Setup.md](SETUP.md) for installation

## Applications
- Top10
    Get top 10 based on the key:
    - key 0 -> top10 visitors
    - key 1 -> top10 visitee
    - key 2 -> top10 visitor-visitee combination

## Usage
- Default: APP=Top10
    ```
    make run # default DATA=1x
    make DATA=6x run
    make DATA=14x run
    ```
    Here change the COUNT_KEY to calculate Top 10 for different keys:
    - key 0 -> top10 visitors
    - key 1 -> top10 visitee
    - key 2 -> top10 visitor-visitee combination
    eg.
    ```
    make DATA=6x COUNT_KEY=2 run
    ``` 
- APP= 
    ```
    make APP=Avg.java run -> default DATA=1x
    make APP=Avg.java DATA=6x run
    make APP=Avg.java DATA=14x run
    ```

- Clean the hdfs directories and tmp data locally
    ```
    make clean
    ```


## References
- https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html
- 