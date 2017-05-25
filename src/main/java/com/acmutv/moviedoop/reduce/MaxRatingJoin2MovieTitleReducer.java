/*
  The MIT License (MIT)

  Copyright (c) 2017 Giacomo Marciani and Michele Porretta

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:


  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.


  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */
package com.acmutv.moviedoop.reduce;

import com.acmutv.moviedoop.Query1;
import com.acmutv.moviedoop.util.RecordParser;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * The reducer for the {@link Query1} job.
 *
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class MaxRatingJoin2MovieTitleReducer extends Reducer<LongWritable,DoubleWritable,Text,DoubleWritable> {

  /**
   * Map (movieId,movieTitle)
   */
  private Map<Long,String> movieIdToMovieTitle = new HashMap<>();

  /**
   * The movie title to emit.
   */
  private Text movieTitle = new Text();

  /**
   * The movie rating to emit.
   */
  private DoubleWritable movieRating = new DoubleWritable();

  /**
   * Configures the reducer.
   * @param ctx the job context.
   */
  protected void setup(Context ctx) {
    try {
      for (URI uri : ctx.getCacheFiles()) {
        Path path = new Path(uri);
        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                new FileInputStream(path.getName())));
        String line;
        while ((line = br.readLine()) != null) {
          Map<String,String> movie = RecordParser.parse(line, new String[] {"id","title","genres"},",");
          long movieId = Long.valueOf(movie.get("id"));
          String movieTitle = movie.get("title");
          this.movieIdToMovieTitle.put(movieId, movieTitle);
        }
      }
    } catch (IOException exc) {
      exc.printStackTrace();
    }
  }

  /**
   * The reduction routine.
   *
   * @param key the input key.
   * @param values the input values.
   * @param ctx the context.
   * @throws IOException when the context cannot be written.
   * @throws InterruptedException when the context cannot be written.
   */
  public void reduce(LongWritable key, Iterable<DoubleWritable> values, Context ctx) throws IOException, InterruptedException {
    double max = 0.0;
    for (DoubleWritable value : values) {
      max = (value.get() > max) ? value.get() : max;
    }
    this.movieTitle.set(this.movieIdToMovieTitle.get(key.get()));
    this.movieRating.set(max);
    ctx.write(this.movieTitle, this.movieRating);
  }

}
