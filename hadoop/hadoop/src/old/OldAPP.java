package old;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import mapreduce.WordCountApp;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
/**
 * hadoop版本1.x的包一般是mapreduce
 * hadoop版本0.x的包一般是mapred
 *
 */
public class OldAPP {
	static final String INPUT_PATH = "hdfs://cloud4:9000/hello";
	static final String OUT_PATH = "hdfs://cloud4:9000/out";
	/**
	 * 改动：
	 * 1.不再使用Job，而是使用JobConf
	 * 2.类的包名不再使用mapreduce，而是使用mapred
	 * 3.不再使用job.waitForCompletion(true)提交作业，而是使用JobClient.runJob(job);
	 * 
	 */
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		final FileSystem fileSystem = FileSystem.get(new URI(INPUT_PATH), conf);
		final Path outPath = new Path(OUT_PATH);
		if(fileSystem.exists(outPath)){
			fileSystem.delete(outPath, true);
		}
		
		final JobConf job = new JobConf(conf , WordCountApp.class);
		//1.1指定读取的文件位于哪里
		FileInputFormat.setInputPaths(job, INPUT_PATH);
		//指定如何对输入文件进行格式化，把输入文件每一行解析成键值对
		//job.setInputFormatClass(TextInputFormat.class);
		
		//1.2 指定自定义的map类
		job.setMapperClass(MyMapper.class);
		//map输出的<k,v>类型。如果<k3,v3>的类型与<k2,v2>类型一致，则可以省略
		//job.setMapOutputKeyClass(Text.class);
		//job.setMapOutputValueClass(LongWritable.class);
		
		//1.3 分区
		//job.setPartitionerClass(HashPartitioner.class);
		//有一个reduce任务运行
		//job.setNumReduceTasks(1);
		
		//1.4 TODO 排序、分组
		
		//1.5 TODO 规约
		
		//2.2 指定自定义reduce类
		job.setReducerClass(MyReducer.class);
		//指定reduce的输出类型
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		
		//2.3 指定写出到哪里
		FileOutputFormat.setOutputPath(job, outPath);
		//指定输出文件的格式化类
		//job.setOutputFormatClass(TextOutputFormat.class);
		
		//把job提交给JobTracker运行
		JobClient.runJob(job);
	}

	
	
	/**
	 * 新api:extends Mapper
	 * 老api:extends MapRedcueBase implements Mapper
	 */
	static class MyMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, LongWritable>{
		@Override
		public void map(LongWritable k1, Text v1,
				OutputCollector<Text, LongWritable> collector, Reporter reporter)
				throws IOException {
			final String[] splited = v1.toString().split("\t");
			for (String word : splited) {
				collector.collect(new Text(word), new LongWritable(1));
			}
		}
	}
	
	static class MyReducer extends MapReduceBase implements Reducer<Text, LongWritable, Text, LongWritable>{
		@Override
		public void reduce(Text k2, Iterator<LongWritable> v2s,
				OutputCollector<Text, LongWritable> collector, Reporter reporter)
				throws IOException {
			long times = 0L;
			while (v2s.hasNext()) {
				final long temp = v2s.next().get();
				times += temp;
			}
			collector.collect(k2, new LongWritable(times));
		}
	}
}
