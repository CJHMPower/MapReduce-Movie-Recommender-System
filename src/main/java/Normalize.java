import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Normalize {

    public static class NormalizeMapper extends Mapper<LongWritable, Text, Text, Text> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //value: movieA: movieB\t relation
            String[] movie_relation = value.toString().trim().split("\t");
            if(movie_relation.length != 2) {
                return;
            }

            String[] movieAB = movie_relation[0].split(":");
            if(movieAB.length!=2)return;
            String movieA = movieAB[0],movieB = movieAB[1];
            context.write(new Text(movieA), new Text(movieB + "=" + movie_relation[1]));

        }
    }

    public static class NormalizeReducer extends Reducer<Text, Text, Text, Text> {
        // reduce method
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            //sum -> denominator
            //context write ->
            String movieA = key.toString().trim();
            double total = 0;

            HashMap<String, Double>map = new HashMap<String,Double>();
            for(Text text:values){
               String[] item = text.toString().split("=");
               String movie = item[0].trim();
               double relation = Double.parseDouble(item[1]);
               if(movie.equals(movieA)) total = relation;
               else map.put(movie, relation);
            }

            for(String movie:map.keySet()){
                double normalized_rating = map.get(movie)/Math.sqrt(total);
                context.write(key, new Text(movie+"="+normalized_rating));
            }
            context.write(key, new Text(movieA+"="+total));
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);
        job.setMapperClass(NormalizeMapper.class);
        job.setReducerClass(NormalizeReducer.class);

        job.setJarByClass(Normalize.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
