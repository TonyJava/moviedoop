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
package com.acmutv.moviedoop.query2.reduce;

import com.acmutv.moviedoop.common.model.MovieWritable;
import com.acmutv.moviedoop.common.model.RatingsWritable;
import com.acmutv.moviedoop.query2.Query2_1;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The reducer for the {@link Query2_1} job.
 *
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class RatingJoinGenreReducer extends Reducer<LongWritable, Text, Text, DoubleWritable> {

  /**
   * The map (movieId, movieRating) for the inner join.
   */
  private Map<Long, Double> ratings = new HashMap<>();

  /**
   * The map (movieId,movieTitle) for the inner join.
   */
  private Map<Long, String> genres = new HashMap<>();

  /**
   * The movie title to emit.
   */
  private Text genre = new Text();

  /**
   * The movie rating to emit.
   */
  private DoubleWritable movieRating = new DoubleWritable();

  /**
   * The reduction routine.
   *
   * @param key the input key.
   * @param values the input values.
   * @param ctx the context.
   * @throws IOException when the context cannot be written.
   * @throws InterruptedException when the context cannot be written.
   */
  public void reduce(LongWritable key, Iterable<Text> values, Context ctx) throws IOException, InterruptedException {

    this.ratings.clear();
    this.genres.clear();

    for (Text value : values) {
      if (value.toString().startsWith("R")) {
        long movieId = key.get();
        double rating = Double.valueOf(value.toString().substring(1));
        if (this.ratings.getOrDefault(movieId, Double.MIN_VALUE).compareTo(rating) < 0) {
          this.ratings.put(movieId, rating);
        }
      } else if (value.toString().startsWith("G")) {
        long movieId = key.get();
        String genre = value.toString().substring(1);
        this.genres.put(movieId, genre);
      } else {
        final String errmsg = String.format("Object is neither %s nor %s",
            RatingsWritable.class.getName(), MovieWritable.class.getName());
        throw new IOException(errmsg);
      }
    }

    for (Map.Entry<Long, Double> entryRating : this.ratings.entrySet()) {

      long movieId = entryRating.getKey();
      double score = entryRating.getValue();
      String movieGenre = this.genres.get(movieId);

      //

      this.genre.set(movieGenre);
      this.movieRating.set(score);
      System.out.printf("# RED # Write (%s,%f)\n", movieGenre, score);
      ctx.write(this.genre, this.movieRating);
    }
  }

}
