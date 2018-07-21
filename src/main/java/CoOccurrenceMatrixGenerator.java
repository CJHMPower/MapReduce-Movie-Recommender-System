import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CoOccurrenceMatrixGenerator {
	public static class MatrixGeneratorMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {

		// map method
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//value = userid\t movie1:rating movie2:rating....
			//[1,2,3] -> 1,1 1,2 1,3 2,1 2,2 2,3...
			String[] line = value.toString().split("\t");
			if(line.length != 2)return;

			String[] ratings = line[1].split(",");
			String[] movies = new String[ratings.length];
			for(int i=0;i<movies.length;i++){
				movies[i] = ratings[i].split(":")[0].trim();
			}

			/* double factor = 1.0/Math.log(1 + movies.length); */
			for(int i=0;i<movies.length;i++){
				for(int j=0;j<movies.length;j++){
                    	context.write(new Text(movies[i]+":"+movies[j]),new DoubleWritable(1.0));
					}
			}
		}
	}

	public static class MatrixGeneratorReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
		// reduce method
		@Override
		public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
				throws IOException, InterruptedException {
             double sum = 0;
             for(DoubleWritable v:values)sum += v.get();
             context.write(key, new DoubleWritable(sum));
			
		}
	}
	
	public static void main(String[] args) throws Exception{
		
		Configuration conf = new Configuration();
		
		Job job = Job.getInstance(conf);
		job.setMapperClass(MatrixGeneratorMapper.class);
		job.setReducerClass(MatrixGeneratorReducer.class);
		
		job.setJarByClass(CoOccurrenceMatrixGenerator.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		
		TextInputFormat.setInputPaths(job, new Path(args[0]));
		TextOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.waitForCompletion(true);
		
	}
}
