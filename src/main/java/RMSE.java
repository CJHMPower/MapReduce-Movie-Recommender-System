import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;

import java.io.IOException;
import java.util.HashMap;


public class RMSE {
    public static class RMSEMapper extends Mapper<LongWritable,Text,Text,DoubleWritable>{

        @Override
        public void map(LongWritable key, Text values, Context context)throws IOException,InterruptedException{
            String[] line = values.toString().trim().split("\t");
            if(line.length !=2 )return;

            context.write(new Text(line[0]), new DoubleWritable(Double.parseDouble(line[1])));
        }
    }

    public static class TestMapper extends Mapper<LongWritable, Text, Text, DoubleWritable>{

        @Override
        public void map(LongWritable key, Text values, Context context)throws IOException,InterruptedException{
            String[] user_movie_rating = values.toString().trim().split(",");
            if(user_movie_rating.length != 3)return;

            String user = user_movie_rating[0];
            String movie = user_movie_rating[1];
            double rating = Double.parseDouble(user_movie_rating[2]);
            context.write(new Text(user+":"+movie),new DoubleWritable(rating));
        }
    }

    public static class RMSEReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable>{

        @Override
        public void reduce(Text key, Iterable<DoubleWritable>values, Context context)throws IOException, InterruptedException{
            double mse = 0;
            int i = 2;
            double[] predict_truth = new double[2];
            for(DoubleWritable value:values){
                i -= 1;
                if(i<0)return;
                predict_truth[i] = value.get();
            }

            if(i>0)return;

            mse = Math.pow(predict_truth[0] - predict_truth[1],2);
            context.write(key, new DoubleWritable(mse));
        }
    }

    public static void main(String[] args)throws Exception{
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);
        job.setJarByClass(Multiplication.class);

        ChainMapper.addMapper(job, RMSEMapper.class, LongWritable.class, Text.class, Text.class, DoubleWritable.class, conf);
        ChainMapper.addMapper(job, TestMapper.class, Text.class, DoubleWritable.class, Text.class, DoubleWritable.class, conf);

        job.setMapperClass(RMSEMapper.class);
        job.setMapperClass(TestMapper.class);

        job.setReducerClass(RMSEReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, RMSEMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, TestMapper.class);

        TextOutputFormat.setOutputPath(job, new Path(args[2]));

        job.waitForCompletion(true);
    }

}
