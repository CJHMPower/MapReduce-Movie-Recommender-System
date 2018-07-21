import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

/**
 * Created by Michelle on 11/12/16.
 */
public class Sum {

    public static class SumMapper extends Mapper<LongWritable, Text, Text, Text> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
                 String[] line = value.toString().split("\t");
                 if(line.length!=2)return;
                 context.write(new Text(line[0]),new Text(line[1]));

        }
    }

    public static class SumReducer extends Reducer<Text, Text, Text, DoubleWritable> {
        // reduce method
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            //key = user:movieA
            //value = <subSum, subSub>
            double sum = 0;
            double normalizer = 0;
            double relation = 0, rating = 0;
            for(Text text:values){
                String[] rating_relation = text.toString().trim().split(":");
                if(rating_relation.length!=2)return;
                rating = Double.parseDouble(rating_relation[0]);
                relation = Double.parseDouble(rating_relation[1]);
                normalizer += relation;
                sum += relation * rating;
            }
            context.write(key, new DoubleWritable(sum/normalizer));
           
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);
        job.setMapperClass(SumMapper.class);
        job.setReducerClass(SumReducer.class);

        job.setJarByClass(Sum.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
