for i in 1
do
    # make APP=Top10 COUNT_KEY="$i" DATA=1x run >> execution_logs/app1_"$i"_1x 2>&1
    # make APP=Top10 clean
    # make APP=Top10 COUNT_KEY="$i" DATA=2x run >> execution_logs/app1_"$i"_2x 2>&1
    # make APP=Top10 clean
    make APP=MonthlyDist COUNT_KEY="$i" DATA=6x run >> execution_logs/app2_"$i"_6x 2>&1
    make APP=MonthlyDist clean
    make APP=MonthlyDist COUNT_KEY="$i" DATA=14x run >> execution_logs/app2_"$i"_14x 2>&1
    make APP=MonthlyDist clean
done