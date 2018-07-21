import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

public class Result {

    public static class ResultMapper extends Mapper<LongWritable, Text, IntWritable, DoubleWritable> {

        @Override
        public void map(LongWritable key, Text values, Context context) throws IOException, InterruptedException {
            String[] line = values.toString().trim().split("\t");
            if (line.length != 2) return;
            context.write(new IntWritable(1), new DoubleWritable(Double.parseDouble(line[1])));

        }
    }

    public static class ResultReducer extends Reducer<IntWritable, DoubleWritable, Text,DoubleWritable>{

        @Override
        public void reduce(IntWritable key, Iterable<DoubleWritable>values, Context context)throws IOException,InterruptedException{

            double mse = 0;
            long len = 0;
            for(DoubleWritable v:values){
                mse += v.get();
                len+=1;
            }
            mse /= len;
            mse = Math.sqrt(mse);
            context.write(new Text("RMSE on test set:"), new DoubleWritable(mse));
        }
    }

    public static void main(String[] args)throws Exception{
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);
        job.setMapperClass(ResultMapper.class);
        job.setReducerClass(ResultReducer.class);

        job.setJarByClass(Result.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(DoubleWritable.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }

}

