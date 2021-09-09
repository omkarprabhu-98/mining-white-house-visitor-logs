import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Top10 {
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
			String outKey = "";
			switch (context.getConfiguration().get("CountKey")) {
				case "0":
					if (attributes.length < 3) return;
					// produce <visitorLastName_visitorFirstName_visitorMiddleInitial, 1>
					outKey = attributes[0] + "_" + attributes[1] + "_" 
						+ attributes[2];
					break;
				case "1":
					if (attributes.length < 21) return;
					// produce <visiteeLastName_visiteeFirstName, 1>
					outKey = attributes[19] + "_" + attributes[20];
					break;
				case "2":
					if (attributes.length < 21) return;
					// produce <visitorLastName_visitorFirstName_visitorMiddleInitial_&&_visiteeLastName_visiteeFirstName, 1>
					outKey = attributes[0] + "_" + attributes[1] + "_" 
						+ attributes[2] + "_&&_" + attributes[19] + "_" + attributes[20];
					break;
				case "3":
					if (attributes.length < 22) return;
					// produce <location, 1>
					outKey = attributes[21];
					break;
				default:
					outKey = "";
			}
			word.set(outKey.toLowerCase());
			context.write(word, one);
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
	// JOB-2 Mapper to get local top 10
	public static class Top10Mapper
			extends Mapper <Object, Text, NullWritable, Text> {
		PriorityQueue<Pair> localTop10 =
			new PriorityQueue<Pair>(11, Comparator.comparing(Pair::getKey));
		@Override
		public void map(Object key, Text value, Context context) 
				throws IOException, InterruptedException {
			// This reads the reducer output which is LastName_FirstName\tCount
			String line = value.toString();
			String[] kvPair = line.split("\t"); 
			String inputKey = kvPair[0];
			Integer count = Integer.parseInt(kvPair[1]);
			localTop10.add(new Pair(count, inputKey));
			if (localTop10.size() > 10) {
				localTop10.poll();
			}
		}
		@Override
		protected void cleanup(Context context) 
			throws IOException, InterruptedException {
			// Merge into the same key (NULL)
			// for the single reducer to reduce
			while (!localTop10.isEmpty()) {
				Pair e = localTop10.poll();
				context.write(NullWritable.get(), 
					new Text(e.getKey() + "--" + e.getValue()));
			}
		}
	}
	// JOB-2 Reducer to get top 10 among the local
	public static class Top10Reducer
		extends Reducer<NullWritable, Text, Text, IntWritable> {
		PriorityQueue<Pair> top10 =
			new PriorityQueue<Pair>(11, Comparator.comparing(Pair::getKey));
		@Override
		protected void reduce(NullWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			for (Text value : values) {
				// System.out.print(values);
				String[] kvPair = value.toString().split("--");
				if (kvPair.length < 2) continue;
				Integer count = Integer.parseInt(kvPair[0]);
				String mapKey = kvPair[1];
				top10.add(new Pair(count, mapKey));
				if (top10.size() > 10) {
					top10.poll();
				}
			}
			while (!top10.isEmpty()) {
				Pair e = top10.poll();
				context.write(new Text(e.getValue()), 
					new IntWritable(e.getKey()));
			}
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
		// key 0 -> count visitors
		// key 1 -> count visitee
		// key 2 -> count visitor-visitee
		// key 3 -> count meeting locations (no of visitors)
		config1.set("CountKey", args[0]);
		Job count = Job.getInstance(config1, "Count");
		count.setJarByClass(Top10.class);
		count.setOutputKeyClass(Text.class);
		count.setOutputValueClass(IntWritable.class);
		count.setMapperClass(CountMapper.class);
		count.setReducerClass(CountReducer.class);
		// args[1] => input, args[2] => tmp
		// set input split size to be 1 MB
		FileInputFormat.setMaxInputSplitSize(count, 40000000);
		FileInputFormat.addInputPath(count, new Path(args[1]));
		FileOutputFormat.setOutputPath(count, new Path(args[2]));
		count.setNumReduceTasks(3);
		count.waitForCompletion(true);
		if (count.isSuccessful()) {
			Configuration config2 = new Configuration();
			Job top10 = Job.getInstance(config2, "Top10");
			top10.setJarByClass(Top10.class);
			top10.setMapOutputKeyClass(NullWritable.class);
			top10.setMapOutputValueClass(Text.class);
			top10.setOutputKeyClass(Text.class);
			top10.setOutputValueClass(IntWritable.class);
			top10.setMapperClass(Top10Mapper.class);
			top10.setReducerClass(Top10Reducer.class);
			// args[2] => tmp, args[3] => output
			FileInputFormat.setMaxInputSplitSize(top10, 40000000);
			FileInputFormat.addInputPath(top10, new Path(args[2]));
			FileOutputFormat.setOutputPath(top10, new Path(args[3]));
			top10.setNumReduceTasks(1);
			top10.waitForCompletion(true);
		}
	}
}