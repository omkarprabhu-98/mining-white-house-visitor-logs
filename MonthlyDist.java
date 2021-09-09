import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MonthlyDist {

	// JOB-1 Mapper to get the count for the required key
	public static class CountMapper
			extends Mapper <LongWritable, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		@Override
		public void map(LongWritable key, Text value, Context context) 
				throws IOException, InterruptedException {
			// skip first line of csv
			if (key.get() == 0) return;
			String line = value.toString();
			String[] attributes = line.split(","); 
			if(attributes.length < 12) return;
			String dateString = attributes[11];
			Pattern pattern = 
				Pattern.compile("^(\\d{1,2})\\/(\\d{1,2})\\/(\\d{4})\\s");
			Matcher matcher = pattern.matcher(dateString);
			String month = "NULL";
			String year = "NULL";
			if (matcher.find()) {
				month = matcher.group(1);
				year = matcher.group(3);
			}
			word.set(month + "-" + year);
			switch (context.getConfiguration().get("CountKey")) {
				case "0":
					// produce <month-year, 1>
					context.write(word, one);
					break;
				case "1":
					if (attributes.length < 21) return;
					// produce <month-year, 1> if visitee == POTUS
					if (attributes[20].equals("POTUS")) {
						context.write(word, one);
					}
					break;
			}
		}
  	}
	// JOB-1 Reducer to aggregate the counts
	public static class CountReducer
			extends Reducer<Text, IntWritable, Text, IntWritable> {
		@Override
		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable value : values) {
				sum += value.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}

	public static class Pair {
		Integer key;
		String value;
		public Pair(Integer key, String value) {
			this.key = key;
			this.value = value;
		}
		public Integer getKey() {
			return this.key;
		}
		public String getValue() {
			return this.value;
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration config1 = new Configuration();
		config1.set("CountKey", args[0]);
		Job count = Job.getInstance(config1, "Count");
		count.setJarByClass(MonthlyDist.class);
		count.setOutputKeyClass(Text.class);
		count.setOutputValueClass(IntWritable.class);
		count.setMapperClass(CountMapper.class);
		count.setReducerClass(CountReducer.class);
		// args[1] => input, args[2] => output
		// FileInputFormat.setMaxInputSplitSize(count, 40000000);
		FileInputFormat.addInputPath(count, new Path(args[1]));
		FileOutputFormat.setOutputPath(count, new Path(args[2]));
		count.setNumReduceTasks(3);
		count.waitForCompletion(true);
	}
}