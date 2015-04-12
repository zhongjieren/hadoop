package group;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

public class GroupApp {
	static final String INPUT_PATH = "hdfs://cloud4:9000/input";
	static final String OUT_PATH = "hdfs://cloud4:9000/out";
	public static void main(String[] args) throws Exception{
		final Configuration configuration = new Configuration();
		
		final FileSystem fileSystem = FileSystem.get(new URI(INPUT_PATH), configuration);
		if(fileSystem.exists(new Path(OUT_PATH))){
			fileSystem.delete(new Path(OUT_PATH), true);
		}
		
		final Job job = new Job(configuration, GroupApp.class.getSimpleName());
		
		//1.1 指定输入文件路径
		FileInputFormat.setInputPaths(job, INPUT_PATH);
		//指定哪个类用来格式化输入文件
		job.setInputFormatClass(TextInputFormat.class);
		
		//1.2指定自定义的Mapper类
		job.setMapperClass(MyMapper.class);
		//指定输出<k2,v2>的类型
		job.setMapOutputKeyClass(NewK2.class);
		job.setMapOutputValueClass(LongWritable.class);
		
		//1.3 指定分区类
		job.setPartitionerClass(HashPartitioner.class);
		job.setNumReduceTasks(1);
		
		//1.4 TODO 排序、分区
		job.setGroupingComparatorClass(MyGroupingComparator.class);
		//1.5  TODO （可选）合并
		
		//2.2 指定自定义的reduce类
		job.setReducerClass(MyReducer.class);
		//指定输出<k3,v3>的类型
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(LongWritable.class);
		
		//2.3 指定输出到哪里
		FileOutputFormat.setOutputPath(job, new Path(OUT_PATH));
		//设定输出文件的格式化类
		job.setOutputFormatClass(TextOutputFormat.class);
		
		//把代码提交给JobTracker执行
		job.waitForCompletion(true);
	}

	
	static class MyMapper extends Mapper<LongWritable, Text, NewK2, LongWritable>{
		protected void map(LongWritable key, Text value, org.apache.hadoop.mapreduce.Mapper<LongWritable,Text,NewK2,LongWritable>.Context context) throws java.io.IOException ,InterruptedException {
			final String[] splited = value.toString().split("\t");
			final NewK2 k2 = new NewK2(Long.parseLong(splited[0]), Long.parseLong(splited[1]));
			final LongWritable v2 = new LongWritable(Long.parseLong(splited[1]));
			context.write(k2, v2);
		};
	}
	
	static class MyReducer extends Reducer<NewK2, LongWritable, LongWritable, LongWritable>{
		protected void reduce(NewK2 k2, java.lang.Iterable<LongWritable> v2s, org.apache.hadoop.mapreduce.Reducer<NewK2,LongWritable,LongWritable,LongWritable>.Context context) throws java.io.IOException ,InterruptedException {
			long min = Long.MAX_VALUE;
			for (LongWritable v2 : v2s) {
				if(v2.get()<min){
					min = v2.get();
				}
			}
			
			context.write(new LongWritable(k2.first), new LongWritable(min));
		};
	}
	
	/**
	 * 问：为什么实现该类？
	 * 答：因为原来的v2不能参与排序，把原来的k2和v2封装到一个类中，作为新的k2
	 *
	 */
	static class  NewK2 implements WritableComparable<NewK2>{
		Long first;
		Long second;
		
		public NewK2(){}
		
		public NewK2(long first, long second){
			this.first = first;
			this.second = second;
		}
		
		
		@Override
		public void readFields(DataInput in) throws IOException {
			this.first = in.readLong();
			this.second = in.readLong();
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeLong(first);
			out.writeLong(second);
		}

		/**
		 * 当k2进行排序时，会调用该方法.
		 * 当第一列不同时，升序；当第一列相同时，第二列升序
		 */
		@Override
		public int compareTo(NewK2 o) {
			final long minus = this.first - o.first;
			if(minus !=0){
				return (int)minus;
			}
			return (int)(this.second - o.second);
		}
		
		@Override
		public int hashCode() {
			return this.first.hashCode()+this.second.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof NewK2)){
				return false;
			}
			NewK2 oK2 = (NewK2)obj;
			return (this.first==oK2.first)&&(this.second==oK2.second);
		}
	}
	
	/**
	 * 问：为什么自定义该类？
	 * 答：业务要求分组是按照第一列分组，但是NewK2的比较规则决定了不能按照第一列分。只能自定义分组比较器。
	 */
	static class MyGroupingComparator implements RawComparator<NewK2>{

		@Override
		public int compare(NewK2 o1, NewK2 o2) {
			return (int)(o1.first - o2.first);
		}
		/**
		 * @param arg0 表示第一个参与比较的字节数组
		 * @param arg1 表示第一个参与比较的字节数组的起始位置
		 * @param arg2 表示第一个参与比较的字节数组的偏移量
		 * 
		 * @param arg3 表示第二个参与比较的字节数组
		 * @param arg4 表示第二个参与比较的字节数组的起始位置
		 * @param arg5 表示第二个参与比较的字节数组的偏移量
		 */
		@Override
		public int compare(byte[] arg0, int arg1, int arg2, byte[] arg3,
				int arg4, int arg5) {
			return WritableComparator.compareBytes(arg0, arg1, 8, arg3, arg4, 8);
		}
		
	}
}
