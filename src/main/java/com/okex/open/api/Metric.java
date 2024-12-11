package com.okex.open.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Metric {
  private String name;
  private Long start;
  private List<Long> samples;

  public Metric(String name) {
    this.name = name;
    samples = new ArrayList<>();
  }

  public void pushSampe(Long sample){
    samples.add(sample);
  }

  public void startSample(){
    start = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
  }

  public void stopSampe(){
    Long end = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
    samples.add(end - start);
  }

  public void reportAll(){
    System.out.print(name);
    System.out.print(" reportAll ");
    for (Long d : samples) {

      System.out.print(d);

      System.out.print(" ");
    }

    System.out.println();
  }

  public void reportAvg(){
    System.out.print(name);
    System.out.print(" reportAVG ");
    Long avg = 0L;
    for (Long d : samples) {
      avg += d;
    }
    avg /= samples.size();
    System.out.print(avg);
    System.out.println();
    
  }

  public void clear(){
    samples.clear();
  }
}
