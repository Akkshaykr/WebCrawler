package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.profiler.Profiled;

import java.util.List;


public interface WebCrawler {


  @Profiled
  CrawlResult crawl(List<String> startingUrls);


  default int getMaxParallelism() {
    return 1;
  }
}
