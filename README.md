# Mining White House Visitor Logs

### FALL 2021 Georgia Tech | CS 6220 | Big Data Systems and Analytics | Assignment 1 | Problem 2 - Option 2 

- Check [Data.md](DATA.md): for details about the dataset and groups
- Check [Setup.md](SETUP.md) for installation
- Experiments and Results can be found here: [Experiments_and_Results.pdf](Experiments_and_Results.pdf)
- Execution logs can be found here: https://drive.google.com/file/d/1SiuYWdhy10NC5H44vopFta9D1wx7zsGe/view?usp=sharing

    `app<app_no>_<key_no>_<dataset_scale>`

## Applications
- Top10
    Get top 10 based on the key:
    - key 0 -> top10 visitors
    - key 1 -> top10 visitee
    - key 2 -> top10 visitor-visitee combination
    - key 3 -> top 10 locations for meetings (based on no of visitors) attending)
- Monthly distribution 
    - key 0 -> of visitors (month, 1)
    - key 1 -> no of visits to the POTUS

## Usage
- Default: APP=Top10
    ```
    make run # default DATA=1x
    make DATA=6x run
    make DATA=14x run
    ```
    Here change the COUNT_KEY to calculate Top 10 for different keys:
    - key 0 -> top10 visitors
    - key 1 -> top10 visitee, etc.
    eg.
    ```
    make DATA=6x COUNT_KEY=2 run
    ``` 
- APP=MonthlyDist
    ```
    make APP=MonthlyDist run -> default DATA=1x
    make APP=MonthlyDist COUNT_KEY=2 DATA=6x run
    make APP=MonthlyDist DATA=14x run
    ```

- Clean the hdfs directories and tmp data locally
    ```
    make APP=MonthlyDist clean
    ```


## References
- https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html