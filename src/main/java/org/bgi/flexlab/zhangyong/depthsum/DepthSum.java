package org.bgi.flexlab.zhangyong.depthsum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class DepthSum {

    // TokenizerMapper作为Map阶段，需要继承Mapper，并重写map()函数
    public static class PosKeyMapper extends Mapper<Object, Text, Text, Text>{

        private Text posKey = new Text();
        private Text depthValue = new Text();
        StringBuffer stringBuffer = new StringBuffer();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] lines = value.toString().split("\t");

            stringBuffer.setLength(0);
            posKey.set(new Text(stringBuffer.append(lines[0]).append("\t").append(lines[1]).toString()));
            stringBuffer.setLength(0);
            stringBuffer.append(lines[2]);
            for(int i = 3; i < lines.length; i++) {
                stringBuffer.append("\t").append(lines[i]);
            }
            depthValue.set(new Text(stringBuffer.toString()));
            context.write(posKey, depthValue);
        }
    }

    // IntSumReducer作为Reduce阶段，需要继承Reducer，并重写reduce()函数
    public static class DepthSumReducer extends Reducer<Text,Text,NullWritable,Text> {
        private Text outputDepth = new Text();
        StringBuffer stringBuffer = new StringBuffer();

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            List<DepthList> depths = new ArrayList<>();

            stringBuffer.setLength(0);
            stringBuffer.append(key.toString()).append("\t");
            for (Text depthString: values) {
                String[] depthStrings = depthString.toString().split("\t");
                for(int i = 0; i < depthStrings.length; i++) {
                    if(i >= depths.size()) {
                        DepthList depthList = new DepthList();
                        depths.add(depthList);
                    }
                    depths.get(i).add(Integer.valueOf(depthStrings[i]));
                }
            }

            for(int i = 0; i < depths.size(); i++) {
                long depthSum = 0;
                int coverage0count = 0, coverage5count = 0;

                DepthList depthList = depths.get(i);
                for (int depth: depthList.depthList) {
                    depthSum += depth;
                    if (depth > 0) coverage0count++;
                    if (depth >= 5) coverage5count++;
                }
                stringBuffer.append(depthSum);
                stringBuffer.append(";");
                stringBuffer.append(coverage0count);
                stringBuffer.append(";");
                stringBuffer.append(coverage5count);
                if(i != depths.size() - 1)
                    stringBuffer.append("\t");
            }
            outputDepth.set(new Text(stringBuffer.toString()));

            context.write(NullWritable.get(), outputDepth);
        }


        public class DepthList {
            private List<Integer> depthList = new ArrayList<>();

            public int get(int i) {
                return depthList.get(i);
            }

            public void add(int val) {
                depthList.add(val);
            }

            public void clear() {
                depthList.clear();
            }
        }
    }



    public static void main(String[] args) throws Exception {
        // 加载hadoop配置
        Configuration conf = new Configuration();

        // 校验命令行输入参数
        if (args.length < 2) {
            System.err.println("Usage: depthSum <in> [<in>...] <out> reducerNumber");
            System.exit(2);
        }

        // 构造一个Job实例job，并命名为"word count"
        Job job = new Job(conf, "depth sum count");

        // 设置jar
        job.setJarByClass(DepthSum.class);

        // 设置Mapper
        job.setMapperClass(PosKeyMapper.class);

        // 设置Reducer
        job.setReducerClass(DepthSumReducer.class);

        // 设置OutputKey
        job.setOutputKeyClass(Text.class);
        // 设置OutputValue
        job.setOutputValueClass(Text.class);
        // 设置输入
        job.setInputFormatClass(TextInputFormat.class);
        // 设置输出
        job.setOutputFormatClass(TextOutputFormat.class);

        // 添加输入路径
        for (int i = 0; i < args.length - 2; ++i) {
            FileInputFormat.addInputPath(job, new Path(args[i]));
        }

        // 添加输出路径
        FileOutputFormat.setOutputPath(job, new Path(args[args.length - 2]));

        job.setNumReduceTasks(Integer.valueOf(args[args.length - 1]));

        // 等待作业job运行完成并退出
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}